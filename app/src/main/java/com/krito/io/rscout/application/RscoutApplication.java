package com.krito.io.rscout.application;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

/**
 * Created by Ahmed Ali on 8/4/2018.
 */

public class RscoutApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }
}
