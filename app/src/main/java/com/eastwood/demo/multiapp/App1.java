package com.eastwood.demo.multiapp;

import android.app.Application;
import android.util.Log;

/**
 * @author eastwood
 * createDate: 2019-03-05
 */
public class App1 extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("Application", "App1 onCreate...");
    }
}
