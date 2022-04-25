package com.da.raft;

import com.da.entity.AppendEntriesResult;
import com.da.entity.AppendEntriesRequest;
import com.da.entity.RequestVoteResult;
import com.da.entity.RequestVoteRequest;

/**
 * Base consensus interface RAFT needs to implement
 * Raft 算法核心机制接口
 */
public interface Consensus {

    RequestVoteResult requestVote(RequestVoteRequest request);

    AppendEntriesResult appendEntries(AppendEntriesRequest request);
    
}
