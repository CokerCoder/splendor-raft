package com.da.node;

import java.util.Objects;

import com.da.entity.AppendEntriesResult;
import com.da.entity.AppendEntriesRpc;
import com.da.entity.RequestVoteResult;
import com.da.entity.RequestVoteRpc;
import com.da.node.nodestatic.GroupMember;
import com.da.node.roles.AbstractNodeRole;
import com.da.node.roles.CandidateNodeRole;
import com.da.node.roles.FollowerNodeRole;
import com.da.node.roles.LeaderNodeRole;
import com.da.node.roles.RoleName;
import com.da.scheduler.ElectionTimeoutTask;
import com.google.common.eventbus.Subscribe;


public class RaftNode implements Node {

    private final NodeContext context;
    private boolean started;
    private volatile AbstractNodeRole role;

    RaftNode(NodeContext context) {
        this.context = context;
    }

    NodeContext getContext() {
        return context;
    }

    @Override
    public synchronized void start() {
        if (started) {
            return;
        }
        context.eventBus().register(this);
        context.rpcAdapter().initialize();

        // load term, votedFor from store and become follower
        NodeStore store = context.store();
        changeToRole(new FollowerNodeRole(store.getTerm(), store.getVotedFor(), null, scheduleElectionTimeout()));
        started = true;
    }

    
    void electionTimeout() {
        context.taskExecutor().submit(this::doProcessElectionTimeout);
    }

    private void doProcessElectionTimeout() {
        if (role.getName() == RoleName.LEADER) {
            return;
        }

        // follower: start election
        // candidate: restart election
        int newTerm = role.getTerm() + 1;
        role.cancelTimeoutOrTask();
        changeToRole(new CandidateNodeRole(newTerm, scheduleElectionTimeout()));

        // TODO: 发送RequestVoteRpc消息

    }

    private void becomeFollower(int term, NodeId votedFor, NodeId leaderId, boolean scheduleElectionTimeout) {
        role.cancelTimeoutOrTask();
        // if (leaderId != null && !leaderId.equals(role.getLeaderId(context.selfId()))) {
        //     logger.info("current leader is {}, term {}", leaderId, term);
        // }
        ElectionTimeoutTask electionTimeout = scheduleElectionTimeout();
        changeToRole(new FollowerNodeRole(term, votedFor, leaderId, electionTimeout));
    }

    private void changeToRole(AbstractNodeRole newRole) {
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

    // private LogReplicationTask scheduleLogReplicationTask() {
    //     return context.scheduler().scheduleLogReplicationTask(this::replicateLog);
    // 

    @Subscribe
    public void onReceiveRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {
        context.taskExecutor().submit(
                () -> context.rpcAdapter().replyRequestVote(
                    doProcessRequestVoteRpc(rpcMessage),
                    context.findMember(rpcMessage.getSourceNodeId()).getEndPoint())
        );
    }

    private RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpcMessage rpcMessage) {

        // reply current term if result's term is smaller than current one
        RequestVoteRpc rpc = rpcMessage.get();
        if (rpc.getTerm() < role.getTerm()) {
            return new RequestVoteResult(role.getTerm(), false);
        }

        boolean voteForCandidate = true;

        // step down if result's term is larger than current term
        if (rpc.getTerm() > role.getTerm()) {
            becomeFollower(rpc.getTerm(), (voteForCandidate ? rpc.getCandidateId() : null), null, true);
            return new RequestVoteResult(rpc.getTerm(), voteForCandidate);
        }

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
        if (result.getTerm() > role.getTerm()) {
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }

        // check role
        if (role.getName() != RoleName.CANDIDATE) {
            return;
        }

        if (result.getTerm() < role.getTerm() || !result.isVoteGranted()) {
            return;
        }

        int currentVotesCount = ((CandidateNodeRole) role).getVotesCount() + 1;
        int countOfMajor = context.group().getCount();
        role.cancelTimeoutOrTask();

        if (currentVotesCount > countOfMajor / 2) {
            // become leader
            changeToRole(new LeaderNodeRole(role.getTerm(), scheduleLogReplicationTask()));
            context.rpcAdapter().resetChannels(); // close all inbound channels
        } else {

            // update votes count
            changeToRole(new CandidateNodeRole(role.getTerm(), currentVotesCount, scheduleElectionTimeout()));
        }
    }


    void replicateLog() {
        context.taskExecutor().submit(this::doReplicateLog);
    }

    private void doReplicateLog() {
        for (GroupMember member : context.group().listReplicationTarget()) {
            doReplicateLog(member);
        }
    }

    private void doReplicateLog(GroupMember member) {
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        // set appendEntries attributes
        // TODO:

        context.rpcAdapter().sendAppendEntries(rpc, member.getEndpoint());
    }


    @Subscribe
    public void onReceiveAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        context.taskExecutor().submit(() ->
                        context.rpcAdapter().replyAppendEntries(doProcessAppendEntriesRpc(rpcMessage), 
                        context.findMember(rpcMessage.getSourceNodeId()).getEndPoint()));
    }

    private AppendEntriesResult doProcessAppendEntriesRpc(AppendEntriesRpcMessage rpcMessage) {
        AppendEntriesRpc rpc = rpcMessage.get();

        // reply current term if term in rpc is smaller than current term
        if (rpc.getTerm() < role.getTerm()) {
            return new AppendEntriesResult(role.getTerm(), false);
        }

        // if term in rpc is larger than current term, step down and append entries
        if (rpc.getTerm() > role.getTerm()) {
            becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
            return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
        }

        assert rpc.getTerm() == role.getTerm();
        
        switch (role.getName()) {
            case FOLLOWER:
                // reset election timeout and append entries
                becomeFollower(rpc.getTerm(), ((FollowerNodeRole) role).getVotedFor(), rpc.getLeaderId(), true);
                return new AppendEntriesResult(rpc.getMessageId(), rpc.getTerm(), appendEntries(rpc));
            case CANDIDATE:

                // more than one candidate but another node won the election
                becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
                return new AppendEntriesResult(rpc.getMessageId(), rpc.getTerm(), appendEntries(rpc));
            case LEADER:
                return new AppendEntriesResult(rpc.getMessageId(), rpc.getTerm(), false);
            default:
                throw new IllegalStateException("unexpected node role [" + role.getName() + "]");
        }
    }


    // TODO:
    private boolean appendEntries(AppendEntriesRpc rpc) {
        return true;
    }


    /**
     * Receive append entries result.
     *
     * @param resultMessage result message
     */
    @Subscribe
    public void onReceiveAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        context.taskExecutor().submit(() -> doProcessAppendEntriesResult(resultMessage));
    }


    private void doProcessAppendEntriesResult(AppendEntriesResultMessage resultMessage) {
        AppendEntriesResult result = resultMessage.get();

        // step down if result's term is larger than current term
        if (result.getTerm() > role.getTerm()) {
            becomeFollower(result.getTerm(), null, null, true);
            return;
        }

        // check role
        if (role.getName() != RoleName.LEADER) {
            return;
        }
    }

    @Override
    public void stop() throws InterruptedException {
        if (!started) {
            throw new IllegalStateException("node not started");
        }
        context.scheduler().stop();
        context.rpcAdapter().close();
        context.store().close();
        context.taskExecutor().shutdown();
        started = false;
    }

}
