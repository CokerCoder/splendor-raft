
package com.da.entity;
import com.da.log.LogEntry;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppendEntriesRpc {

    /** 当前的任期号 **/
    private int term;

    /** 领导人ID */
    private int leaderId;

    /** 紧邻新日志前的那个日志条目的索引 */
    private int prevLogIndex;

    /** 紧邻新日志前的那个日志条目的任期号 */
    private int prevLogTerm;

    /** 准备存储的日志条目（表示心跳时为空；可以一次性发送多个） */
    private LogEntry[] entries;

    /** 领导人已经提交的日志的索引值  */
    private int leaderCommit;
    
}
