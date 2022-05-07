package com.da.entity;

public class RequestVoteResult {
    
    private final int term; // 选举term
    private final boolean voteGranted; // 是否投票

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

    @Override
    public String toString() {
        return "RequestVoteResult{" + "term=" + term +
                ", voteGranted=" + voteGranted +
                '}';
    }

}
