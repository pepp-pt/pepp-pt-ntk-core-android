package org.pepppt.core.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

import org.pepppt.core.ProximityTracingService;

/**
 * This class will be started as a foreground service and keeps the whole
 * application in the foreground.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class CoreService extends Service {
    private static final int FOREGROUND_NOTIFICATION_ID = 21032020;
    private static final String TAG = CoreService.class.getSimpleName();


    public static void init(Context context) {
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(lifecycleCallbacks);
        RestartWorker.schedulePeriodicRestart(context.getApplicationContext());
    }

    public static void launch(Context context) {
        if (isRunning(context)) return;
        Log.i(TAG, "launch");
        /*
        Prior to Android 8.0, the usual way to create a foreground service was to create a
        background service, then promote that service to the foreground. With Android 8.0,
        there is a complication; the system doesn't allow a background app to create a
        background service. For this reason, Android 8.0 introduces the new method
        startForegroundService() to start a new service in the foreground. After the system
        has created the service, the app has five seconds to call the service's
        startForeground() method to show the new service's user-visible notification.
        If the app does not call startForeground() within the time limit, the system stops
        the service and declares the app to be ANR.
        https://developer.android.com/about/versions/oreo/background
        and
        https://developer.android.com/reference/android/content/Context#startForegroundService(android.content.Intent)
         */
        Intent intent = new Intent(context, CoreService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent);
        else
            context.startService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            runAsAForegroundService();
        }
    }

    /*
    A started service can use the startForeground(int, android.app.Notification) API to put the
    service in a foreground state, where the system considers it to be something the user is
    actively aware of and thus not a candidate for killing when low on memory. (It is still
    theoretically possible for the service to be killed under extreme memory pressure from the
    current foreground application, but in practice this should not be a concern.
    https://developer.android.com/reference/android/app/Service#startForeground(int,%20android.app.Notification)
     */
    private void runAsAForegroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "restarting ForegroundService");
            try {
                Notification no = ProximityTracingService.getInstance().getForegroundServiceNotification();
                if (no != null) {
                    startForeground(FOREGROUND_NOTIFICATION_ID, no);
                    Log.i(TAG, "ForegroundService started successfully");
                } else {
                    Log.e(TAG, "user callback: foreground service notification is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in notification " + e.getMessage());
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RestartWorker.scheduleOneTimeRestart(this, RestartWorker.TEN_SECS);
        Log.i(TAG, "onDestroy");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "onStartCommand");
        UncaughtExceptionWatcher.set(getApplicationContext());
        return START_STICKY;
    }

    private static boolean isRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (CoreService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
        }
        return false;
    }

    // ActivityLifecycleCallbacks
    private static Application.ActivityLifecycleCallbacks lifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {
        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            RestartWorker.scheduleOneTimeRestartNow(activity);
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {
        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {
        }
    };
}
