package org.pepppt.core.service;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.concurrent.TimeUnit;

import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.ListenableWorker;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * This class helps to stabilize the apps.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class RestartWorker extends ListenableWorker {

    private static String TAG = RestartWorker.class.getSimpleName();

    private static final String RESTART_PERIODIC_WORKER_NAME = "org.pepppt.core.worker.restart.periodic";
    private static final String RESTART_ONE_TIME_WORKER_NAME = "org.pepppt.core.worker.restart.onetime";
    private static final String LABEL = "label";

    public static final long TEN_SECS = 10L * 1000L;

    public static void schedulePeriodicRestart(Context context) {
        Log.i(TAG, "schedulePeriodicRestart");
        Data data = new Data.Builder()
                .putString(LABEL, RESTART_PERIODIC_WORKER_NAME)
                .build();
        PeriodicWorkRequest work = new PeriodicWorkRequest.Builder(RestartWorker.class, 30, TimeUnit.MINUTES)
                .setConstraints((new Constraints.Builder()).build())
                .setInitialDelay(30, TimeUnit.MINUTES)
                .setInputData(data)
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(RESTART_PERIODIC_WORKER_NAME, ExistingPeriodicWorkPolicy.REPLACE, work);
    }

    public static void cancelPeriodicRestart(Context context) {
        Log.i(TAG, "cancelPeriodicRestart");
        WorkManager.getInstance(context).cancelUniqueWork(RESTART_PERIODIC_WORKER_NAME);
    }

    public static void scheduleOneTimeRestartNow(Context context) {
        scheduleOneTimeRestart(context, 0);
    }

    public static void scheduleOneTimeRestart(Context context, long delay) {
        Log.i(TAG, "scheduleRestart in " + delay / 1000 + " s");
        Data data = new Data.Builder()
                .putString(LABEL, RESTART_ONE_TIME_WORKER_NAME)
                .build();
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(RestartWorker.class)
                .setConstraints((new Constraints.Builder()).build())
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(RESTART_ONE_TIME_WORKER_NAME, ExistingWorkPolicy.REPLACE, work);
    }

    public static void cancelOneTimeRestart(Context context) {
        Log.i(TAG, "cancelRestart");
        WorkManager.getInstance(context).cancelUniqueWork(RESTART_ONE_TIME_WORKER_NAME);
    }

    public RestartWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        final String lbl = getInputData().getString(LABEL);
        Log.i(TAG, "startWork begin: " + lbl);
        return CallbackToFutureAdapter.getFuture(completer -> {
            CoreService.launch(getApplicationContext());
            //let some time to the system to start the service
            // it is safe to wait here for a few seconds,
            // the system won't kill us since it is designed to execute async code
            new Handler().postDelayed(() -> {
                Log.i(TAG, "startWork end: " + lbl);
                completer.set(Result.success());
            }, 30000);
            return null;
        });
    }
}
