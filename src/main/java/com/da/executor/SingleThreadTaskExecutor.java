package com.da.executor;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class SingleThreadTaskExecutor implements TaskExecutor {
    
    private final ExecutorService executorService;

    public SingleThreadTaskExecutor() {
        this(Executors.defaultThreadFactory());
    }

    public SingleThreadTaskExecutor(String name) {
        this(r -> new Thread(r, name));
    }

    private SingleThreadTaskExecutor(ThreadFactory threadFactory) {
        executorService = Executors.newSingleThreadExecutor(threadFactory);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

     @Override
     public <V> Future<V> submit(Callable<V> task) {
         return executorService.submit(task);
     }


    @Override
    public void shutdown() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1, TimeUnit.SECONDS);
    }
}
