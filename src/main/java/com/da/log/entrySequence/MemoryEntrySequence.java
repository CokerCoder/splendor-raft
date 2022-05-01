package com.da.log.entrySequence;

import com.da.log.Entry;

import java.util.ArrayList;
import java.util.List;

public class MemoryEntrySequence extends  AbstractEntrySequence{
    private final List<Entry> entries = new ArrayList<>();
    private int commitIndex = 0;

    public MemoryEntrySequence(){
        this(1);
    }

    public MemoryEntrySequence(int logIndexOffset){
        super(logIndexOffset);
    }

    @Override
    protected List<Entry> doSubList(int fromIndex, int toIndex) {
        return entries.subList(fromIndex-logIndexOffset, toIndex-logIndexOffset);
    }

    @Override
    protected Entry doGetEntry(int index) {
        return entries.get(index-logIndexOffset);
    }

    @Override
    protected void doAppend(Entry entry) {
        entries.add(entry);
    }

    public void commit(int index){
        commitIndex = index;
    }

    public int getCommitIndex(){
        return commitIndex;
    }

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
