package com.da.log.entrySequence;

import com.da.log.Entry;
import com.da.log.GeneralEntry;
import com.da.log.NoOpEntry;

public class EntryFactory {
    public Entry create(int kind, int index, int term, byte[] commandBytes){
        switch (kind){
            case Entry.KIND_NO_OP:
                return new NoOpEntry(index,term);
            case Entry.KIND_GENERAL:
                return new GeneralEntry(index, term, commandBytes);
            default:
                throw new IllegalArgumentException("unexpected entry kind");
        }
    }
}
