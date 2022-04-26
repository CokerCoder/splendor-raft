package com.da.node.roles;

import com.da.node.NodeId;

public class CandidateNodeRole extends AbstractNodeRole {

    private final int votesCount;

    public CandidateNodeRole(int term, int votesCount) {
        super(RoleName.CANDIDATE, term);
        this.votesCount = votesCount;
    }

    public int getVotesCount() {
        return votesCount;
    }

    @Override
    public NodeId getLeaderId(NodeId selfId) {
        return null; // no leader when as candidate
    }
    
}
