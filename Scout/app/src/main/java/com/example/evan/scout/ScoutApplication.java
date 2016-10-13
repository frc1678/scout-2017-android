package com.example.evan.scout;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.instabug.library.IBGInvocationEvent;
import com.instabug.library.Instabug;

public class ScoutApplication extends Application implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onCreate() {
        super.onCreate();
        new Instabug.Builder(this, "a2dea1c21dcb2688370dbfc0a7dc80bd")
                .setInvocationEvent(IBGInvocationEvent.IBGInvocationEventShake)
                .build();
        registerActivityLifecycleCallbacks(this);
    }
    private static Activity currentActivity = null;
    public static Activity getCurrentActivity() throws NullPointerException {
        if (currentActivity == null) {
            throw new NullPointerException();
        }
        return currentActivity;
    }

    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
    }

    public void onActivityCreated(Activity activity, Bundle bundle) {
        currentActivity = activity;
    }

    //do nothing
    public void onActivityResumed(Activity activity) {}
    public void onActivityStopped(Activity activity) {}
    public void onActivityPaused(Activity activity) {}
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}
    public void onActivityStarted(Activity activity) {}
}
