package com.da.log.entryImpl;

import com.da.log.entrySequence.EntrySequence;
import com.da.log.entrySequence.MemoryEntrySequence;

public class MemoryLog extends AbstractLog{

    public MemoryLog(){
        this(new MemoryEntrySequence());
    }

    MemoryLog(EntrySequence entrySequence){
        this.entrySequence = entrySequence;
    }

}