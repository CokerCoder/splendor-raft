package com.da.log.stateMachine;

public interface StateMachine {

    int getLastApplied();

    void applyLog(int index, byte[] commandBytes);

    void shutdown();

}
