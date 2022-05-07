package com.da.rpc;

import com.da.entity.*;
import com.da.log.Entry;
import com.da.rpc.proto.*;

import com.google.protobuf.ByteString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.List;

/**
 * RPCClient is used for a node to call remote method from other nodes.
 */
public class RPCClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPCClient.class);

    private final RaftGrpc.RaftBlockingStub blockingStub;
    private final ManagedChannel channel;

    private final RequestVoteRequest.Builder RVRBuilder = RequestVoteRequest.newBuilder();
    private final AppendEntriesRequest.Builder AERBuilder = AppendEntriesRequest.newBuilder();
    private final AppendEntriesRequest.Entry.Builder EntryBuilder = AppendEntriesRequest.Entry.newBuilder();

    public RPCClient(String target) {
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
        
        RequestVoteReply reply = null;
        try {
            reply = blockingStub.requestVote(rpcRequest);
        } catch (Exception e) {
            LOGGER.error("requestVote rpc error");
        }
       
       return new RequestVoteResult(reply.getTerm(), reply.getVoteGranted());
    }

    public AppendEntriesResult appendEntriesRPC(AppendEntriesRpc request) {
        // AERBuilder.setEntries
        List<Entry> entries = request.getEntries();
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            EntryBuilder
                    .setKind(entry.getKind())
                    .setIndex(entry.getIndex())
                    .setTerm(entry.getTerm())
                    .setData(ByteString.copyFrom(entry.getCommandBytes()));
            AERBuilder.setEntries(i, EntryBuilder);
        }

        AppendEntriesRequest rpcRequest = AERBuilder
                .setTerm(request.getTerm())
                .setLeaderId(request.getLeaderId().toString())
                .setPrevLogIndex(request.getPrevLogIndex())
                .setPrevLogTerm(request.getPrevLogTerm())
                .setLeaderCommit(request.getLeaderCommit())
                .build();

        AppendEntriesReply reply = null;
        try {
            reply = blockingStub.appendEntries(rpcRequest);
        } catch (Exception e) {
            LOGGER.error("appendEntries rpc error");
        }
       
       return new AppendEntriesResult(reply.getTerm(), reply.getSuccess());
    }

    public void close() {
        channel.shutdown();
    }
}
