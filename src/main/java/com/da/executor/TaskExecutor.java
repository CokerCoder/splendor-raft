package com.da.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * 抽象化的任务执行器，可以在实际运行中使用异步单线程实现
 */
public interface TaskExecutor {
    /**
     * 提交任务
     * @param task
     * @return
     */
    Future<?> submit(Runnable task);


    /**
     * 提交任务，任务有返回值
     * @param task
     * @param <V>
     * @return
     */
    <V> Future<V> submit(Callable<V> task);



    /**
     * 关闭任务执行器
     * @throws InterruptedException
     */
    void shutdown() throws InterruptedException;
}
