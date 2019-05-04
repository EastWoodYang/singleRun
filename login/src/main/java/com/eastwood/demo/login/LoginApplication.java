package com.eastwood.demo.login;

import android.app.Application;
import android.util.Log;

public class LoginApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("LoginApplication", "onCreate");
    }
}
