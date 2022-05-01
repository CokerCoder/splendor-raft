package com.da.log;

public interface Entry {
    //日志条目类型
    int KIND_NO_OP = 0;
    int KIND_GENERAL=1;
    //获取类型
    int getKind();
    //获取索引
    int getIndex();
    //获取term
    int getTerm();
    //获取元信息（kind,term,index）
    EntryMeta getMeta();
    //获取日志负载
    byte[] getCommandBytes();
}
