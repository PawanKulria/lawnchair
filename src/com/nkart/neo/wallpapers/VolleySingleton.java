package com.nkart.neo.wallpapers;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.nkart.neo.MyApplication;

import java.util.Objects;

import app.lawnchair.LawnchairApp;

public class VolleySingleton {
    private static VolleySingleton sInstance = null;
    private RequestQueue mRequestQueue;

    private VolleySingleton() {
        mRequestQueue = Volley.newRequestQueue(Objects.requireNonNull(LawnchairApp.getInstance().getApplicationContext()));
    }

    public static VolleySingleton getInstance() {
        if (sInstance == null) {
            sInstance = new VolleySingleton();
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }
}
