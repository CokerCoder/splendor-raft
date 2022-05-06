package com.da.client;

import com.da.node.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Store the server and their corresponding addresses.
 */
public class ServerRouter {

    private static final Logger logger = LoggerFactory.getLogger(ServerRouter.class);
    private final Map<NodeId, SocketChannel> availableServers = new HashMap<>();
    private NodeId leaderId;

    public Object send(Object payload) {
        for (NodeId nodeId : getCandidateNodeIds()) {
            try {
                Object result = doSend(nodeId, payload);
                this.leaderId = nodeId;
                return result;
            } catch (RedirectException e) { // TODO redirect
                logger.debug("not a leader server, redirect to server ()", e.getLeaderId());
                this.leaderId = e.getLeaderId();
                return doSend(e.getLeaderId(), payload);
            } catch (Exception e) {
                logger.debug("failed to process whith server " + nodeId + ", cause " + e.getMessage());
            }
        }
        throw new NoAvailableServerException("no available server");
    }

    public void add(NodeId nodeId, SocketChannel channel) {
        availableServers.put(nodeId, channel);
    }

    private Collection<NodeId> getCandidateNodeIds() {
        if (availableServers.isEmpty()) {
            throw new NoAvailableServerException("no available server");
        }

        if (leaderId != null) {
            List<NodeId> nodeIds = new ArrayList<>();
            nodeIds.add(leaderId);
            for (NodeId nodeId : availableServers.keySet()) {
                if (!nodeId.equals(leaderId)) {
                    nodeIds.add(nodeId);
                }
            }
            return nodeIds;
        }
        return availableServers.keySet();
    }

    // TODO get redirect info
    private Object doSend(NodeId id, Object payload) {
        return availableServers.get(id).send(payload);
    }
}
