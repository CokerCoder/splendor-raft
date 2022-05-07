package com.da.node.nodestatic;

//集群成员信息

import com.da.log.ReplicatingState;
import com.da.node.NodeId;

/**
 * (1) NodeEndpoint: 节点的服务器ip和端口
 * (2) ReplicationState:复制进度，包含nextIndex and matchIndex
 */
public class GroupMember {
    private final NodeEndpoint endpoint;
    private ReplicatingState replicatingState;

    //无日志复制状态的构造
    GroupMember(NodeEndpoint endpoint){
        this(endpoint, null);
    }

    //带日志复制状态的构造
    GroupMember(NodeEndpoint endpoint, ReplicatingState replicatingState){
        this.endpoint = endpoint;
        this.replicatingState = replicatingState;
    }

    public NodeEndpoint getEndpoint() {
        return endpoint;
    }


    public ReplicatingState getReplicatingState() {
        return replicatingState;
    }

    private ReplicatingState ensureReplicatingState(){
        if(replicatingState==null){
            throw new IllegalStateException("Replication state not set yet!");
        }
        return replicatingState;
    }

    public int getNextIndex(){ return ensureReplicatingState().getNextIndex(); }

    int getMatchIndex(){return ensureReplicatingState().getMatchIndex();}

    public boolean advanceReplicatingState(int lastEntryIndex) {
        return ensureReplicatingState().advance(lastEntryIndex);
    }

    public boolean idEquals(NodeId id) {
        return id.equals(endpoint.getId());
    }

    public NodeId getId(){
        return endpoint.getId();
    }

    public boolean backoffNextIndex() {
        return ensureReplicatingState().backOffNextIndex();
    }
}
