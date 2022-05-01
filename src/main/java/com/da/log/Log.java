package com.da.log;

import com.da.entity.AppendEntriesRpc;
import com.da.node.NodeId;

import java.util.List;

public interface Log {
    int ALL_ENTRIES = -1;

    //获得最后一条日志的元消息
    EntryMeta getLastEntryMeta();

    //创建AppendEntries消息
    AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId,
                                            int nextIndex, int maxEntries);
    int getNextIndex();
    int getCommitIndex();
    boolean isNewerThan(int lastLogIndex, int lastLogTerm);
    NoOpEntry appendEntry(int term);
    GeneralEntry appendEntry(int term, byte[] command);
    boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> entries);
    void advanceCommitIndex(int newCommitIndex, int currentTerm);
    //void setStateMachine(StateMachine stateMachine);
    void close();

}
