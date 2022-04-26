package com.da.node.roles;

import com.da.node.NodeId;

public class FollowerNodeRole extends AbstractNodeRole {

    private final NodeId votedFor;
    private final NodeId leaderId;

    public FollowerNodeRole(int term, NodeId votedFor, NodeId leaderId) {
        super(RoleName.FOLLOWER, term);
        this.votedFor = votedFor;
        this.leaderId = leaderId;
    }

    public NodeId getVotedFor() {
        return votedFor;
    }

    public NodeId getLeaderId() {
        return leaderId;
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return leaderId;
    }
    
}
