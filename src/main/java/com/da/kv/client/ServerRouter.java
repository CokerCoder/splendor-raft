package com.da.kv.client;

import com.da.kv.messages.GetCommand;
import com.da.kv.messages.GetCommandResponse;
import com.da.kv.messages.SetCommand;
import com.da.kv.messages.SetCommandResponse;
import com.da.node.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Store the server and their corresponding addresses.
 */
public class ServerRouter {

    private static final Logger logger = LoggerFactory.getLogger(ServerRouter.class);
    private final Map<NodeId, RPCChannel> availableServers = new HashMap<>();
    private NodeId leaderId;

    public byte[] get(String key) {
        GetCommandResponse response = availableServers.get(leaderId).get(new GetCommand(key));
        return response.getValue();
    }

    public void set(String key, byte[] value) {
        SetCommandResponse response = availableServers.get(leaderId).set(new SetCommand(key, value));
        while (response.getLeaderId() != null) { // TODO
            // currently no leader, electing
            if (response.getLeaderId().isEmpty()) {
                System.out.println("Server temporarily unavailable, please try again.");
                return;
            }
            logger.debug("redirect to server {}", response.getLeaderId());
            leaderId = new NodeId(response.getLeaderId());
            RPCChannel leaderChannel = availableServers.get(leaderId);
            if (leaderChannel != null) {
                response = leaderChannel.set(new SetCommand(key, value));
            }
        }
        if (response.getErrorMessage() != null) { // TODO
            logger.error(response.getErrorMessage());
        }
    }

    public void add(NodeId nodeId, RPCChannel channel) {
        // Treat first node as leader if not specify the leader
        if (leaderId == null) {
            leaderId = nodeId;
        }
        availableServers.put(nodeId, channel);
    }

    public void close() {
        for (RPCChannel channel : availableServers.values()) {
            channel.close();
        }
    }
}
