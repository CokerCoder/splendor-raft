package com.da.entity;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppendEntriesResult {
    /** 当前的任期号 */
    private int term;

    /** 当跟随者包含了匹配上 prevLogIndex 和 prevLogTerm 的日志时为真，否则为假  */
    private boolean success;

    public AppendEntriesResult(int term) {
        this.term = term;
    }

    public AppendEntriesResult(boolean success) {
        this.success = success;
    }

    public AppendEntriesResult(int term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public static AppendEntriesResult fail() {
        return new AppendEntriesResult(false);
    }

    public static AppendEntriesResult succeed() {
        return new AppendEntriesResult(true);
    }
}
