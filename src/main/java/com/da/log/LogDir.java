package com.da.log;

import java.io.File;

public interface LogDir {
    void initialize();
    boolean exists();
    File getEntriesFile();
    File getEntryOffsetIndexFile(); //获得EntryIndexFile对应的文件
    File get();                     //获取目录
    boolean renameTo(LogDir logDir);//重命名目录
}
