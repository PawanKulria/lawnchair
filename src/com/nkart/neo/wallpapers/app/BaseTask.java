package com.nkart.neo.wallpapers.app;

public abstract class BaseTask<R> implements CustomCallable<R> {
    @Override
    public void setUiForLoading() {

    }

    @Override
    public void onMyPostExecute(R result) {

    }

    @Override
    public R call() {
        return null;
    }
}