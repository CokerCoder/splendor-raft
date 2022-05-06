package com.da.kv.server;

import java.io.IOException;

import com.da.kv.messages.GetCommand;
import com.da.kv.messages.GetCommandResponse;
import com.da.kv.messages.SetCommand;
import com.da.kv.messages.SetCommandResponse;
import com.da.node.RaftNode;
import com.da.rpc.proto.GetRequest;
import com.da.rpc.proto.GetResponse;
import com.da.rpc.proto.KVGrpc;
import com.da.rpc.proto.SetRequest;
import com.da.rpc.proto.SetResponse;
import com.google.protobuf.ByteString;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class KVServer {

    private RaftNode node;
    private int port;

    private Server server; // gRPC server
    private KVService service;

    public KVServer(RaftNode node, int port) {
        this.node = node;
        this.port = port;

        this.service = new KVService(node);

        server = ServerBuilder.forPort(port).addService(new RaftKVService(service)).build();
    }

    public void start() throws IOException {
        System.out.println("Start kv server at port " + port);

        this.node.start(); // raft node
        this.server.start(); // kv server
    }

    public void stop() throws InterruptedException {
        this.node.stop();
    }

    /**
     * A service that encapsulates the method called by other nodes.
     * It will decode the message and handle the request using the map in service
     */
    private static class RaftKVService extends KVGrpc.KVImplBase {

        private final KVService service;
        private final GetResponse.Builder getResponseBuilder = GetResponse.newBuilder();
        private final SetResponse.Builder setResponseBuilder = SetResponse.newBuilder();

        public RaftKVService(KVService service) {
            this.service = service;
        }

        @Override
        public void getKey(GetRequest request, StreamObserver<GetResponse> responseObserver) {
            GetCommand command = new GetCommand(request.getKey());
            // query the kv service to retrieve the value
            GetCommandResponse response = service.get(command);

            // encode the message
            GetResponse reply = getResponseBuilder.setValue(
                ByteString.copyFrom(response.getValue())
            ).build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void setKey(SetRequest request, StreamObserver<SetResponse> responseObserver) {
            SetCommand command = new SetCommand(
                request.getKey(), request.getValue().toByteArray());
            SetCommandResponse response = service.set(command);

            SetResponse reply = setResponseBuilder.setSuccess(true).build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

    }
    
}
