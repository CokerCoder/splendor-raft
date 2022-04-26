package com.da.raft;

import com.da.entity.AppendEntriesResult;
import com.da.entity.AppendEntriesRpc;
import com.da.entity.RequestVoteResult;
import com.da.entity.RequestVoteRpc;

/**
 * Base consensus interface RAFT needs to implement
 * Raft 算法核心机制接口
 */
public interface Consensus {

    RequestVoteResult requestVote(RequestVoteRpc request);

    AppendEntriesResult appendEntries(AppendEntriesRpc request);
    
}
