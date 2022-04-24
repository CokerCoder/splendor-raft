package com.da;

import com.da.entity.AppendEntriesResult;
import com.da.entity.AppendEntriesRequest;
import com.da.entity.RequestVoteResult;
import com.da.entity.RequestVoteRequest;

/**
 * Base consensus interface RAFT needs to implement
 */
public interface Consensus {


    RequestVoteResult requestVote(RequestVoteRequest request);

    AppendEntriesResult appendEntries(AppendEntriesRequest request);
    
}
