package com.yeahdev.yeahstreamer;

import android.app.Application;
import com.firebase.client.Firebase;
import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;


public class AppApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Fabric.with(this, new Crashlytics());
    }
}
