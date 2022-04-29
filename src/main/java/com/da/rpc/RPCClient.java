package com.da.rpc;

import com.da.entity.*;
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
        // TODO wait for completed RequestVoteRpc
//        RequestVoteRequest rpcRequest = RVRBuilder.setTerm()
//                .setCandidateId()
//                .setLastLogIndex()
//                .setLastLogIndex()
//                .build();
//        RequestVoteReply reply = blockingStub.requestVote(rpcRequest);
//        return new RequestVoteResult(reply.getTerm(), reply.getVoteGranted());
        return null;
    }

    public AppendEntriesResult appendEntriesRPC(AppendEntriesRpc request) {
        // TODO wait for completed AppendEntriesRpc
//        AppendEntriesRequest rpcRequest = AERbuilder.setTerm()
//                .setLeaderId()
//                .setPrevLogIndex()
//                .setPrevLogTerm()
//                .setLeaderCommit()
//                .build();
//        AppendEntriesReply reply = blockingStub.appendEntries(rpcRequest);
//        return new AppendEntriesResult();
        return null;
    }

    public void close() {
        channel.shutdown();
    }
}
