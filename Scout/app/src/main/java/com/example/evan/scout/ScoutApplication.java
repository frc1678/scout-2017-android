package com.example.evan.scout;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ScoutApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private Activity currentActivity = null;
    @Override
    public void onCreate() {
        super.onCreate();
        new LeaderBoardUpdateLoop(this).start();
    }

    public Activity getCurrentActivity() {return currentActivity;}

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
