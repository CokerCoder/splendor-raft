package com.da.node;

/**
 * 基于内存实现NodeStore接口
 */
public class MemoryNodeStore implements NodeStore {

    private int term;
    private NodeId votedFor;

    public MemoryNodeStore(int term, NodeId votedFor) {
        this.term = term;
        this.votedFor = votedFor;
    }

    @Override
    public int getTerm() {
        return term;
    }

    @Override
    public void setTerm(int term) {
        this.term = term;
    }

    @Override
    public NodeId getVotedFor() {
        return votedFor;
    }

    @Override
    public void setVotedFor(NodeId votedFor) {
        this.votedFor = votedFor;
    }

    @Override
    public void close() {
        
    }
    
}
