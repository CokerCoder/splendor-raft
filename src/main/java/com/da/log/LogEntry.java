package com.da.log;

import com.da.common.Command;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LogEntry implements Comparable {
    
    private int index;
    private int term;
    private Command command;
    
    @Override
    public int compareTo(Object o) {
        // TODO Auto-generated method stub
        return 0;
    }


}
