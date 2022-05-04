package com.da.RequestVoteTest;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.da.entity.AppendEntriesResult;
import com.da.entity.AppendEntriesRpc;
import com.da.entity.RequestVoteResult;
import com.da.entity.RequestVoteRpc;
import com.da.executor.DirectTaskExecutor;
import com.da.node.NodeBuilder;
import com.da.node.NodeId;
import com.da.node.RaftNode;
import com.da.node.nodestatic.NodeEndpoint;
import com.da.node.roles.CandidateNodeRole;
import com.da.node.roles.FollowerNodeRole;
import com.da.node.roles.LeaderNodeRole;
import com.da.rpc.MockRPCAdapter;
import com.da.rpc.RPCService;
import com.da.scheduler.NullScheduler;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests using mock rpc adapter (no real messages being exchanged)
 */
public class RequestVoteTest {
    
    private NodeBuilder newNodeBuilder(NodeId selfId, NodeEndpoint... endpoints) {
        return new NodeBuilder(Arrays.asList(endpoints), selfId)
                .setScheduler(new NullScheduler())
                .setConnector(new MockRPCAdapter())
                .setTaskExecutor(new DirectTaskExecutor());
    }

    /**
     * Test fresh start of a single node
     * The node should be a follower role upon start, and term 0 and has not voted
     */
    @Test
    public void testStartNode() {
        RaftNode node = (RaftNode) newNodeBuilder(NodeId.of("A"), new NodeEndpoint("A", "localhost", 2333))
                .build();
        node.getContext().setRPCAdapter(new RPCService(node));
        node.start();

        // check if follower
        Assert.assertTrue(node.getRole() instanceof FollowerNodeRole);

        FollowerNodeRole role = (FollowerNodeRole) node.getRole();
        
        Assert.assertEquals(0, role.getTerm());
        Assert.assertNull(role.getVotedFor());
    }


    /**
     * Test election timeout task
     * When election timeout, follower node should send requestVote rpc to peers
     */
    @Test
    public void testElectionTimeoutAsFollower() {
        RaftNode node = (RaftNode) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();
        node.electionTimeout(); // 由于定时器是mock的，此处显性调用electionTimeout

        // should change to candidate role
        Assert.assertTrue(node.getRole() instanceof CandidateNodeRole);

        CandidateNodeRole role = (CandidateNodeRole) node.getRole();
        Assert.assertEquals(1, role.getTerm());
        Assert.assertEquals(1, role.getVotesCount());

        // check rpc data
        MockRPCAdapter mockRPCAdapter = (MockRPCAdapter) node.getContext().rpcAdapter();
        // should be two messages sent
        Assert.assertEquals(2, mockRPCAdapter.getMessageCount());

        RequestVoteRpc rpc = (RequestVoteRpc) mockRPCAdapter.getRpc();

        Assert.assertEquals(1, rpc.getTerm());
        Assert.assertEquals(NodeId.of("A"), rpc.getCandidateId());
        Assert.assertEquals(0, rpc.getLastLogIndex());
        Assert.assertEquals(0, rpc.getLastLogTerm());
    }


    /**
     * Test when receive requestVote request
     * As a follower, when receive the request, should response with vote
     */
    @Test
    public void testOnReceiveRequestVoteRpcAsFollower() {
        RaftNode node = (RaftNode) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();

        // 自己是Node A，当前角色为follower，收到来自C的requestVote请求
        RequestVoteRpc rpc = new RequestVoteRpc();
        rpc.setTerm(1);
        rpc.setCandidateId(NodeId.of("C"));
        rpc.setLastLogIndex(0);
        rpc.setLastLogTerm(0);

        // 处理来自C的请求
        RequestVoteResult result = node.onReceiveRequestVoteRpc(rpc);
        
        // 核对信息
        Assert.assertEquals(1, result.getTerm());
        Assert.assertTrue(result.isVoteGranted());
        Assert.assertEquals(NodeId.of("C"), ((FollowerNodeRole) node.getRole()).getVotedFor());
    }


    /**
     * Test successfully voted as the new leader
     */
    @Test
    public void testOnReceiveRequestVoteResultAsCandidate() {
        // 测试当Candidate节点A收到任意来自B或C的同意投票后，应当变成leader
        RaftNode node = (RaftNode) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();

        node.electionTimeout(); // 此时A变为Candidate

        // 收到拒绝投票不应该变成leader
        node.onReceiveRequestVoteResult(new RequestVoteResult(1, false));
        Assert.assertTrue(node.getRole() instanceof CandidateNodeRole);

        node.onReceiveRequestVoteResult(new RequestVoteResult(1, true));
        LeaderNodeRole newRole = (LeaderNodeRole) node.getRole();
        Assert.assertEquals(1, newRole.getTerm());
        Assert.assertTrue(node.getRole() instanceof LeaderNodeRole);
    }


    /**
     * Test when leader A becomes the leader, should send AppendEntriesRpc to all peers
     */
    @Test
    public void testReplicateLogAsLeader() {
        RaftNode node = (RaftNode) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();

        node.electionTimeout();
        // A becomes the leader
        node.onReceiveRequestVoteResult(new RequestVoteResult(1, true));

        // send replicate log messages
        node.replicateLog();

        MockRPCAdapter mockRPCAdapter = (MockRPCAdapter) node.getContext().rpcAdapter();

        // there should be a total of three messages sent (2 requestVote + 2 appendEntries)
        Assert.assertEquals(4, mockRPCAdapter.getMessageCount());

        List<MockRPCAdapter.Message> messages = mockRPCAdapter.getMessages();
        Set<NodeId> destinationNodeIds = messages.subList(2, 4).stream()
            .map(MockRPCAdapter.Message::getDestinationNodeId)
            .collect(Collectors.toSet());
        // check if received from both B and C
        Assert.assertEquals(2, destinationNodeIds.size());
        Assert.assertTrue(destinationNodeIds.contains(NodeId.of("B")));
        Assert.assertTrue(destinationNodeIds.contains(NodeId.of("C")));

        // check the appendEntriesRPC data
        AppendEntriesRpc rpc = (AppendEntriesRpc) messages.get(2).getRpc();
        Assert.assertEquals(1, rpc.getTerm());
    }



    /**
     * Test receive appendEntriesRpc when as follower
     * should set own term to the latest one and reply ok
     */
    @Test
    public void testOnReceiveAppendEntriesRpcAsFollower() {
        RaftNode node = (RaftNode) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();

        AppendEntriesRpc rpc = new AppendEntriesRpc();
        rpc.setTerm(1);
        rpc.setLeaderId(NodeId.of("B"));

        AppendEntriesResult result = node.onReceiveAppendEntriesRpc(rpc);

        Assert.assertEquals(1, result.getTerm());
        Assert.assertTrue(result.isSuccess());

        // should still be a follower
        Assert.assertTrue(node.getRole() instanceof FollowerNodeRole);

        FollowerNodeRole role = (FollowerNodeRole) node.getRole();
        Assert.assertEquals(1, role.getTerm());
        Assert.assertEquals(NodeId.of("B"), role.getLeaderId());
    }


    /**
     * Test as a leader receive ok messages from other nodes
     */
    @Test
    public void testOnReceiveAppendEntriesResult() {
        RaftNode node = (RaftNode) newNodeBuilder(
                NodeId.of("A"),
                new NodeEndpoint("A", "localhost", 2333),
                new NodeEndpoint("B", "localhost", 2334),
                new NodeEndpoint("C", "localhost", 2335)
        ).build();
        node.start();
        node.electionTimeout();
        node.onReceiveRequestVoteResult(new RequestVoteResult(1, true));

        // now A is the leader
        Assert.assertTrue(node.getRole() instanceof LeaderNodeRole);

        node.replicateLog();
        node.onReceiveAppendEntriesResult(new AppendEntriesResult(1, true));
    }
    

}
