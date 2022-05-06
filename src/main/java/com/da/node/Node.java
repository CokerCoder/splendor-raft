package com.da.node;

import com.da.kv.server.KVService;

/**
 * 节点接口
 */
public interface Node {

    void start();
    
    void stop() throws InterruptedException;

    void registerStateMachine(KVService service);

}
