package com.da.log;

public interface logModule {

    void write(LogEntry logEntry);

    LogEntry read(Long index);

    void removeFromIndex(Long startIndex);

    LogEntry getLastEntry();

    Long getLastIndex();
}
