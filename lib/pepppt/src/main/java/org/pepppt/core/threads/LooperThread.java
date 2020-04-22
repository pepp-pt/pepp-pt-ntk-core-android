package org.pepppt.core.threads;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RestrictTo;

/**
 * This Handler
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class LooperThread {
    private Handler coreHandler;
    private Looper looper;

    public void create() {
        createThread();
    }

    public void post(Runnable runnable) {
        coreHandler.post(runnable);
    }

    public void postDelayed(Runnable r, long delayMillis) {
        coreHandler.postDelayed(r, delayMillis);
    }

    public void quit() {
        looper.quitSafely();
    }

    private void createThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                looper = Looper.myLooper();
                coreHandler = new Handler();
                Looper.loop();
            }
        };
        thread.start();
    }
}
