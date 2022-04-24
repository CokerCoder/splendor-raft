package com.da.entity;

public class RequestVoteResult {
    
    private final int term;
    private final boolean voteGranted;

    public RequestVoteResult(int term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    // getter and setter
    public int getTerm() {
        return term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

}
