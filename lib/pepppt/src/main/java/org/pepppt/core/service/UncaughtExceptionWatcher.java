package org.pepppt.core.service;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

import org.pepppt.core.telemetry.KPI;
import org.pepppt.core.telemetry.Telemetry;

import java.lang.ref.WeakReference;

/**
 * This class catches all exception that have not been catched anywhere else.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class UncaughtExceptionWatcher implements Thread.UncaughtExceptionHandler {

    private static final String LOG_AREA = UncaughtExceptionWatcher.class.getSimpleName();

    private WeakReference<Context> wContext;
    private Thread.UncaughtExceptionHandler oldHandler;

    public static void set(Context ctx) {
        try {
            Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionWatcher(ctx));
        } catch (Exception e) {
            Log.e(LOG_AREA, "Failed to set exception handler", e);
        }
    }

    private UncaughtExceptionWatcher(Context ctx) {
        this.wContext = new WeakReference<>(ctx);
        this.oldHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        Log.e(LOG_AREA, "Uncaught exception", e);
        Telemetry.sendKPI(new KPI(e));
        Context ctx = wContext.get();
        if (ctx != null) {
            RestartWorker.scheduleOneTimeRestart(ctx, RestartWorker.TEN_SECS);
        }
        oldHandler.uncaughtException(t, e);
    }
}
