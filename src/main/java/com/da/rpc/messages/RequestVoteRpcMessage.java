package com.da.rpc.messages;

import com.da.entity.RequestVoteRpc;
import com.da.node.NodeId;
import com.da.rpc.Channel;

public class RequestVoteRpcMessage extends AbstractRpcMessage<RequestVoteRpc> {

    public RequestVoteRpcMessage(RequestVoteRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }

    public RequestVoteRpcMessage(RequestVoteRpc rpc, NodeId of) {
        this(rpc, of, null);
    }

}