package com.da.rpc;

import com.da.entity.*;
import com.da.rpc.proto.AppendEntriesReply;
import com.da.rpc.proto.AppendEntriesRequest;
import com.da.rpc.proto.RaftGrpc;
import com.da.rpc.proto.RequestVoteReply;
import com.da.rpc.proto.RequestVoteRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * RPCClient is used for a node to call remote method from other nodes.
 */
class RPCClient {

    private final RaftGrpc.RaftBlockingStub blockingStub;
    private final ManagedChannel channel;
    
    private final String target;

    private final RequestVoteRequest.Builder RVRBuilder = RequestVoteRequest.newBuilder();
    private final AppendEntriesRequest.Builder AERbuilder = AppendEntriesRequest.newBuilder();

    public RPCClient(String target) {
        this.target = target;
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        blockingStub = RaftGrpc.newBlockingStub(channel);
    }

    public RequestVoteResult requestVoteRPC(RequestVoteRpc request) {
        RequestVoteRequest rpcRequest = RVRBuilder
            .setTerm(request.getTerm())
            .setCandidateId(request.getCandidateId().toString())
            .setLastLogIndex(request.getLastLogIndex())
            .setLastLogTerm(request.getLastLogTerm())
            .build();
       RequestVoteReply reply = blockingStub.requestVote(rpcRequest);
       return new RequestVoteResult(reply.getTerm(), reply.getVoteGranted());
    }

    public AppendEntriesResult appendEntriesRPC(AppendEntriesRpc request) {
        AppendEntriesRequest rpcRequest = AERbuilder
            .setTerm(request.getTerm())
            .setLeaderId(request.getLeaderId().toString())
            .setPrevLogIndex(request.getPrevLogIndex())
            .setPrevLogTerm(request.getPrevLogTerm())
            .setLeaderCommit(request.getLeaderCommit())
            .build();
       AppendEntriesReply reply = blockingStub.appendEntries(rpcRequest);
       return new AppendEntriesResult(reply.getTerm(), reply.getSuccess());
    }

    public void close() {
        channel.shutdown();
    }
}
