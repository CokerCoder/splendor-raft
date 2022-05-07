package com.da.LogTest;

import com.da.log.Entry;
import com.da.log.NoOpEntry;
import com.da.log.entryImpl.MemoryLog;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MemoryLogTest {

    // (index, term)
    // follower: (1, 1), (2, 1)
    // leader  :         (2, 1), (3, 2)
    @Test
    public void testAppendEntriesFromLeaderSkip() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        List<Entry> leaderEntries = Arrays.asList(
                new NoOpEntry(2, 1),
                new NoOpEntry(3, 2)
        );
        Assert.assertTrue(log.appendEntriesFromLeader(1, 1, leaderEntries));
    }

    // follower: (1, 1), (2, 1)
    // leader  :         (2, 2), (3, 2)
    @Test
    public void testAppendEntriesFromLeaderConflict1() {
        MemoryLog log = new MemoryLog();
        log.appendEntry(1); // 1
        log.appendEntry(1); // 2
        List<Entry> leaderEntries = Arrays.asList(
                new NoOpEntry(2, 2),
                new NoOpEntry(3, 2)
        );
        Assert.assertTrue(log.appendEntriesFromLeader(1, 1, leaderEntries));
    }


}
