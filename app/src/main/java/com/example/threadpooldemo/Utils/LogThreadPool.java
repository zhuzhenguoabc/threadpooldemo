package com.example.threadpooldemo.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class LogThreadPool {
    private static final String TAG = "LogThreadPool";

    ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

    public static ExecutorService executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new ThreadFactory() {
                private final AtomicInteger mCount = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "LogThreadPool #" + mCount.getAndIncrement());
                }
            },
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                    throw new RejectedExecutionException("Task " + runnable.toString() +
                            " rejected from " +
                            threadPoolExecutor.toString());
                }
            });

}