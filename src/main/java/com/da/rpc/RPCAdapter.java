package com.da.rpc;

import java.util.Collection;

import com.da.entity.*;
import com.da.node.Node;
import com.da.node.nodestatic.NodeEndpoint;



// 最终RPC服务转换成的适配器接口，RPC service暂时没有实现
// 该类相当于lu-raft里的RpcClient，但是把可以发送的request拆分成了具体的（下面四个）
public interface RPCAdapter {
    
    void initialize();

    void sendRequestVote(RequestVoteRpc request, Collection<NodeEndpoint> destinations);

    void replyRequestVote(RequestVoteResult result, NodeEndpoint destination);

    void sendAppendEntries(AppendEntriesRpc request, NodeEndpoint destinations);

    void replyAppendEntries(AppendEntriesResult result, NodeEndpoint destination);


    /**
     * Start a RPC server listening to port
     * @param port The port is monitored by the node
     */
    void listen(int port);

    /**
     * Send request vote message to the other node
     * @param request The request information
     * @param destination The target node
     * @return The rpc result
     *  destination 类型待定（需要address和port)
     *  RPC 需调用 Node的 XXXResult handleXXX(XXXRpc)
     */
    RequestVoteResult requestVoteRPC(RequestVoteRpc request, Node destination);

    /**
     * Send append entries message to the other node
     * @param request The request information
     * @param destination The target node
     * @return The rpc result
     */
    AppendEntriesResult appendEntriesRPC(AppendEntriesRpc request, Node destination);

    /**
     * Close all connection to other nodes and stop listening
     */
    void close();

}
