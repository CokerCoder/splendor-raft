package com.da.rpc;

import com.da.entity.*;
import com.da.node.Node;


/**
 * The definition of the PRC service instance
 */
public interface RPCAdapter {

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
