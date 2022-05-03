package com.da.log.entryImpl;

import com.da.log.entrySequence.FileEntrySequence;

import java.io.File;

//p141
public class FileLog extends AbstractLog{

    @Override
    public int getNextIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getCommitIndex() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
        
    }
    // private final RootDir rootDir;

    // public FileLog(File baseDir){
    //     rootDir = new RootDir(baseDir); //baseDir 表示日志目录的根目录
    //     //获得最新日志代
    //     LogGeneration latestGeneration = rootDir.getLatestGeneration();
    //     if(latestGeneration!=null){
    //         //日志存在
    //         entrySequence = new FileEntrySequence(
    //                 latestGeneration,latestGeneration.getLogIndexOffset()
    //         );
    //     }else{
    //         //日志不存在
    //         LogGeneration firstGeneration = rootDir.createFirstGeneration();
    //         entrySequence = new FileEntrySequence(firstGeneration, 1);
    //     }
    // }
}
