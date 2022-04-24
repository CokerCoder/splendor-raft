package com.da.rpc;

import java.util.Collection;

import com.da.entity.AppendEntriesRequest;
import com.da.entity.AppendEntriesResult;
import com.da.entity.RequestVoteRequest;
import com.da.entity.RequestVoteResult;
import com.da.node.Node;

// 最终RPC服务转换成的适配器接口，RPC service暂时没有实现
// 该类相当于lu-raft里的RpcClient，但是把可以发送的request拆分成了具体的（下面四个）
public interface RPCAdapter {
    
    void initialize();

    // 这里的Node并不是地址类，需要新增Node地址类
    void sendRequestVote(RequestVoteRequest request, Collection<Node> destinations);

    void replyRequestVote(RequestVoteResult result, Node destination);

    void sendAppendEntries(AppendEntriesRequest request, Collection<Node> destinations);

    void replyAppendEntries(AppendEntriesResult result, Node destination);

    void close();
    
}
