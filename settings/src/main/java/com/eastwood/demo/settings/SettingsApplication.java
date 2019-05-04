package com.eastwood.demo.settings;

import android.app.Application;
import android.util.Log;

public class SettingsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("SettingsApplication", "onCreate");
    }
}
