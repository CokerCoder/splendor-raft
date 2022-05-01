package com.da.node;

/**
 * 节点接口
 */
public interface Node {

    void start();
    
    void stop() throws InterruptedException;

}
