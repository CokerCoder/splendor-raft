package com.da.raft;

import com.da.entity.AppendEntriesRequest;
import com.da.entity.AppendEntriesResult;
import com.da.entity.RequestVoteRequest;
import com.da.entity.RequestVoteResult;
import com.da.node.Node;

/**
 * Raft 核心逻辑
 */
public class RaftConsensus implements Consensus {

    private Node node;

    public RaftConsensus(Node node) {
        this.node = node;
    }

    @Override
    public RequestVoteResult requestVote(RequestVoteRequest request) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public AppendEntriesResult appendEntries(AppendEntriesRequest request) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
