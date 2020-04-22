package org.pepppt.core.database;

import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
class EncounterJsonHelper {
    public static String createEncounterJson(String receiverid, String transmitterid,
                                             int tx, int rss, long timestamp, String model,
                                             String swScanMode,
                                             String swScanRate,
                                             String swScanActive,
                                             String swAdMode,
                                             String swAdRate,
                                             String swAdActive,
                                             String swAdPower) {
        return new EncounterJson(receiverid, transmitterid,
                String.valueOf(tx),
                String.valueOf(rss),
                String.valueOf(timestamp), model,
                swScanMode,
                swScanRate,
                swScanActive,
                swAdMode,
                swAdRate,
                swAdActive,
                swAdPower
                ).createJson();
    }
}
