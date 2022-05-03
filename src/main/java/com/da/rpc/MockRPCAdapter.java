package com.da.rpc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.da.entity.AppendEntriesResult;
import com.da.entity.AppendEntriesRpc;
import com.da.entity.RequestVoteResult;
import com.da.entity.RequestVoteRpc;
import com.da.node.NodeId;
import com.da.node.nodestatic.NodeEndpoint;
import com.da.rpc.messages.AppendEntriesRpcMessage;
import com.da.rpc.messages.RequestVoteRpcMessage;

/**
 * For testing purpose
 */
public class MockRPCAdapter implements RPCAdapter {

    private LinkedList<Message> messages = new LinkedList<>();

    @Override
    public void sendRequestVote(RequestVoteRpc rpc, Collection<NodeEndpoint> destinationEndpoints) {
        Message m = new Message();
        m.rpc = rpc;
        messages.add(m);
    }

    @Override
    public void replyRequestVote(RequestVoteResult result, NodeEndpoint destinationEndpoint) {
        Message m = new Message();
        m.result = result;
        m.destinationNodeId = destinationEndpoint.getId();
        messages.add(m);
    }

    @Override
    public void sendAppendEntries(AppendEntriesRpc rpc, NodeEndpoint destinationEndpoint) {
        Message m = new Message();
        m.rpc = rpc;
        m.destinationNodeId = destinationEndpoint.getId();
        messages.add(m);
    }

    @Override
    public void replyAppendEntries(AppendEntriesResult result, NodeEndpoint destinationEndpoint) {
        Message m = new Message();
        m.result = result;
        m.destinationNodeId = destinationEndpoint.getId();
        messages.add(m);
    }

    public Message getLastMessage() {
        return messages.isEmpty() ? null : messages.getLast();
    }

    private Message getLastMessageOrDefault() {
        return messages.isEmpty() ? new Message() : messages.getLast();
    }

    public Object getRpc() {
        return getLastMessageOrDefault().rpc;
    }

    public Object getResult() {
        return getLastMessageOrDefault().result;
    }

    public NodeId getDestinationNodeId() {
        return getLastMessageOrDefault().destinationNodeId;
    }

    public int getMessageCount() {
        return messages.size();
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void clearMessage() {
        messages.clear();
    }

    public static class Message {

        private Object rpc; // RPC 消息
        private NodeId destinationNodeId; // 目标节点
        private Object result; // 结果
        // 获取RPC消息
        public Object getRpc() {
            return rpc;
        }
        // 获取目标节点
        public NodeId getDestinationNodeId() {
            return destinationNodeId;
        }
        // 获取结果
        public Object getResult() {
            return result;
        }

        @Override
        public String toString() {
            return "Message{" +
                    "destinationNodeId=" + destinationNodeId +
                    ", rpc=" + rpc +
                    ", result=" + result +
                    '}';
        }

    }

    @Override
    public void initialize() {
        
    }

    @Override
    public void close() {
        
    }

}