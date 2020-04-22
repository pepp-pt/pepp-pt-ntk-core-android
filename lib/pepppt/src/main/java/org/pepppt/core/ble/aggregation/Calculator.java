package org.pepppt.core.ble.aggregation;

import androidx.annotation.RestrictTo;

import java.util.List;

/**
 * Collection of the pure mathematic functions used to aggregate.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
class Calculator {

    private Calculator() {
    }

    static int calculcateVariance(final List<Integer> rssiValues, final int meanRssi) {
        int diffSum = 0;

        for (final int rssi : rssiValues) {
            diffSum += Math.pow(rssi - meanRssi, 2);
        }

        return Math.round((float) diffSum / rssiValues.size());
    }

    static int calculateMean(final List<Integer> rssiValues) {
        int rssiSum = 0;

        for (final int rssi : rssiValues) {
            rssiSum += rssi;
        }

        return Math.round((float) rssiSum / rssiValues.size());
    }
}
