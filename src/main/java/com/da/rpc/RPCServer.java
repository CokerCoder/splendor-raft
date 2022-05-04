package com.da.rpc;

import com.da.entity.*;
import com.da.log.Entry;
import com.da.log.GeneralEntry;
import com.da.node.NodeId;
import com.da.node.RaftNode;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import com.da.rpc.proto.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RPCServer is used for a node to listen to a port and expose its methods.
 */
public class RPCServer {

    private Server server;

    public RPCServer(int port, RaftNode node) {
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

        private final RaftNode node;
        private final RequestVoteReply.Builder RVRBuilder = RequestVoteReply.newBuilder();
        private final AppendEntriesReply.Builder AERBuilder = AppendEntriesReply.newBuilder();

        public RaftService(RaftNode node) {
            this.node = node;
        }

        @Override
        public void requestVote(RequestVoteRequest request, StreamObserver<RequestVoteReply> responseObserver) {

            RequestVoteRpc rpc = new RequestVoteRpc();
            rpc.setTerm(request.getTerm());
            rpc.setCandidateId(NodeId.of(request.getCandidateId()));
            rpc.setLastLogTerm(request.getLastLogTerm());
            rpc.setLastLogIndex(request.getLastLogIndex());

            RequestVoteResult result = node.onReceiveRequestVoteRpc(rpc);

            RequestVoteReply reply = RVRBuilder
                    .setTerm(result.getTerm())
                    .setVoteGranted(result.isVoteGranted())
                    .build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void appendEntries(AppendEntriesRequest request, StreamObserver<AppendEntriesReply> responseObserver) {
            List<Entry> entries = new ArrayList<>(request.getEntriesCount());
            if (request.getEntriesList().get(0).getKind() == Entry.KIND_NO_OP) {
                AppendEntriesRequest.Entry rpcEntry = request.getEntriesList().get(0);
                Entry entry = new GeneralEntry(rpcEntry.getIndex(), rpcEntry.getTerm(), rpcEntry.toByteArray());
                entries.add(entry);
            }
            else {
                for (AppendEntriesRequest.Entry rpcEntry : request.getEntriesList()) {
                    Entry entry = new GeneralEntry(rpcEntry.getIndex(), rpcEntry.getTerm(), rpcEntry.toByteArray());
                    entries.add(entry);
                }
            }
            AppendEntriesRpc rpc = new AppendEntriesRpc();
            rpc.setTerm(request.getTerm());
            rpc.setPrevLogTerm(request.getPrevLogTerm());
            rpc.setPrevLogIndex(request.getPrevLogIndex());
            rpc.setLeaderId(NodeId.of(request.getLeaderId()));
            rpc.setLeaderCommit(request.getLeaderCommit());
            rpc.setEntries(entries);

            AppendEntriesResult result =  node.onReceiveAppendEntriesRpc(rpc);

            AppendEntriesReply reply = AERBuilder
                    .setTerm(result.getTerm())
                    .setSuccess(result.isSuccess())
                    .build();

            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }
}
