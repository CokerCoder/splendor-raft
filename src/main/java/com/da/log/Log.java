package com.da.log;

import com.da.entity.AppendEntriesRpc;
import com.da.node.NodeId;

public interface Log {
    int ALL_ENTRIES = -1;

    //获得最后一条日志的元消息
    EntryMeta getLastEntryMeta();

    //创建AppendEntries消息
    AppendEntriesRpc createAppendEntriesRpc(int term, NodeId selfId,
                                            int nextIndex, int maxEntries);



}
