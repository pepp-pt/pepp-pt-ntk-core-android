package org.pepppt.core.ble.aggregation;

import android.util.Log;

import androidx.annotation.RestrictTo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of the measurement aggregator. The aggregator will calculate following values:
 * <ul>
 * <li>Number of measurements that have benn aggregated {@link AggregationResult.AggregationData#getCountOfObservations()}</li>
 * <li>Mean of RSSI values {@link AggregationResult.AggregationData#getMeanRssi()}</li>
 * <li>Variance of RSSI values {@link AggregationResult.AggregationData#getRssiVariance()}</li>
 * <li>Mean of TX Level Values {@link AggregationResult.AggregationData#getMeanTxLevel()}</li>
 * <li>Mean of TX Correction Level values {@link AggregationResult.AggregationData#getMeanTxCorrectionLevel()}</li>
 * </ul>
 * <p>
 * Example: call process after each measurement
 * <pre>
 *   {@code
 *      final Aggregator<UUID> aggregator = new Aggregator<>();
 *      aggregator.submit(new Measurement<>(UUID.randomUUID(), System.nanoTime(), -83, -7, 10));
 *      aggregator.submit(new Measurement<>(UUID.randomUUID(), System.nanoTime(), -77, -7, -10));
 *
 *      for (final AggregationResult<UUID> result : aggregator.process()) {
 *          final long startTime = result.getTimeNanosStart();
 *          final Map<UUID, AggregationResult.AggregationData> aggregatedData = result.getData();
 *
 *          for (final Map.Entry<UUID, AggregationResult.AggregationData> entry : aggregatedData.entrySet()) {
 *              final UUID senderId = entry.getKey();
 *              final int rssiVariance = entry.getValue().getRssiVariance();
 *              final int meanRssi = entry.getValue().getMeanRssi();
 *              final int meanTxLevel = entry.getValue().getMeanTxLevel();
 *              final int meanTxCorrectionLevel = entry.getValue().getMeanTxCorrectionLevel();
 *
 *              //store it, e.g.: storeInDatabase(startTime, senderId, rssiVariance, meanRssi, meanTxLevel, meanTxCorrectionLevel);
 *          }
 *      }
 *   }
 * </pre>
 *
 * @param <T> the concrete type of the identifier that measurements are associated to.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Aggregator<T> {

    private final int aggregationTime;

    private final TimeUnit timeUnit;

    private final Deque<Measurement<T>> measurementDeque = new ArrayDeque<>();

    private long currentIntervalEnd = -1;

    /**
     * Creates a new measurement aggregator that will operate with a default time window of 10 seconds.
     */
    public Aggregator() {
        this(10, TimeUnit.SECONDS);
    }

    /**
     * Creates a new measurement aggregator that will operate with the given aggregation time and time unit.
     *
     * @param aggregationTime the size of the aggregation time window, must be > 0.
     * @param timeUnit        the time unit for the aggregation time window
     */
    public Aggregator(final int aggregationTime, final TimeUnit timeUnit) {
        if (timeUnit.toNanos(aggregationTime) <= 0) {
            throw new IllegalArgumentException("Aggregation interval must be > 0");
        }

        this.aggregationTime = aggregationTime;
        this.timeUnit = timeUnit;
    }

    /**
     * Submits the specified measurement data to the aggregator.
     *
     * @param measurement the measurement to be submitted for aggregation
     * @return true when the data was submitted successfully; false otherwise
     */
    public boolean submit(final Measurement<T> measurement) {
        final boolean success = measurementDeque.offer(measurement);

        if (!success) {
            Log.e(getClass().getSimpleName(),
                    "Failed to add scan result to queue! Maybe the queue is full?!");
        }
        return success;
    }

    /**
     * Process all data in the queue that lies in a completed aggregation interval.
     *
     * @return a list with an entry for each aggregation interval that is completed until now.
     */
    public List<AggregationResult<T>> process() {
        return process(System.nanoTime());
    }

    /**
     * Same as {@link #process()}, but process all intervals that are completed until the given time.
     *
     * @param currentTimeNanos the latest interval end time that is processed.
     * @return a list with an entry for each aggregation interval that is completed until now.
     */
    public List<AggregationResult<T>> process(final long currentTimeNanos) {
        final List<AggregationResult<T>> aggregationResultList = new ArrayList<>();

        if (currentIntervalEnd < 0) {
            // First run - initialize currentIntervalEnd
            final Measurement<T> oldest = measurementDeque.peekFirst();
            if (oldest != null) {
                currentIntervalEnd = oldest.getTimestamp() + aggregationTimeNanos();
            } else {
                return aggregationResultList;
            }
        }

        // process in intervals - stop if there are no entries left or if the interval is not yet completed
        while (!measurementDeque.isEmpty() && currentIntervalEnd < currentTimeNanos) {
            final AggregationResult<T> result = processCurrentInterval();
            if (!result.getData().isEmpty()) {
                aggregationResultList.add(result);
            }
            currentIntervalEnd += aggregationTimeNanos();
        }

        return aggregationResultList;
    }

    /**
     * Take all values out of the queue that fit into the current interval and aggregate them.
     *
     * @return an result containing the aggregated result plus information about the interval.
     */
    private AggregationResult<T> processCurrentInterval() {
        final List<Measurement<T>> measurements = new ArrayList<>();
        while (!measurementDeque.isEmpty()) {
            final Measurement<T> nextEntry = measurementDeque.peekFirst();
            if (nextEntry != null && nextEntry.getTimestamp() < currentIntervalEnd) {
                measurements.add(measurementDeque.pop());
            } else {
                break;
            }
        }
        AggregationResult<T> result = new AggregationResult<>(currentIntervalEnd - aggregationTimeNanos(), aggregationTimeNanos());
        aggregate(measurements, result);
        return result;
    }

    /**
     * @return the aggregation time in nanoseconds.
     */
    private long aggregationTimeNanos() {
        return TimeUnit.NANOSECONDS.convert(aggregationTime, timeUnit);
    }

    /**
     * Batch aggregation. Aggregates the given list of measurements directly into an aggregation result.
     *
     * @param measurements the list of measurements to be aggregated
     * @param <T>          the concrete type of the identifier
     * @return the aggregated data
     */
    public static <T> AggregationResult<T> aggregate(final List<Measurement<T>> measurements) {
        long start = -1;
        long duration = 0;
        if (measurements.size() > 0) {
            start = measurements.get(0).getTimestamp();
            duration = measurements.get(measurements.size() - 1).getTimestamp() - start;
        }
        final AggregationResult<T> aggregationResult = new AggregationResult<>(start, duration);
        aggregate(measurements, aggregationResult);
        return aggregationResult;
    }

    /**
     * Version of {@link Aggregator#aggregate(List)} that modifies an aggregation result.
     */
    private static <T> void aggregate(final List<Measurement<T>> measurements, final AggregationResult<T> modifiedResult) {
        for (final Map.Entry<T, List<Measurement<T>>> entry : groupMeasurementsPerId(measurements).entrySet()) {
            final List<Measurement<T>> measurementsForId = entry.getValue();

            // collect values to be aggregated
            final List<Integer> rssiValues = extractValues(measurementsForId, Measurement::getRssi);
            final List<Integer> txLevels = extractValues(measurementsForId, Measurement::getTxLevel);
            final List<Integer> txCorrectionLevels = extractValues(measurementsForId, Measurement::getTxCorrectionLevel);

            // aggregate the values
            final int meanRssi = Calculator.calculateMean(rssiValues);
            modifiedResult.add(
                    entry.getKey(),
                    meanRssi,
                    Calculator.calculcateVariance(rssiValues, meanRssi),
                    Calculator.calculateMean(txLevels),
                    Calculator.calculateMean(txCorrectionLevels),
                    measurementsForId.size()
            );
        }
    }

    private static <T> Map<T, List<Measurement<T>>> groupMeasurementsPerId(final List<Measurement<T>> measurements) {
        final Map<T, List<Measurement<T>>> id2scanResultsMap = new HashMap<>();

        for (final Measurement<T> measurement : measurements) {
            final T id = measurement.getId();
            List<Measurement<T>> rssiValues = id2scanResultsMap.get(id);

            if (rssiValues == null) {
                rssiValues = new ArrayList<>();
                id2scanResultsMap.put(id, rssiValues);
            }
            rssiValues.add(measurement);
        }
        return id2scanResultsMap;
    }

    private static <T> List<Integer> extractValues(final List<Measurement<T>> measurements, final ValueExtractor<T> extractor) {
        final ArrayList<Integer> results = new ArrayList<>();
        for (final Measurement<T> measurement : measurements) {
            results.add(extractor.extractValue(measurement));
        }
        return results;
    }

    /**
     * Extract an integer value out of a measurement
     *
     * @param <T>
     */
    private interface ValueExtractor<T> {
        Integer extractValue(Measurement<T> measurement);
    }
}
