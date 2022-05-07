package com.da.node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.da.entity.AppendEntriesResult;
import com.da.entity.AppendEntriesRpc;
import com.da.entity.RequestVoteResult;
import com.da.entity.RequestVoteRpc;
import com.da.kv.server.KVService;
import com.da.log.StateMachine;
import com.da.log.EntryMeta;
import com.da.log.Log;
import com.da.node.nodestatic.GroupMember;
import com.da.node.nodestatic.NodeEndpoint;
import com.da.node.roles.AbstractNodeRole;
import com.da.node.roles.CandidateNodeRole;
import com.da.node.roles.FollowerNodeRole;
import com.da.node.roles.LeaderNodeRole;
import com.da.node.roles.RoleName;
import com.da.scheduler.ElectionTimeoutTask;
import com.da.scheduler.LogReplicationTask;


public class RaftNode implements Node {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RaftNode.class); // slf4j 日志
    
    private boolean started; //是否已经启动
    private final NodeContext context; // 核心上下文组件
    private volatile AbstractNodeRole role; // 当前的角色与信息
    private StateMachine stateMachine;

    RaftNode(NodeContext context) {
        this.context = context;
    }

    // 获取当前角色
    public AbstractNodeRole getRole() {
        return role;
    }

    // 获取核心组件上下文
    public NodeContext getContext() {
        return context;
    }

    @Override
    public synchronized void start() {

        // 如果已经启动则直接跳过
        if (started) {
            // logger.info("Node {} has already started, return.", this);
            return;
        }

        context.rpcAdapter().listen(context.group().getSelfEndpoint().getAddress().getPort());

        // load term, votedFor from store and become follower
        changeToRole(new FollowerNodeRole(0, null, null, scheduleElectionTimeout()));
        started = true;

    }

    
    public void electionTimeout() {
        LOGGER.debug("Node {} triggered election timeout", context.selfId());
        context.taskExecutor().submit(this::doProcessElectionTimeout);
        // blocking method 
        // doProcessElectionTimeout();;
    }

    private void doProcessElectionTimeout() {
        if (role.getName() == RoleName.LEADER) {
            LOGGER.debug("Current is leader, ignore election timeout", context.selfId());
            return;
        }
        // follower: start election
        // candidate: restart election
        // increase the term
        int newTerm = role.getTerm() + 1;
        role.cancelTimeoutOrTask();
        LOGGER.debug("Node {} start election, term {}", context.selfId(), newTerm);
        // 变成candidate 角色
        changeToRole(new CandidateNodeRole(newTerm, scheduleElectionTimeout()));

        EntryMeta lastEntryMeta = context.log().getLastEntryMeta();

        RequestVoteRpc rpc = new RequestVoteRpc();
        rpc.setTerm(newTerm);
        rpc.setCandidateId(context.selfId());
        rpc.setLastLogIndex(lastEntryMeta.getIndex());
        rpc.setLastLogTerm(lastEntryMeta.getTerm());

        // Blocking
        Set<NodeEndpoint> destinations = context.group().listEndPointExceptSelf();
        for (NodeEndpoint dest : destinations) {

            // send to all endpoints except self the requestVoteRpc using 
            // the single thread executor
            final Future<RequestVoteResult> future = context.taskExecutor().submit(
                () -> context.rpcAdapter().requestVoteRPC(rpc, dest));
            // try get the requestVoteRpcResult with a seperate runnable task
            context.taskExecutor().submit(
                () -> {
                    try {
                        onReceiveRequestVoteResult(future.get());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });

        }
    }

    private void becomeFollower(int term, NodeId votedFor, NodeId leaderId, boolean scheduleElectionTimeout) {
        role.cancelTimeoutOrTask(); // 取消超时或者定时器
        // if (leaderId != null && !leaderId.equals(role.getLeaderId(context.selfId()))) {
        //     logger.info("current leader is {}, term {}", leaderId, term);
        // }
        // 重新创建选举超时定时器
        ElectionTimeoutTask electionTimeout = scheduleElectionTimeout ? scheduleElectionTimeout() : ElectionTimeoutTask.NONE;
        changeToRole(new FollowerNodeRole(term, votedFor, leaderId, electionTimeout));
    }

    private void changeToRole(AbstractNodeRole newRole) {
        LOGGER.debug("Node {}, role state changed -> {}, current term: {}", context.selfId(), newRole, newRole.getTerm());
        role = newRole;
    }

    private ElectionTimeoutTask scheduleElectionTimeout() {
        return context.scheduler().scheduleElectionTimeoutTask(this::electionTimeout);
    }

    private LogReplicationTask scheduleLogReplicationTask() {
        return context.scheduler().scheduleLogReplicationTask(this::replicateLog);
    }


    public RequestVoteResult onReceiveRequestVoteRpc(RequestVoteRpc rpc) {
        LOGGER.debug("Node {} received {} from node {}", context.selfId(), rpc, rpc.getCandidateId());
        // return (RequestVoteResult) context.taskExecutor().submit(
        //     () -> doProcessRequestVoteRpc(rpc));
        return doProcessRequestVoteRpc(rpc);
    }

    private RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpc rpc) {

        if (rpc.getTerm() < role.getTerm()) {
            LOGGER.debug("Term from rpc < current term, don't vote ({} <- {})",
                        rpc.getTerm(), role.getTerm());
            return new RequestVoteResult(role.getTerm(), false);
        }

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


    public void onReceiveRequestVoteResult(RequestVoteResult result) {
        LOGGER.debug("Node {} received {}", context.selfId(), result);
        // context.taskExecutor().submit(() -> doProcessRequestVoteResult(result));
        doProcessRequestVoteResult(result);
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
            LOGGER.debug("Reveived request vote result and current role is not candidate, ignore");
            return;
        }

        // 如果对方的term比自己小或者对方没有投票给自己，则忽略
        if (result.getTerm() < role.getTerm() || !result.isVoteGranted()) {
            return;
        }

        // 当前票数
        int currentVotesCount = ((CandidateNodeRole) role).getVotesCount();
        currentVotesCount = result.isVoteGranted() ? currentVotesCount + 1 :currentVotesCount;

        // 节点数
        int countOfMajor = context.group().getCount();
        LOGGER.debug("Votes count {}, node count {}", currentVotesCount, countOfMajor);
        // 取消选举超时定时器
        role.cancelTimeoutOrTask();

        if (currentVotesCount > countOfMajor / 2) {  // 票数过半
            resetReplicatingStates();
            // become leader
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
    public void replicateLog() {

        // context.taskExecutor().submit(this::doReplicateLogAll);

        doReplicateLogAll();
    }

    private void doReplicateLogAll() {

        LOGGER.debug("Replication target group: {}", context.group().listReplicationTarget());

        for (GroupMember member : context.group().listReplicationTarget()) {
            // todo: can be wrong
            doReplicateLog(member, Log.ALL_ENTRIES);
            //doReplicateLog(member, context.config().getMaxReplicationEntries());
        }
    }

    private void doReplicateLog(GroupMember member, int maxEntries) {

        AppendEntriesRpc rpc = context.log().createAppendEntriesRpc(role.getTerm(), context.selfId(), member.getNextIndex(), maxEntries);

        // send to all endpoints except self the AppendEntriesRpc using 
        // the single thread executor
        final Future<AppendEntriesResult> future = context.taskExecutor().submit(
            () -> context.rpcAdapter().appendEntriesRPC(rpc, member.getEndpoint()));

        // try get the appendEntriesResult with a seperate runnable task
        context.taskExecutor().submit(
            () -> {
                try {
                    // with the associated rpc
                    onReceiveAppendEntriesResult(future.get(), rpc);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });

    }



    public AppendEntriesResult onReceiveAppendEntriesRpc(AppendEntriesRpc rpc) {
        LOGGER.debug("Node {} received {} from node {}", context.selfId(), rpc, rpc.getLeaderId());
        // context.taskExecutor().submit(() ->
        //                 context.rpcAdapter().replyAppendEntries(doProcessAppendEntriesRpc(rpcMessage), 
        
        //                 context.group().getMember(rpcMessage.getSourceNodeId()).getEndpoint()));
        return doProcessAppendEntriesRpc(rpc);
    }

    private AppendEntriesResult doProcessAppendEntriesRpc(AppendEntriesRpc rpc) {

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


    private boolean appendEntries(AppendEntriesRpc rpc) {
        boolean result = context.log().appendEntriesFromLeader(rpc.getPrevLogIndex(),
            rpc.getPrevLogTerm(), rpc.getEntries());

        if (result) {
            context.log().advanceCommitIndex(
                    Math.min(rpc.getLeaderCommit(), rpc.getLastEntryIndex()),  rpc.getTerm());
        }
        return result;
    }


    public void onReceiveAppendEntriesResult(AppendEntriesResult result, AppendEntriesRpc rpc) {
        LOGGER.debug("Node {} received {} from node {}", context.selfId(), result, rpc.getLeaderId());
        
        context.taskExecutor().submit(() -> doProcessAppendEntriesResult(result, rpc));
        // doProcessAppendEntriesResult(result);
    }


    private void doProcessAppendEntriesResult(AppendEntriesResult result, AppendEntriesRpc rpc) {

        if (result == null) {
            return;
        }

        // step down if result's term is larger than current term
        // 如果对方term比自己大，则退化为Follower角色
        if (result.getTerm() > role.getTerm()) {
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }

        // check role
        if (role.getName() != RoleName.LEADER) {
            LOGGER.debug("reveive append entries result from node {} but current node is not leader, ignore", rpc.getLeaderId());
            return;
        }

        NodeId sourceNodeId = rpc.getLeaderId();

        GroupMember member = context.group().getMember(sourceNodeId);
        // 没有指定的成员
        if (member == null) {
            LOGGER.debug("unexpected append entries result from node {}, node maybe removed", sourceNodeId);
            return;
        }

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
        // 关闭任务执行器
        context.taskExecutor().shutdown();
        started = false;
    }

    @Override
    public void registerStateMachine(KVService service) {
        this.stateMachine = service;
    }

    private void resetReplicatingStates() {
        context.group().resetReplicatingStates(context.log().getNextIndex());
    }

    public void appendLog(byte[] bytes) {
    }

}
