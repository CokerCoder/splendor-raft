package com.da.rpc;

import com.da.entity.*;
import com.da.node.RaftNode;
import com.da.node.nodestatic.NodeEndpoint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A main toolkit of RPC for a node. All RPCs should be processed via this class.
 * 构造函数传入的node类型待定
 */
public class RPCService implements RPCAdapter {

    private RPCServer server;
    private Map<NodeEndpoint, RPCClient> remotes;
    private RaftNode node;

    public RPCService(RaftNode node) {
        this.node = node;
        remotes = new HashMap<>();
    }

    @Override
    public void listen(int port) {
        server = new RPCServer(port, node);
        try {
            server.start();
        } catch (IOException e) {

        }
    }

    @Override
    public RequestVoteResult requestVoteRPC(RequestVoteRpc request, NodeEndpoint destination) {
        if (!remotes.containsKey(destination)) { 
            RPCClient client = new RPCClient(destination.getAddress().toString());
            remotes.put(destination, client);
        }
        RequestVoteResult result = remotes.get(destination).requestVoteRPC(request);
        return result;
    }


    @Override
    public AppendEntriesResult appendEntriesRPC(AppendEntriesRpc request, NodeEndpoint destination) {
        if (!remotes.containsKey(destination)) {
            RPCClient client = new RPCClient(destination.getAddress().toString());
            remotes.put(destination, client);
        }
        AppendEntriesResult result = remotes.get(destination).appendEntriesRPC(request);
        return result;
    }

    @Override
    public void close() {
        for (RPCClient client : remotes.values()) {
            client.close();
        }
        server.close();
    }

}
