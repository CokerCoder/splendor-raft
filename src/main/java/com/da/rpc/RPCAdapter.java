package com.da.rpc;

import com.da.entity.*;
import com.da.node.nodestatic.NodeEndpoint;

public interface RPCAdapter {

    // using blocking calls

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
     */
    RequestVoteResult requestVoteRPC(RequestVoteRpc request, NodeEndpoint destination);

    /**
     * Send append entries message to the other node
     * @param request The request information
     * @param destination The target node
     * @return The rpc result
     */
    AppendEntriesResult appendEntriesRPC(AppendEntriesRpc request, NodeEndpoint destination);

    /**
    * Close all connections to other nodes and stop listening
    */
    void close();

}
