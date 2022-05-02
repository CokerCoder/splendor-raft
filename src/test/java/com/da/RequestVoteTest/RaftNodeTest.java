package com.da.RequestVoteTest;

import java.util.Arrays;

import com.da.executor.SingleThreadTaskExecutor;
import com.da.node.NodeBuilder;
import com.da.node.NodeId;
import com.da.node.RaftNode;
import com.da.node.nodestatic.NodeEndpoint;
import com.da.node.roles.FollowerNodeRole;
import com.da.rpc.MockRPCAdapter;
import com.da.scheduler.NullScheduler;

import org.junit.Assert;
import org.junit.Test;

public class RaftNodeTest {
    
    private NodeBuilder newNodeBuilder(NodeId selfId, NodeEndpoint... endpoints) {
        return new NodeBuilder(Arrays.asList(endpoints), selfId)
                .setScheduler(new NullScheduler())
                .setConnector(new MockRPCAdapter())
                .setTaskExecutor(new SingleThreadTaskExecutor());
    }

    @Test
    public void testStart() {
        RaftNode node = (RaftNode) newNodeBuilder(NodeId.of("A"), new NodeEndpoint("A", "localhost", 2333))
                .build();

        node.start();

        FollowerNodeRole role = (FollowerNodeRole) node.getRole();
        
        Assert.assertEquals(0, role.getTerm());
        Assert.assertNull(role.getVotedFor());
    }

}
