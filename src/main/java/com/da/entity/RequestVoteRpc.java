package com.da.entity;

import com.da.node.NodeId;

public class RequestVoteRpc {
    
    private int term;
    private NodeId candidateId;
    private int lastLongIndex; // 候选者最后一条日志索引
    private int lastLogTerm; // 候选者最后一条日志term

    // getter and setter
    public int getTerm() {
        return term;
    }

    public NodeId getCandidateId() {
        return candidateId;
    }
    
}
