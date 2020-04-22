package org.pepppt.core.util;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to measure and log the time between reoccurring events.
 */
class Measurement {
    final private static Map<String,Long> myMap = new HashMap<>();

    public static void logLap(String tag, String id, int warningspan) {
        long span = Measurement.getLap(id);
        Log.i(tag, id + ": lap (last " + span + "ms ago)");
        if (span > warningspan)
            Log.w(tag, "WARNING: took too long");
    }

    public static void logLap(String tag, String id) {
        long span = Measurement.getLap(id);
        Log.i(tag, id + ": lap (last " + span + "ms ago)");
    }

    private static long getLap(String str) {
        long current = System.currentTimeMillis();
        long lastrun;
        if( myMap.containsKey(str)) {
            lastrun = myMap.get(str);
            myMap.put(str, current);
        }
        else {
            myMap.put(str, current);
            lastrun = current;
        }
        return current - lastrun;
    }

}
