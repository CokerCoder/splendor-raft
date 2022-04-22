package com.da.log;

import com.da.common.Command;

public class LogEntry implements Comparable {
    
    private Long index;
    private long term;
    private Command command;
    
    @Override
    public int compareTo(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }
    
}
