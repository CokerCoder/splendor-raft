package com.da.node.roles;

public class FollowerNodeRole extends AbstractNodeRole {

    private final int votedFor;
    private final int leaderId;

    public FollowerNodeRole(int term, int votedFor, int leaderId) {
        super(RoleName.FOLLOWER, term);
        this.votedFor = votedFor;
        this.leaderId = leaderId;
    }

    public int getVotedFor() {
        return votedFor;
    }

    public int getLeaderId() {
        return leaderId;
    }

    @Override
    public int getLeaderId(int selfId) {
        return leaderId;
    }
    
}
