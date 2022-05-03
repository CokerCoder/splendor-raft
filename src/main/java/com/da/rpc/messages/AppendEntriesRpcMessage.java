package com.da.rpc.messages;

import com.da.entity.AppendEntriesResult;
import com.da.entity.AppendEntriesRpc;
import com.da.node.NodeId;
import com.da.rpc.Channel;

public class AppendEntriesRpcMessage extends AbstractRpcMessage<AppendEntriesRpc> {

    public AppendEntriesRpcMessage(AppendEntriesRpc rpc, NodeId sourceNodeId, Channel channel) {
        super(rpc, sourceNodeId, channel);
    }

    public AppendEntriesRpcMessage(AppendEntriesRpc rpc, NodeId of) {
        this(rpc, of, null);
    }

}
