package com.da.log;

public interface logModule {

    void write(LogEntry logEntry);

    LogEntry read(int index);

    void removeFromIndex(Long startIndex);

    LogEntry getLastEntry();

    int getLastIndex();
}
