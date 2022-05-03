package com.da.entity;

import com.da.node.NodeId;

public class RequestVoteRpc {
    
    private int term;
    private NodeId candidateId;
    private int lastLogIndex;
    private int lastLogTerm;

    // getter and setter
    public int getTerm() {
        return term;
    }

    public NodeId getCandidateId() {
        return candidateId;
    }

    public int getLastLogIndex() {
        return lastLogIndex;
    }

    public int getLastLogTerm() {
        return lastLogTerm;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public void setCandidateId(NodeId candidateId) {
        this.candidateId = candidateId;
    }

    public void setLastLogIndex(int lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public void setLastLogTerm(int lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }
    
}
