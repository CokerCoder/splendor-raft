package com.da.node.roles;

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
    public int getLeaderId(int selfId) {
        return -1; // no leader when as candidate
    }
    
}
