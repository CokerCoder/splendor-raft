package com.da.log.entrySequence;

import com.da.log.EntryMeta;

import javax.annotation.concurrent.Immutable;

//自加
@Immutable
public class EntryIndexItem {

    private final int index;
    private final long offset;
    private final int kind;
    private final int term;

    EntryIndexItem(int index, long offset, int kind, int term) {
        this.index = index;
        this.offset = offset;
        this.kind = kind;
        this.term = term;
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

    EntryMeta toEntryMeta() {
        return new EntryMeta(kind, index, term);
    }

}
