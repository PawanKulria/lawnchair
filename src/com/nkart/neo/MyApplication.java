package com.nkart.neo;

import android.app.Application;
import android.content.Context;


public class MyApplication extends Application {
    private static MyApplication sInstance = null;
    @Override
    public void onCreate() {
        try {
            Class.forName("android.os.AsyncTask");
        } catch (Throwable ignore){
        }
        super.onCreate();
        sInstance = this;
    }

    public static Application getInstance() {
        return sInstance;
    }

    public static Context getAppContext() {
        return sInstance.getApplicationContext();
    }

}
