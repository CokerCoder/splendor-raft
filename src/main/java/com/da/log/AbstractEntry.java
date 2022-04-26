package com.da.log;

public class AbstractEntry implements Entry{
    private final int kind;
    protected final int index;
    protected final int term;

    public AbstractEntry(int kind, int index, int term) {
        this.kind = kind;
        this.index = index;
        this.term = term;
    }

    public int getKind(){
        return this.kind;
    }

    public int getIndex(){
        return this.index;
    }

    public int getTerm(){
        return this.term;
    }

    public EntryMeta getMeta(){
        return new EntryMeta(kind,index,term);
    }

    @Override
    public byte[] getCommandBytes() {
        return new byte[0];
    }
}
