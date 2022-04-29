package com.da.rpc;

import com.da.entity.*;
import com.da.node.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A main toolkit of RPC for a node. All RPCs should be processed via this class.
 * 构造函数传入的node类型待定
 */
public class RPCService implements RPCAdapter{

    private RPCServer server;
//    private Map<Node, RPCClient> remotes;
    private Node node; // TODO may change

    public RPCService(Node node) {
        this.node = node;
//        remotes = new HashMap<>();
    }

    @Override
    public void listen(int port) {
        server = new RPCServer(port, node);
        try {
            server.start();
        } catch (IOException e) {
            // TODO
        }
    }

    @Override
    public RequestVoteResult requestVoteRPC(RequestVoteRpc request, Node destination) {
//        if (!remotes.containsKey(xxx)) { // TODO
//            RPCClient client = new RPCClient("xx.x.xx.xx:xx");
//            remotes.put(xxx, client);
//        }
//        RequestVoteResult result = remotes.get(xxx).requestVoteRPC(request);
        return null;
    }


    @Override
    public AppendEntriesResult appendEntriesRPC(AppendEntriesRpc request, Node destination) {
//        if (!remotes.containsKey(xxx)) { // TODO
//            ...
//        }
//        AppendEntriesResult result = remotes.get(xxx).appendEntriesRPC(request);
        return null;
    }

    @Override
    public void close() {
//        for (RPCClient client : remotes.values()) { // TODO
//            client.close();
//        }
        server.close();
    }
}
