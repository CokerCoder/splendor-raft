package com.da.node.roles;

import com.da.scheduler.ElectionTimeoutTask;

public class CandidateNodeRole extends AbstractNodeRole {

    private final int votesCount;
    private final ElectionTimeoutTask electionTimeout;

    public CandidateNodeRole(int term, ElectionTimeoutTask electionTimeout) {
        this(term, 1, electionTimeout); // by default get one vote from self
    }

    public CandidateNodeRole(int term, int votesCount, ElectionTimeoutTask electionTimeout) {
        super(RoleName.CANDIDATE, term);
        this.votesCount = votesCount;
        this.electionTimeout = electionTimeout;
    }

    public int getVotesCount() {
        return votesCount;
    }

    @Override
    public void cancelTimeoutOrTask() {
        electionTimeout.cancel();   
    }
    
}
