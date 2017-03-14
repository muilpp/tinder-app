package com.tinderapp.model;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class BaseApplication extends Application {
    private static Bus mEventBus;
    private static String userToken;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Bus getEventBus() {
        return mEventBus;
    }

    public static String getUserToken() { return userToken; }

    public static void setUserToken(String token) { userToken = token; }

    @Override
    public void onCreate() {
        super.onCreate();

        mEventBus = new Bus(ThreadEnforcer.ANY);
    }
}
