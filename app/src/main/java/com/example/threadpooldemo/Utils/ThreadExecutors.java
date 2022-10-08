package com.example.threadpooldemo.Utils;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadExecutors {
    public static final String TAG = "ThreadExecutors";

    public static final ExecutorService diskIO = Executors.newSingleThreadExecutor();
    public static final ExecutorService singleTask = Executors.newSingleThreadExecutor();
    public static final ExecutorService networkIO = Executors.newFixedThreadPool(3);

    public static final MainThreadExecutor mainThread = new MainThreadExecutor();

    public static final class MainThreadExecutor implements Executor {
        Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            handler.post(command);
        }
    }

}