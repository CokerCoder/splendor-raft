package com.da.node;

import com.da.LifeCycle;
import com.da.entity.AppendEntriesRpc;
import com.da.entity.AppendEntriesResult;
import com.da.entity.RequestVoteRpc;
import com.da.entity.RequestVoteResult;

/**
 * 节点接口
 */
public interface Node extends LifeCycle {
    
    // 处理请求投票rpc请求
    RequestVoteResult handleRequestVote(RequestVoteRpc request);

    // 处理附加日志rpc请求
    AppendEntriesResult handleAppendEntries(AppendEntriesRpc request);

    // 客户端请求
    // ...
}
