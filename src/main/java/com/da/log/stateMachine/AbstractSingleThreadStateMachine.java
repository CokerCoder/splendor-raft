package com.da.log.stateMachine;

import com.da.executor.SingleThreadTaskExecutor;
import com.da.executor.TaskExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSingleThreadStateMachine implements StateMachine {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSingleThreadStateMachine.class);
    
    private volatile int lastApplied = 0;
    private final TaskExecutor taskExecutor;

    public AbstractSingleThreadStateMachine() {
        taskExecutor = new SingleThreadTaskExecutor("state-machine");
    }

    @Override
    public int getLastApplied() {
        return lastApplied;
    }

    @Override
    public void applyLog(int index, byte[] commandBytes) {
        taskExecutor.submit(() -> doApplyLog(index, commandBytes));
    }

    private void doApplyLog(int index, byte[] commandBytes) {
        if (index <= lastApplied) {
            return;
        }
        logger.debug("apply log {}", index);
        applyCommand(commandBytes);
        lastApplied = index;
    }

    protected abstract void applyCommand(byte[] commandBytes);

    @Override
    public void shutdown() {
        try {
            taskExecutor.shutdown();
        } catch (InterruptedException e) {
            throw new StateMachineException(e);
        }
    }

}
