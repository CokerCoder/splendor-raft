package com.da.entity;

import com.da.node.NodeId;

public class RequestVoteRpc {
    
    private int term;
    private NodeId candidateId;
    private int lastLongIndex;
    private int lastLogTerm;

    // getter and setter
    public int getTerm() {
        return term;
    }

    public NodeId getCandidateId() {
        return candidateId;
    }
    
}
