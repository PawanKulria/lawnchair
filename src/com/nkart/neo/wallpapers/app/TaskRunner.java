package com.nkart.neo.wallpapers.app;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskRunner {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future result;
    private boolean isCancelled;

    public <R> void executeAsync(CustomCallable<R> callable) {
        try {
            callable.setUiForLoading();
            result = executor.submit(new RunnableTask<>(handler, callable));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel(boolean mayInterruptIfRunning) {
        try {
            handler.removeCallbacksAndMessages(null);
            if (result != null) {
                result.cancel(mayInterruptIfRunning);
            }
            isCancelled = true;
            executor.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class RunnableTask<R> implements Runnable {
        private final Handler handler;
        private final CustomCallable<R> callable;

        public RunnableTask(Handler handler, CustomCallable<R> callable) {
            this.handler = handler;
            this.callable = callable;
        }

        @Override
        public void run() {
            try {
                final R result = callable.call();
                if (!isCancelled)
                    handler.post(new RunnableTaskForHandler(callable, result));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class RunnableTaskForHandler<R> implements Runnable {

        private CustomCallable<R> callable;
        private R result;

        public RunnableTaskForHandler(CustomCallable<R> callable, R result) {
            this.callable = callable;
            this.result = result;
        }

        @Override
        public void run() {
            callable.onMyPostExecute(result);
        }
    }
}