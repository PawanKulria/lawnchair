package com.nkart.neo.wallpapers.app;

import java.util.concurrent.Callable;

public interface CustomCallable<R> extends Callable<R> {
    void onMyPostExecute(R result);

    void setUiForLoading();
}