package com.da.node;

import java.util.Objects;

import com.da.entity.AppendEntriesResult;
import com.da.entity.AppendEntriesRpc;
import com.da.entity.RequestVoteResult;
import com.da.entity.RequestVoteRpc;
import com.da.log.EntryMeta;
import com.da.log.Log;
import com.da.node.nodestatic.GroupMember;
import com.da.node.roles.AbstractNodeRole;
import com.da.node.roles.CandidateNodeRole;
import com.da.node.roles.FollowerNodeRole;
import com.da.node.roles.LeaderNodeRole;
import com.da.node.roles.RoleName;
import com.da.rpc.messages.AppendEntriesResultMessage;
import com.da.rpc.messages.AppendEntriesRpcMessage;
import com.da.rpc.messages.RequestVoteRpcMessage;
import com.da.scheduler.ElectionTimeoutTask;
import com.da.scheduler.LogReplicationTask;
import com.google.common.eventbus.Subscribe;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;


public class RaftNode implements Node {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftNode.class); // slf4j 日志

    private final NodeContext context; // 核心上下文组件
    private boolean started; //是否已经启动
    private volatile AbstractNodeRole role; // 当前的角色与信息

    // 获取当前角色
    public AbstractNodeRole getRole() {
        return role;
    }


    RaftNode(NodeContext context) {
        this.context = context;
    }

    // 获取核心组件上下文
    public NodeContext getContext() {
        return context;
    }

    /**
     * 系统启动时，在EventBus中注册自己感兴趣的消息，以及初始化RPC组件，切换角色为Follower, 并且设置选举超时
     */
    @Override
    public synchronized void start() {
        // 如果已经启动则直接跳过
        if (started) {
            return;
        }
        // 注册到自己的eventbus
        context.eventBus().register(this);
        context.rpcAdapter().initialize();

        // load term, votedFor from store and become follower
        NodeStore store = context.store();
        changeToRole(new FollowerNodeRole(store.getTerm(), store.getVotedFor(), null, scheduleElectionTimeout()));
        started = true;
    }

    
    public void electionTimeout() {
        context.taskExecutor().submit(this::doProcessElectionTimeout);
    }

    private void doProcessElectionTimeout() {
        if (role.getName() == RoleName.LEADER) {
            LOGGER.debug("node {}, current role is leader, ignore election timeout", context.selfId());
            return;
        }

        // follower: start election
        // candidate: restart election
        // increase the term
        int newTerm = role.getTerm() + 1;
        role.cancelTimeoutOrTask();
        LOGGER.debug("start election");
        // 变成candidate 角色
        changeToRole(new CandidateNodeRole(newTerm, scheduleElectionTimeout()));

        EntryMeta lastEntryMeta = context.log().getLastEntryMeta();

        RequestVoteRpc rpc = new RequestVoteRpc();
        rpc.setTerm(newTerm);
        rpc.setCandidateId(context.selfId());
//        rpc.setLastLogIndex(0);
//        rpc.setLastLogTerm(0);
        rpc.setLastLogIndex(lastEntryMeta.getIndex());
        rpc.setLastLogTerm(lastEntryMeta.getTerm());
        context.rpcAdapter().sendRequestVote(rpc, context.group().listEndPointExceptSelf());

    }

    private void becomeFollower(int term, NodeId votedFor, NodeId leaderId, boolean scheduleElectionTimeout) {
        role.cancelTimeoutOrTask(); // 取消超时或者定时器
        // if (leaderId != null && !leaderId.equals(role.getLeaderId(context.selfId()))) {
        //     logger.info("current leader is {}, term {}", leaderId, term);
        // }
        // 重新创建选举超时定时器
        ElectionTimeoutTask electionTimeout = scheduleElectionTimeout();
        changeToRole(new FollowerNodeRole(term, votedFor, leaderId, electionTimeout));
    }

    private void changeToRole(AbstractNodeRole newRole) {
        LOGGER.debug("node {}, role state changed -> {}", context.selfId(), newRole);

        NodeStore store = context.store();
        store.setTerm(newRole.getTerm());
        if (newRole.getName() == RoleName.FOLLOWER) {
            store.setVotedFor(((FollowerNodeRole)newRole).getVotedFor());
        }
        role = newRole;
    }

    private ElectionTimeoutTask scheduleElectionTimeout() {
        return context.scheduler().scheduleElectionTimeoutTask(this::electionTimeout);
    }

    private LogReplicationTask scheduleLogReplicationTask() {
        return context.scheduler().scheduleLogReplicationTask(this::replicateLog);
    }

    @Subscribe
    public void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        context.taskExecutor().submit(
                () -> context.rpcAdapter().replyRequestVote(
                    doProcessRequestVoteRpc(rpcMessage),
                    // 找到发送消息的节点
                    context.group().getMember(rpcMessage.getSourceNodeId()).getEndpoint())

        );
    }

    private RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {

        // reply current term if result's term is smaller than current one
        // 如果对方term比自己小，则不投票并且返回自己的term给对象
        RequestVoteRpc rpc = rpcMessage.get();
        if (rpc.getTerm() < role.getTerm()) {
            LOGGER.debug("term from rpc < current term, don't vote ({} <- {})",
                        rpc.getTerm(), role.getTerm());
            return new RequestVoteResult(role.getTerm(), false);
        }
        // 此处无条件投票

        boolean voteForCandidate = !context.log().isNewerThan(rpc.getLastLogIndex(), rpc.getLastLogTerm());

        // step down if result's term is larger than current term
        // 如果对象的term比自己大，则切换为follower角色
        if (rpc.getTerm() > role.getTerm()) {
            becomeFollower(rpc.getTerm(), (voteForCandidate ? rpc.getCandidateId() : null), null, true);
            return new RequestVoteResult(rpc.getTerm(), voteForCandidate);
        }

        // 本地的term与消息的term一致
        switch (role.getName()) {
            case FOLLOWER:
                FollowerNodeRole follower = (FollowerNodeRole) role;
                NodeId votedFor = follower.getVotedFor();
                // reply vote granted for
                // 1. not voted and candidate's log is newer than self
                // 2. voted for candidate
                if ((votedFor == null && voteForCandidate ||
                        Objects.equals(votedFor, rpc.getCandidateId()))) {
                    becomeFollower(role.getTerm(), rpc.getCandidateId(), null, true);
                    return new RequestVoteResult(rpc.getTerm(), true);
                }
                return new RequestVoteResult(role.getTerm(), false);
            case CANDIDATE: // voted for self
            case LEADER:
                return new RequestVoteResult(role.getTerm(), false);
            default:
                throw new IllegalStateException("unexpected node role [" + role.getName() + "]");
        }
    }


    @Subscribe
    public void onReceiveRequestVoteResult(RequestVoteResult result) {
        context.taskExecutor().submit(() -> doProcessRequestVoteResult(result));
    }

    private void doProcessRequestVoteResult(RequestVoteResult result) {

        // step down if result's term is larger than current term
        // 如果对象的term比自己大，则退化为follower角色
        if (result.getTerm() > role.getTerm()) {
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }

        // check role
        // 如果自己不是Candidate角色，那么忽略
        if (role.getName() != RoleName.CANDIDATE) {
            LOGGER.debug("reveive request vote result and current role is not candidate, ignore");
            return;
        }

        // 如果对方的term比自己小或者对方没有投票给自己，则忽略
        if (result.getTerm() < role.getTerm() || !result.isVoteGranted()) {
            return;
        }

        // 当前票数
        int currentVotesCount = ((CandidateNodeRole) role).getVotesCount() + 1;
        // 节点数
        int countOfMajor = context.group().getCount();
        LOGGER.debug("votes count {}, node count", currentVotesCount, countOfMajor);
        // 取消选举超时定时器
        role.cancelTimeoutOrTask();

        if (currentVotesCount > countOfMajor / 2) {  // 票数过半
            // become leader
            LOGGER.debug("become leader, term {}", role.getTerm());
            changeToRole(new LeaderNodeRole(role.getTerm(), scheduleLogReplicationTask()));


        } else {

            // update votes count
            // 票数没有达到一半
            // 收到修改的投票数，并重新创建选举超时定时器
            changeToRole(new CandidateNodeRole(role.getTerm(), currentVotesCount, scheduleElectionTimeout()));
        }
    }

    /**
     * 发送心跳或者appendEntries
     */
    void replicateLog() {

        //context.taskExecutor().submit(this::doReplicateLog);
        doReplicateLog();
    }

    private void doReplicateLog() {
        LOGGER.debug("replicate log");
        for (GroupMember member : context.group().listReplicationTarget()) {
            // todo: can be wrong
            doReplicateLog(member, Log.ALL_ENTRIES);
            //doReplicateLog(member, context.config().getMaxReplicationEntries());
        }
    }

    private void doReplicateLog(GroupMember member, int maxEntries) {
        // AppendEntriesRpc rpc = new AppendEntriesRpc();
        // set appendEntries attributes
        AppendEntriesRpc rpc = context.log().createAppendEntriesRpc(role.getTerm(), context.selfId(), member.getNextIndex(), maxEntries);

        context.rpcAdapter().sendAppendEntries(rpc, member.getEndpoint());
    }


    @Subscribe
    public void onReceiveAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        context.taskExecutor().submit(() ->

                        context.rpcAdapter().replyAppendEntries(doProcessAppendEntriesRpc(rpcMessage),
                                // 发送消息的节点
                        context.group().getMember(rpcMessage.getSourceNodeId()).getEndpoint()));

    }

    private AppendEntriesResult doProcessAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        AppendEntriesRpc rpc = rpcMessage.get();

        // reply current term if term in rpc is smaller than current term
        // 如果对方term比自己小，则回复自己的term
        if (rpc.getTerm() < role.getTerm()) {
            return new AppendEntriesResult(role.getTerm(), false);
        }

        // if term in rpc is larger than current term, step down and append entries
        // 如果对方的term比自己大，则退化为Follower角色
        if (rpc.getTerm() > role.getTerm()) {
            becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
            return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
        }

        assert rpc.getTerm() == role.getTerm();
        
        switch (role.getName()) {
            case FOLLOWER:
                // reset election timeout and append entries
                // 设置leaderId并重置选举定时器
                becomeFollower(rpc.getTerm(), ((FollowerNodeRole) role).getVotedFor(), rpc.getLeaderId(), true);
                // 并且追加日志

                return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));

            case CANDIDATE:

                // more than one candidate but another node won the election
                becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
                return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
            case LEADER:
                // Leader 收到AppendEntries消息，打印警告日志
                LOGGER.debug("receive append entries rpc from another leader {}, ignore", rpc.getLeaderId());

                return new AppendEntriesResult(rpc.getTerm(), false);

            default:
                throw new IllegalStateException("unexpected node role [" + role.getName() + "]");
        }
    }


    // TODO:
    private boolean appendEntries(AppendEntriesRpc rpc) {
        boolean result = context.log().appendEntriesFromLeader(rpc.getPrevLogIndex(),
        rpc.getPrevLogTerm(), rpc.getEntries());

        if (result) {
            context.log().advanceCommitIndex(
                    Math.min(rpc.getLeaderCommit(), rpc.getLastEntryIndex()),  rpc.getTerm());
        }
        return result;
    }


    @Subscribe
    public void onReceiveAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        context.taskExecutor().submit(() -> doProcessAppendEntriesResult(resultMessage));
    }


    private void doProcessAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        AppendEntriesResult result = resultMessage.get();

        // step down if result's term is larger than current term
        // 如果对方term比自己大，则退化为Follower角色
        if (result.getTerm() > role.getTerm()) {
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }

        // check role
        if (role.getName() != RoleName.LEADER) {
            LOGGER.debug("reveive append entries result from node {} but current node is not leader, ignore", resultMessage.getSourceNodeId());
            return;
        }

        NodeId sourceNodeId = resultMessage.getSourceNodeId();
        GroupMember member = context.group().getMember(sourceNodeId);
        // 没有指定的成员
        if (member == null) {
            LOGGER.debug("unexpected append entries result from node {}, node maybe removed", sourceNodeId);
            return;
        }
        AppendEntriesRpc rpc = resultMessage.getRpc();
        if (result.isSuccess()) {
            // 回复成功
            // 推进matchIndex和nextIndex
            if (member.advanceReplicatingState(rpc.getLastEntryIndex())) {
                // 推进本地的commitIndex
                context.log().advanceCommitIndex(
                        context.group().getMatchIndexOfMajor(), role.getTerm());

            } else {
                // 对方回复失败
                if (!member.backoffNextIndex()) {
                    LOGGER.debug("cannot back off next index more, node {}", sourceNodeId);
                }
            }
        }
    }

    @Override
    public void stop() throws InterruptedException {
        // 不允许没有启动时关闭
        if (!started) {
            throw new IllegalStateException("node not started");
        }
        // 关闭定时器
        context.scheduler().stop();
        // 关闭RPC连接器
        context.rpcAdapter().close();
        //
        context.store().close();
        // 关闭任务执行器
        context.taskExecutor().shutdown();
        started = false;
    }

}
