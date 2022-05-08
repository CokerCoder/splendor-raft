package com.da.node;

import com.da.log.stateMachine.StateMachine;

/**
 * 节点接口
 */
public interface Node {

    void start();
    
    void stop() throws InterruptedException;

    void registerStateMachine(StateMachine stateMachine);

    void appendLog(byte[] commandBytes);

}
