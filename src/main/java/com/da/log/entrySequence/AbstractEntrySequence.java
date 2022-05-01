package com.da.log.entrySequence;

import com.da.log.Entry;
import com.da.log.EntryMeta;
import com.da.log.LogEntry;

import java.util.Collections;
import java.util.List;

public abstract class AbstractEntrySequence implements EntrySequence{
    int logIndexOffset;
    int nextLogIndex;

    AbstractEntrySequence(int logIndexOffset){
        this.logIndexOffset = logIndexOffset;
        this.nextLogIndex = logIndexOffset;
    }

    public boolean isEmpty(){
        return logIndexOffset==nextLogIndex;
    }

    public int getFirstLogIndex(){
        if(isEmpty()){
            throw new IllegalStateException("No Log index");
        }
        return doGetFirstLogIndex();
    }

    int doGetFirstLogIndex(){return logIndexOffset;}

    public int getLastLogIndex(){
        if(isEmpty()){
            throw new IllegalStateException("No Log index");
        }
        return doGetLastLogIndex();
    }

    int doGetLastLogIndex(){return nextLogIndex-1;}

    public int getNextLogIndex(){
        return nextLogIndex;
    }

    public boolean isEntryPresent(int index){
        return !isEmpty() && index>=doGetFirstLogIndex() && index<=doGetLastLogIndex();
    }

    public Entry getEntry(int index){
        if(!isEntryPresent(index)){
            return null;
        }
        return doGetEntry(index);
    }

    public EntryMeta getEntryMeta(int index){
        Entry entry = getEntry(index);
        return entry!=null?entry.getMeta():null;
    }

    public Entry getLastEntry(){
        return isEmpty() ?null:doGetEntry(doGetLastLogIndex());
    }

    //获取指定索引的日志条目 （抽象）
    protected abstract Entry doGetEntry(int index);

    //获得序列的子视图
    public List<Entry> subList(int fromIndex){
        if(isEmpty() || fromIndex>doGetLastLogIndex()){
            return Collections.emptyList();
        }
        return subList(Math.max(fromIndex,doGetFirstLogIndex()), nextLogIndex);
    }

    //获得序列子视图，指定范围
    public LogEntry[] subList(int fromIndex, int toIndex){
        if(isEmpty()){
            throw new IllegalStateException("EntrySequence is empty");
        }
        if(fromIndex<doGetFirstLogIndex() || toIndex>doGetLastLogIndex()+1 ||
            fromIndex>toIndex){
            throw new IllegalArgumentException("Illegal from index or toIndex");
        }
        return doSubList(fromIndex, toIndex);
    }

    protected abstract List<Entry> doSubList(int fromIndex, int toIndex);


    //追加单条日志
    public void append(Entry entry) {
        if(entry.getIndex()!=nextLogIndex){
            throw new IllegalArgumentException("entry index must be "+ nextLogIndex);
        }
        doAppend(entry);
        nextLogIndex++;
    }

    //追加多条日志
    public void append(List<Entry> entries){
        for(Entry entry : entries){
            append(entry);
        }
    }

    protected abstract void doAppend(Entry entry);

    public void removeAfter(int index){
        if(isEmpty() || index>=doGetLastLogIndex()){
            return;
        }
        doRemoveAfter(index);
    }

    protected abstract void doRemoveAfter(int index);

    //void commit(int index);
    //int getCommitIndex();
    //void close();
}
