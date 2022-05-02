package com.da.log;

public class EntryMeta {
    private int kind;
    private int index;
    private int term;

    public EntryMeta(int kind, int index, int term) {
        this.kind = kind;
        this.index = index;
        this.term = term;
    }

    public int getKind() {
        return kind;
    }

    public int getIndex() {
        return index;
    }

    public int getTerm() {
        return term;
    }
}
