package com.da.node;

/**
 * 在node重启后需要恢复最新的currentTerm和votedFor数据
 */
public interface NodeStore {
    int getTerm();
    void setTerm(int term);
    NodeId getVotedFor();
    void setVotedFor(NodeId votedFor);
    void close();
}
