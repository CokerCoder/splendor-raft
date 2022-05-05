package com.da.LogTest;

import com.da.log.Entry;
import com.da.log.NoOpEntry;
import com.da.log.entrySequence.ByteArraySeekableFile;
import com.da.log.entrySequence.EntriesFile;
import com.da.log.entrySequence.EntryIndexFile;
import com.da.log.entrySequence.FileEntrySequence;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class FileEntrySequenceTest {

    private EntriesFile entriesFile;
    private EntryIndexFile entryIndexFile;

    @Before
    public void setUp() throws IOException {
        entriesFile = new EntriesFile(new ByteArraySeekableFile());
        entryIndexFile = new EntryIndexFile(new ByteArraySeekableFile());
    }

    @Test
    public void testInitialize() throws IOException {
        entryIndexFile.appendEntryIndex(1, 0L, 1, 1);
        entryIndexFile.appendEntryIndex(2, 20L, 1, 1);
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        Assert.assertEquals(3, sequence.getNextLogIndex());
        Assert.assertEquals(1, sequence.getFirstLogIndex());
        Assert.assertEquals(2, sequence.getLastLogIndex());
        Assert.assertEquals(2, sequence.getCommitIndex());
    }

    @Test
    public void testAppendEntry() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        Assert.assertEquals(1, sequence.getNextLogIndex());
        sequence.append(new NoOpEntry(1, 1));
        Assert.assertEquals(2, sequence.getNextLogIndex());
        Assert.assertEquals(1, sequence.getLastEntry().getIndex());
    }

    private void appendEntryToFile(Entry entry) throws IOException {
        long offset = entriesFile.appendEntry(entry);
        entryIndexFile.appendEntryIndex(entry.getIndex(), offset, entry.getKind(), entry.getTerm());
    }

    @Test
    public void testGetEntry() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1));
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(2, 1));
        Assert.assertNull(sequence.getEntry(0));
        Assert.assertEquals(1, sequence.getEntry(1).getIndex());
        Assert.assertEquals(2, sequence.getEntry(2).getIndex());
        Assert.assertNull(sequence.getEntry(3));
    }

    @Test
    public void testSubList2() throws IOException {
        appendEntryToFile(new NoOpEntry(1, 1)); // 1
        appendEntryToFile(new NoOpEntry(2, 2)); // 2
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 3)); // 3
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 4)); // 4

        List<Entry> subList = sequence.subView(2);
        Assert.assertEquals(3, subList.size());
        Assert.assertEquals(2, subList.get(0).getIndex());
        Assert.assertEquals(4, subList.get(2).getIndex());
    }

    @Test
    public void testRemoveAfterPendingEntries2() {
        FileEntrySequence sequence = new FileEntrySequence(entriesFile, entryIndexFile, 1);
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 1));
        sequence.append(new NoOpEntry(sequence.getNextLogIndex(), 2));
        Assert.assertEquals(1, sequence.getFirstLogIndex());
        Assert.assertEquals(2, sequence.getLastLogIndex());
        sequence.removeAfter(1);
        Assert.assertEquals(1, sequence.getFirstLogIndex());
        Assert.assertEquals(1, sequence.getLastLogIndex());
    }


}
