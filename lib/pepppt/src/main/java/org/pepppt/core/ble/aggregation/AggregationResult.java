package org.pepppt.core.ble.aggregation;

import androidx.annotation.RestrictTo;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Storage for aggregation results that are associated to identifiers with type {@link T}.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AggregationResult<T> {

    private final Map<T, AggregationData> dataMap = new HashMap<>();
    private final long timeNanosStart;
    private final long timeNanoDuration;

    public AggregationResult(final long timeNanosStart, final long timeNanoDuration) {
        this.timeNanoDuration = timeNanoDuration;
        this.timeNanosStart = timeNanosStart;
    }


    /**
     * Retreive a map that associates an identifier to the latest aggregation result.
     */
    public Map<T, AggregationData> getData() {
        return dataMap;
    }

    /**
     * Add the result of the aggregation of one time series of RSSI values.
     */
    void add(final T id,
             final int meanRssi,
             final int rssiVariance,
             final int meanTxLevel,
             final int meanTxCorrectionLevel,
             final int countOfObservations) {

        dataMap.put(id, new AggregationData(
                meanRssi,
                rssiVariance,
                meanTxLevel,
                meanTxCorrectionLevel,
                countOfObservations)
        );
    }

    public long getTimeNanosStart() {
        return timeNanosStart;
    }

    public long getTimeNanoDuration() {
        return timeNanoDuration;
    }

    /**
     * Data class to store the result of an aggregation process for one series of RSSI values.
     */
    public static class AggregationData {
        private final int rssiVariance;
        private final int meanRssi;

        private final int meanTxLevel;
        private final int meanTxCorrectionLevel;
        private final int countOfObservations;

        private AggregationData(final int meanRssi,
                                final int rssiVariance,
                                final int meanTxLevel,
                                final int meanTxCorrectionLevel,
                                final int countOfObservations) {

            this.meanRssi = meanRssi;
            this.rssiVariance = rssiVariance;
            this.meanTxLevel = meanTxLevel;
            this.meanTxCorrectionLevel = meanTxCorrectionLevel;
            this.countOfObservations = countOfObservations;
        }

        /**
         * @return the calculated mean of all rssi values in {@link Measurement#Measurement(Object, long, int, int, int)}.
         */
        public int getMeanRssi() {
            return meanRssi;
        }

        /**
         * @return the variance of all rssi values in {@link Measurement#Measurement(Object, long, int, int, int)}.
         */
        public int getRssiVariance() {
            return rssiVariance;
        }

        /**
         * @return the mean of all txLevel values in {@link Measurement#Measurement(Object, long, int, int, int)}.
         */
        public int getMeanTxLevel() {
            return meanTxLevel;
        }

        /**
         * @return the mean of all txCorrectionLevel values in {@link Measurement#Measurement(Object, long, int, int, int)}.
         */
        public int getMeanTxCorrectionLevel() {
            return meanTxCorrectionLevel;
        }

        /**
         * @return the number of {@link Measurement}s that have been aggregated.
         */
        public int getCountOfObservations() {
            return countOfObservations;
        }

        @NotNull
        @Override
        public String toString() {
            return String.format(Locale.ENGLISH,
                    "(variance = %d, mean = %d)",
                    rssiVariance, meanRssi);
        }
    }
}
