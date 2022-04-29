package com.da.rpc;

import com.da.entity.*;
import com.da.node.Node;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

/**
 * RPCServer is used for a node to listen to a port and expose its methods.
 */
class RPCServer {

    private Server server;

    public RPCServer(int port, Node node) {
        server = ServerBuilder.forPort(port).addService(new RaftService(node)).build();

    }

    public void start() throws IOException {
        server.start();
    }

    void close() {
        server.shutdown();
    }

    /**
     * A service that encapsulates the method called by other nodes.
     * It will decode the message and handle the request by calling the method
     * from Node.
     */
    private static class RaftService extends RaftGrpc.RaftImplBase {

        private final Node node;
        private final RequestVoteReply.Builder RVRbuilder = RequestVoteReply.newBuilder();
        private final AppendEntriesReply.Builder AERbuilder = AppendEntriesReply.newBuilder();

        public RaftService(Node node) {
            this.node = node;
        }

        @Override
        public void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteReply> responseObserver) {
            RequestVoteRpc r = null; // TODO new RequestVoteRpc(); r.XXX = request.XXX;
            RequestVoteResult result = node.handleRequestVote(r);
            RequestVoteReply reply = RVRbuilder.setTerm(result.getTerm())
                    .setVoteGranted(result.isVoteGranted())
                    .build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesReply> responseObserver) {
            AppendEntriesRpc r = null; // TODO new AppendEntriesRpc(); r.XXX = request.XXX;
            AppendEntriesResult result =  node.handleAppendEntries(r);
//            AppendEntriesReply reply = AERbuilder.setTerm()
//                    .setSuccess()
//                    .build();
//            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
