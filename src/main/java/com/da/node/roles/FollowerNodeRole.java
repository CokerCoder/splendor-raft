package com.da.node.roles;

import com.da.node.NodeId;
import com.da.scheduler.ElectionTimeoutTask;

public class FollowerNodeRole extends AbstractNodeRole {

    private final NodeId votedFor;
    private final NodeId leaderId;

    private final ElectionTimeoutTask electionTimeout; // election timeout task

    public FollowerNodeRole(int term, NodeId votedFor, NodeId leaderId, ElectionTimeoutTask electionTimeout) {
        super(RoleName.FOLLOWER, term);
        this.votedFor = votedFor;
        this.leaderId = leaderId;
        this.electionTimeout = electionTimeout;
    }

    public NodeId getVotedFor() {
        return votedFor;
    }

    public NodeId getLeaderId() {
        return leaderId;
    }

    @Override
    public void cancelTimeoutOrTask() {
        electionTimeout.cancel();
    }

}
