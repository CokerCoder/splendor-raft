package com.da.node;

import com.da.entity.AppendEntriesRpc;
import com.da.entity.AppendEntriesResult;
import com.da.entity.RequestVoteRpc;
import com.da.entity.RequestVoteResult;
import com.da.node.roles.AbstractNodeRole;
import com.da.raft.Consensus;
import com.da.raft.RaftConsensus;

/**
 * Raft节点实现，负责处理节点之间rvote/aentry请求以及客户端请求
 * 启动时node角色为follower，term为0（有日志的话需要从最后一条日志重新计算最后的term）
 */
public class RaftNode implements Node {

    private AbstractNodeRole role; // 当前的角色及信息
    private Consensus consensus; // 共识算法实现
    private boolean started;

    private RaftNode() {};

    @Override
    public void init() {
        this.started = true;
        this.consensus = new RaftConsensus(this);
    }

    @Override
    public void destroy() {
        this.started = false;

        // stop all other services
        // ...
    }

    @Override
    public RequestVoteResult handleRequestVote(RequestVoteRpc request) {
        return consensus.requestVote(request);
    }

    @Override
    public AppendEntriesResult handleAppendEntries(AppendEntriesRpc request) {
        return consensus.appendEntries(request);
    }

    // 角色变更方法
    private void changeToRole(AbstractNodeRole newRole) {
        role = newRole;
    }
    
}
