package org.pepppt.core.ble.aggregation;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AggregatorTest {

    /**
     * The RSSI values to be aggregated.
     */
    private static final int[] RSSI_VALUES = new int[]{-30, -40, -50, -60, -70, -80, -90};

    /**
     * Random unique app identifier.
     */
    private static final UUID SENDER_ID = UUID.randomUUID();


    /**
     * The expected outcome of the RSSI mean calculation when aggregating all measurements.
     */
    private static final int EXPECTED_MEAN = -60;

    /**
     * The expected outcome of the txLevel mean calculation when aggregating all measurements.
     */
    private static final int EXPECTED_TX_LEVEL_MEAN = -30;

    /**
     * The expected outcome of the tcCorrectionLevel calculation when aggregating all measurements.
     */
    private static final int EXPECTED_TX_CORRECTION_LEVEL_MEAN = 30;

    /**
     * The expected outcom of the observation count when aggregating all measurements.
     */
    private static final int EXPECTED_OBSERVATION_COUNT = RSSI_VALUES.length;

    /**
     * The expected outcome of the variance calculation.
     */
    private static final int EXPECTED_VARIANCE = 400;

    private static final int AGGREGATION_INTERVAL = 10;

    /**
     * Tests the batch aggregation.
     */
    @Test
    public void aggregate() {
        final ArrayList<Measurement<UUID>> list = new ArrayList<>();

        for (int rssiValue : RSSI_VALUES) {
            list.add(new Measurement<>(SENDER_ID, System.nanoTime(), rssiValue, rssiValue + 30, rssiValue + 90));
        }

        final AggregationResult<UUID> result = Aggregator.aggregate(list);
        checkResults(result);
    }

    /**
     * Tests the sequential aggregation.
     */
    @Test
    public void submitAndProcessAll() {

        // pushing 7 measurements - 1 every second
        final Aggregator<UUID> aggregator = new Aggregator<>(AGGREGATION_INTERVAL, TimeUnit.SECONDS);
        final long startTime = System.nanoTime();
        final long lastSubmit = submit(aggregator, secondsInNanos(1), startTime);

        // processing after =~ 7 seconds should not lead to any results as the interval is not completed yet
        final List<AggregationResult<UUID>> results1 = aggregator.process(lastSubmit);
        Assert.assertEquals(0, results1.size());

        // processing after =~ 11 seconds should lead to one 10s interval being processed
        final List<AggregationResult<UUID>> results2 = aggregator.process(startTime + secondsInNanos(11));
        Assert.assertEquals(1, results2.size());
        checkResults(results2.get(0));

    }

    /**
     * Tests the sequential aggregation if processing parts of the submitted data.
     */
    @Test
    public void submitAndProcessParts() {

        // pushing 7 measurements - 1 every 2 seconds
        final Aggregator<UUID> aggregator = new Aggregator<>(AGGREGATION_INTERVAL, TimeUnit.SECONDS);
        final long startSubmitting = System.nanoTime();
        final long lastSubmit = submit(aggregator, secondsInNanos(2), startSubmitting);

        // processing after =~ 11 seconds should not lead to any results as the interval is not completed yet
        final List<AggregationResult<UUID>> results1 = aggregator.process(startSubmitting + secondsInNanos(11));
        Assert.assertEquals(1, results1.size());

        // processing after long time should lead to one 10s interval being processed
        final List<AggregationResult<UUID>> results2 = aggregator.process(lastSubmit + secondsInNanos(500));
        Assert.assertEquals(1, results2.size());

    }

    /**
     * Tests the sequential aggregation if measurement rate is lower than aggregation time.
     */
    @Test
    public void submitAndProcessLowRateData() {

        // pushing 7 measurements - 1 every 20 seconds and aggregating in 10s interval
        final Aggregator<UUID> aggregator = new Aggregator<>(AGGREGATION_INTERVAL, TimeUnit.SECONDS);
        final long startTime = System.currentTimeMillis();
        final long lastSubmit = submit(aggregator, secondsInNanos(20), startTime);

        // processing long after all measurements have been added
        final List<AggregationResult<UUID>> results = aggregator.process(lastSubmit + secondsInNanos(1000));
        Assert.assertEquals(7, results.size());

        for (int index = 0; index < results.size() && index < RSSI_VALUES.length; index++) {
            final AggregationResult<UUID> aggregationResult = results.get(index);
            Assert.assertNotNull(aggregationResult);
            Assert.assertEquals(secondsInNanos(AGGREGATION_INTERVAL), aggregationResult.getTimeNanoDuration());
            Assert.assertEquals(startTime + index * 2 * secondsInNanos(AGGREGATION_INTERVAL), aggregationResult.getTimeNanosStart());
            final AggregationResult.AggregationData aggregationData = aggregationResult.getData().get(SENDER_ID);
            Assert.assertNotNull(aggregationData);
            Assert.assertEquals(RSSI_VALUES[index], aggregationData.getMeanRssi());
            Assert.assertEquals(0, aggregationData.getRssiVariance());
            Assert.assertEquals(1, aggregationData.getCountOfObservations());
        }

    }

    @Test
    public void aggregatorExampleUsage() {
        final Aggregator<UUID> aggregator = new Aggregator<>();
        aggregator.submit(new Measurement<>(UUID.randomUUID(), System.nanoTime(), -83, -7, 10));
        aggregator.submit(new Measurement<>(UUID.randomUUID(), System.nanoTime(), -77, -7, -10));

        for (final AggregationResult<UUID> result : aggregator.process()) {
            final long startTime = result.getTimeNanosStart();
            final Map<UUID, AggregationResult.AggregationData> aggregatedData = result.getData();

            for (final Map.Entry<UUID, AggregationResult.AggregationData> entry : aggregatedData.entrySet()) {
                final UUID senderId = entry.getKey();
                final int rssiVariance = entry.getValue().getRssiVariance();
                final int meanRssi = entry.getValue().getMeanRssi();
                final int meanTxLevel = entry.getValue().getMeanTxLevel();
                final int meanTxCorrectionLevel = entry.getValue().getMeanTxCorrectionLevel();

                //store it, e.g.: storeInDatabase(startTime, senderId, rssiVariance, meanRssi, meanTxLevel, meanTxCorrectionLevel);
            }
        }
    }

    private long submit(final Aggregator<UUID> aggregator, final long timeStepNanos, long startTimeNanos) {

        for (final int rssiValue : RSSI_VALUES) {
            Assert.assertTrue(aggregator.submit(new Measurement<>(SENDER_ID, startTimeNanos, rssiValue, rssiValue + 30, rssiValue + 90)));
            startTimeNanos += timeStepNanos;
        }
        return startTimeNanos;
    }

    private void checkResults(final AggregationResult<UUID> aggregationResult) {
        Assert.assertFalse("Aggregation result was empty!", aggregationResult.getData().isEmpty());

        final AggregationResult.AggregationData aggregationData = aggregationResult.getData().get(SENDER_ID);

        Assert.assertNotNull("Aggregation data was null!", aggregationData);


        Assert.assertEquals("Expected and calculated mean do not match!",
                EXPECTED_MEAN, aggregationData.getMeanRssi());

        Assert.assertEquals("Expected and calculated variance do not match!",
                EXPECTED_VARIANCE, aggregationData.getRssiVariance());

        Assert.assertEquals(EXPECTED_TX_LEVEL_MEAN, aggregationData.getMeanTxLevel());
        Assert.assertEquals(EXPECTED_TX_CORRECTION_LEVEL_MEAN, aggregationData.getMeanTxCorrectionLevel());
        Assert.assertEquals(EXPECTED_OBSERVATION_COUNT, aggregationData.getCountOfObservations());
    }

    private long secondsInNanos(final int seconds) {
        return TimeUnit.NANOSECONDS.convert(seconds, TimeUnit.SECONDS);
    }
}