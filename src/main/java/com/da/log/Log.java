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
    // 获取下一条日志的索引
    int getNextIndex();
    // 获取当前的commitIndex
    int getCommitIndex();
    // 判断对方的lastLogIndex和lastLogTerm是否比自己的新
    boolean isNewerThan(int lastLogIndex, int lastLogTerm);
    // 增加一个NO-OP日志
    NoOpEntry appendEntry(int term);
    // 增加一条普通日志
    GeneralEntry appendEntry(int term, byte[] command);
    // 追加来自leader的普通条目
    boolean appendEntriesFromLeader(int prevLogIndex, int prevLogTerm, List<Entry> entries);
    // 推进commitIndex
    void advanceCommitIndex(int newCommitIndex, int currentTerm);
    //void setStateMachine(StateMachine stateMachine);
    void close();

}
