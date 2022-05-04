package com.da.RequestVoteTest;

import java.util.Arrays;

import com.da.executor.DirectTaskExecutor;
import com.da.node.NodeBuilder;
import com.da.node.NodeId;
import com.da.node.RaftNode;
import com.da.node.nodestatic.NodeEndpoint;
import com.da.node.roles.LeaderNodeRole;
import com.da.rpc.RPCService;
import com.da.scheduler.NullScheduler;

import org.junit.Assert;
import org.junit.Test;

public class RequestVoteTestGrpc {

    private NodeBuilder newNodeBuilder(NodeId selfId, NodeEndpoint... endpoints) {
        return new NodeBuilder(Arrays.asList(endpoints), selfId)
                .setScheduler(new NullScheduler())
                .setTaskExecutor(new DirectTaskExecutor());
    }

    /**
     * Test node 1 election timeout using real rpc client
     */
    @Test
    public void testElectionTimeoutAsFollowerGrpc() {
        RaftNode node1 = (RaftNode) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        RaftNode node2 = (RaftNode) newNodeBuilder(
                NodeId.of("B"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        RaftNode node3 = (RaftNode) newNodeBuilder(
                NodeId.of("C"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node1.getContext().setRPCAdapter(new RPCService(node1));
        node2.getContext().setRPCAdapter(new RPCService(node2));
        node3.getContext().setRPCAdapter(new RPCService(node3));

        node1.start();
        node2.start();
        node3.start();

        node1.electionTimeout(); // 由于定时器是mock的，此处显性调用electionTimeout
        // 收到B和C的同意选举后，切换为Leader角色
        Assert.assertTrue(node1.getRole() instanceof LeaderNodeRole);

        LeaderNodeRole role = (LeaderNodeRole) node1.getRole();
        Assert.assertEquals(1, role.getTerm());
    }
    
}
