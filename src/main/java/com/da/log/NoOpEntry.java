package com.da.log;

//空日志条目
public class NoOpEntry extends AbstractEntry{
    public NoOpEntry(int kind, int index, int term) {
        super(KIND_NO_OP, index, term);
    }

    public byte[] getCommandBytes(){
        return new byte[0];
    }

    @Override
    public String toString() {
        return "NoOpEntry{" +
                "index=" + index +
                ", term=" + term +
                '}';
    }
}
