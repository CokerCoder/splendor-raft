package com.da.rpc;

import com.da.entity.*;
import com.da.node.RaftNode;
import com.da.node.nodestatic.NodeEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A main toolkit of RPC for a node. All RPCs should be processed via this class.
 */
public class RPCService implements RPCAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPCService.class); 

    private RPCServer server;
    private final Map<NodeEndpoint, RPCClient> remotes;
    private final RaftNode node;

    public RPCService(RaftNode node) {
        this.node = node;
        remotes = new HashMap<>();
    }

    @Override
    public void listen(int port) {
        server = new RPCServer(port, node);
        try {
            server.start();
        } catch (IOException ignored) {}
    }

    @Override
    public RequestVoteResult requestVoteRPC(RequestVoteRpc request, NodeEndpoint destination) {
        LOGGER.debug("Sending {} to node {}", request, destination);
        if (!remotes.containsKey(destination)) {
            RPCClient client = new RPCClient(destination.getAddress().toString());
            remotes.put(destination, client);
        }
        return remotes.get(destination).requestVoteRPC(request);
    }


    @Override
    public AppendEntriesResult appendEntriesRPC(AppendEntriesRpc request, NodeEndpoint destination) {
        LOGGER.debug("Sending {} to node {}", request, destination);
        if (!remotes.containsKey(destination)) {
            RPCClient client = new RPCClient(destination.getAddress().toString());
            remotes.put(destination, client);
        }
        return remotes.get(destination).appendEntriesRPC(request);
    }

    @Override
    public void close() {
        for (RPCClient client : remotes.values()) {
            client.close();
        }
        server.close();
    }

}
