package com.da.log.entrySequence;

import com.da.log.Entry;
import com.da.log.EntryMeta;
import com.da.log.LogEntry;

import java.util.List;

public interface EntrySequence {
    boolean isEmpty();
    int getFirstLogIndex();
    int getLastLogIndex();
    int getNextLogIndex();
    List<Entry> subList(int fromIndex);    //获得序列的子视图
    LogEntry[] subList(int fromIndex, int toIndex); //获得序列子视图，指定范围
    boolean isEntryPresent(int index);
    EntryMeta getEntryMeta(int index);
    Entry getEntry(int index);
    Entry getLastEntry();
    void append(Entry entry);   //追加单条日志
    void append(List<Entry> entries);   //追加多条日志
    void commit(int index);
    int getCommitIndex();
    void removeAfter(int index);
    void close();
}
