package com.da.kv.client;

import com.da.kv.messages.GetCommand;
import com.da.kv.messages.GetCommandResponse;
import com.da.kv.messages.SetCommand;
import com.da.kv.messages.SetCommandResponse;
import com.da.rpc.proto.*;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * Implement IO to servers
 */
public class RPCChannel {

    private final ManagedChannel channel;
    private final KVGrpc.KVBlockingStub blockingStub;

    private final GetRequest.Builder GRBuilder = GetRequest.newBuilder();
    private final SetRequest.Builder SRBuilder = SetRequest.newBuilder();

    public RPCChannel(String target) {
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        blockingStub = KVGrpc.newBlockingStub(channel);
    }

    public GetCommandResponse get(GetCommand command) {
        GetResponse response = blockingStub.getKey(GRBuilder.setKey(command.getKey()).build());
        return new GetCommandResponse(response.getValue().toByteArray());
    }

    public SetCommandResponse set(SetCommand command) {
        SetRequest request = SRBuilder.setRequestId(command.getRequestId())
                .setKey(command.getKey())
                .setValue(ByteString.copyFrom(command.getValue()))
                .build();
        SetResponse response = blockingStub.setKey(request);
        if (response.getSuccess()) {
            return new SetCommandResponse();
        }
        else if (!response.getLeaderId().isEmpty()) {
            return new SetCommandResponse(response.getLeaderId());
        }
        return new SetCommandResponse(response.getErrorCode(), response.getErrorMessage());
    }

    public void close() {
        channel.shutdown();
    }
}

