package com.da.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AppendEntriesResult {
    /** 当前的任期号 */
    private int term; // 选举term

    public int getTerm() {
        return term;
    }

    /** 当跟随者包含了匹配上 prevLogIndex 和 prevLogTerm 的日志时为真，否则为假  */
    private boolean success; // 是否追加成功

    public boolean isSuccess() {
        return success;
    }


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


//    public static AppendEntriesResult fail() {
//        return new AppendEntriesResult(false);
//    }
//
//    public static AppendEntriesResult succeed() {
//        return new AppendEntriesResult(true);
//    }
}
