package com.da.log.entrySequence;

import com.da.log.EntryMeta;

//自加
public class EntryIndexItem {
    private int index;
    private long offset;
    private int kind;
    private int term;

    public EntryIndexItem(int index, long offset, int kind, int term) {
        this.index = index;
        this.offset = offset;
        this.kind = kind;
        this.term = term;
    }

    public EntryMeta toEntryMeta(){
        return new EntryMeta(kind, index, term);
    }

    public int getIndex() {
        return index;
    }

    public long getOffset() {
        return offset;
    }

    public int getKind() {
        return kind;
    }

    public int getTerm() {
        return term;
    }
}
