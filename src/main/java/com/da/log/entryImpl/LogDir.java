package com.da.log.entryImpl;

import java.io.File;

public interface LogDir {

    void initialize();

    boolean exists();

    File getSnapshotFile();

    File getEntriesFile();

    File getEntryOffsetIndexFile();

    File get();

    boolean renameTo(LogDir logDir);

}
