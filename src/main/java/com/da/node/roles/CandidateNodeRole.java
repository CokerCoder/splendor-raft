package com.da.node.roles;

import com.da.scheduler.ElectionTimeoutTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CandidateNodeRole extends AbstractNodeRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(CandidateNodeRole.class);

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
        LOGGER.debug("Candidate election timeout task cancelled.");
        electionTimeout.cancel();   
    }


    @Override
    public String toString() {
        return "CandidateNodeRole{" +
                "term=" + term +
                ", votesCount=" + votesCount +
                ", electionTimeout=" + electionTimeout +
                '}';
    }
    
}
