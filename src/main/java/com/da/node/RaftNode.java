package com.da.node;

import java.util.Objects;
import java.util.Set;

import com.da.entity.AppendEntriesResult;
import com.da.entity.AppendEntriesRpc;
import com.da.entity.RequestVoteResult;
import com.da.entity.RequestVoteRpc;
import com.da.node.nodestatic.GroupMember;
import com.da.node.nodestatic.NodeEndpoint;
import com.da.node.roles.AbstractNodeRole;
import com.da.node.roles.CandidateNodeRole;
import com.da.node.roles.FollowerNodeRole;
import com.da.node.roles.LeaderNodeRole;
import com.da.node.roles.RoleName;
import com.da.scheduler.ElectionTimeoutTask;
import com.da.scheduler.LogReplicationTask;
import com.google.common.eventbus.Subscribe;


public class RaftNode implements Node {

    private static final int RPC_PORT = 3333;

    private final NodeContext context;
    private boolean started;
    private volatile AbstractNodeRole role;

    public AbstractNodeRole getRole() {
        return role;
    }

    RaftNode(NodeContext context) {
        this.context = context;
    }

    public NodeContext getContext() {
        return context;
    }

    @Override
    public synchronized void start() {
        if (started) {
            return;
        }
        context.eventBus().register(this);
        context.rpcAdapter().listen(RPC_PORT);

        // load term, votedFor from store and become follower
        NodeStore store = context.store();
        changeToRole(new FollowerNodeRole(store.getTerm(), store.getVotedFor(), null, scheduleElectionTimeout()));
        started = true;
    }

    
    public void electionTimeout() {
        // context.taskExecutor().submit(this::doProcessElectionTimeout);
        doProcessElectionTimeout();;
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

        RequestVoteRpc rpc = new RequestVoteRpc();
        rpc.setTerm(newTerm);
        rpc.setCandidateId(context.selfId());
        rpc.setLastLogIndex(0);
        rpc.setLastLogTerm(0);
        // context.rpcAdapter().sendRequestVote(rpc, context.group().listEndPointExceptSelf());

        // Blocking
        Set<NodeEndpoint> destinations = context.group().listEndPointExceptSelf();
        for (NodeEndpoint dest : destinations) {
            RequestVoteResult result = context.rpcAdapter().
                requestVoteRPC(rpc, dest);
            // receive and parse the results from destinations
            onReceiveRequestVoteResult(result);
        }

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

    private LogReplicationTask scheduleLogReplicationTask() {
        return context.scheduler().scheduleLogReplicationTask(this::replicateLog);
    }


    public RequestVoteResult onReceiveRequestVoteRpc(RequestVoteRpc rpc) {
        return doProcessRequestVoteRpc(rpc);
    }

    private RequestVoteResult doProcessRequestVoteRpc(RequestVoteRpc rpc) {

        // reply current term if result's term is smaller than current one
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


    public void onReceiveRequestVoteResult(RequestVoteResult result) {
        // context.taskExecutor().submit(() -> doProcessRequestVoteResult(result));
        doProcessRequestVoteResult(result);
    }

    private void doProcessRequestVoteResult(RequestVoteResult result) {

        if (result == null) {
            return;
        }

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


        } else {

            // update votes count
            changeToRole(new CandidateNodeRole(role.getTerm(), currentVotesCount, scheduleElectionTimeout()));
        }
    }


    public void replicateLog() {
        // context.taskExecutor().submit(this::doReplicateLog);
        doReplicateLog();
    }

    private void doReplicateLog() {
        for (GroupMember member : context.group().listReplicationTarget()) {
            doReplicateLog(member);
        }
    }

    private void doReplicateLog(GroupMember member) {
        AppendEntriesRpc rpc = new AppendEntriesRpc();
        // set appendEntries attributes
        rpc.setTerm(role.getTerm());
        rpc.setLeaderId(context.selfId());
        rpc.setPrevLogIndex(0);
        rpc.setPrevLogTerm(0);
        rpc.setLeaderCommit(0);

        AppendEntriesResult result = 
            context.rpcAdapter().appendEntriesRPC(rpc, member.getEndpoint());
        onReceiveAppendEntriesResult(result);

    }


    public AppendEntriesResult onReceiveAppendEntriesRpc(AppendEntriesRpc rpc) {
        // context.taskExecutor().submit(() ->
        //                 context.rpcAdapter().replyAppendEntries(doProcessAppendEntriesRpc(rpcMessage), 
        
        //                 context.group().getMember(rpcMessage.getSourceNodeId()).getEndpoint()));
        return doProcessAppendEntriesRpc(rpc);
    }

    private AppendEntriesResult doProcessAppendEntriesRpc(AppendEntriesRpc rpc) {

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
                return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
            case CANDIDATE:

                // more than one candidate but another node won the election
                becomeFollower(rpc.getTerm(), null, rpc.getLeaderId(), true);
                return new AppendEntriesResult(rpc.getTerm(), appendEntries(rpc));
            case LEADER:
                return new AppendEntriesResult(rpc.getTerm(), false);
            default:
                throw new IllegalStateException("unexpected node role [" + role.getName() + "]");
        }
    }


    // TODO:
    private boolean appendEntries(AppendEntriesRpc rpc) {
        return true;
    }


    @Subscribe
    public void onReceiveAppendEntriesResult(AppendEntriesResult result) {
        // context.taskExecutor().submit(() -> doProcessAppendEntriesResult(resultMessage));
        doProcessAppendEntriesResult(result);
    }


    private void doProcessAppendEntriesResult(AppendEntriesResult result) {

        if (result == null) {
            return;
        }

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
