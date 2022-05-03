package com.da.log.entrySequence;

import com.da.log.Entry;

import java.util.ArrayList;
import java.util.List;

public class MemoryEntrySequence extends  AbstractEntrySequence{
    private final List<Entry> entries = new ArrayList<>();
    private int commitIndex = 0;
    //构造函数，日志索引偏移 1
    public MemoryEntrySequence(){
        this(1);
    }
    //构造 函数，指定日志索引偏移
    public MemoryEntrySequence(int logIndexOffset){
        super(logIndexOffset);
    }
    //获取子视图
    @Override
    protected List<Entry> doSubList(int fromIndex, int toIndex) {
        return entries.subList(fromIndex-logIndexOffset, toIndex-logIndexOffset);
    }
    //按照索引获取日志条目
    @Override
    protected Entry doGetEntry(int index) {
        return entries.get(index-logIndexOffset);
    }
    // 追加日志条目
    @Override
    protected void doAppend(Entry entry) {
        entries.add(entry);
    }
    // 提交，检验由外层处理
    public void commit(int index){
        commitIndex = index;
    }
    // 获取提交索引
    public int getCommitIndex(){
        return commitIndex;
    }
    // 移除指定索引后的日志条目
    @Override
    protected void doRemoveAfter(int index) {
        if(index<doGetFirstLogIndex()){
            entries.clear();
            nextLogIndex = logIndexOffset;
        }else{
            entries.subList(index-logIndexOffset+1, entries.size()).clear();
            nextLogIndex = index+1;
        }
    }

    public void close(){}

    @Override
    public String toString() {
        return "MemoryEntrySequence{" +
                "logIndexOffset=" + logIndexOffset +
                ", nextLogIndex=" + nextLogIndex +
                ", entries size="+entries.size()+
                '}';
    }
}
