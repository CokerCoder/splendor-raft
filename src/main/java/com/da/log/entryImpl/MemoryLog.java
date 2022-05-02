package com.da.log.entryImpl;

import com.da.log.GeneralEntry;
import com.da.log.entrySequence.EntrySequence;
import com.da.log.entrySequence.MemoryEntrySequence;

public class MemoryLog extends AbstractLog{

    public MemoryLog(){
        this(new MemoryEntrySequence());
    }

    MemoryLog(EntrySequence entrySequence){
        this.entrySequence = entrySequence;
    }

    @Override
    public int getNextIndex() {
        return 0;
    }

    @Override
    public int getCommitIndex() {
        return 0;
    }

    @Override
    public GeneralEntry appendEntry(int term, byte[] command) {
        return null;
    }

    @Override
    public void close() {

    }
}
