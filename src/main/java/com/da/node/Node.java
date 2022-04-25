package com.da.node;

import com.da.LifeCycle;
import com.da.entity.AppendEntriesRequest;
import com.da.entity.AppendEntriesResult;
import com.da.entity.RequestVoteRequest;
import com.da.entity.RequestVoteResult;

/**
 * 节点接口
 */
public interface Node extends LifeCycle {
    
    // 处理请求投票rpc请求
    RequestVoteResult handleRequestVote(RequestVoteRequest request);

    // 处理附加日志rpc请求
    AppendEntriesResult handleAppendEntries(AppendEntriesRequest request);

    // 客户端请求
    // ...
}
