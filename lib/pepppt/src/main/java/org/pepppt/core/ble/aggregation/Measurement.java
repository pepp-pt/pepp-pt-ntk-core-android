package org.pepppt.core.ble.aggregation;

import androidx.annotation.RestrictTo;

/**
 * Represents the measurement of a single RSSI value of a received message.
 *
 * @param <T> the concrete type of the sender identifier
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class Measurement<T> {

    private final long timestamp;

    private final T id;

    private final int rssi;

    private final int txLevel;

    private final int txCorrectionLevel;

    /**
     * Create a new measurement.
     *
     * @param id                is the identifier of the sender device that this rssi value is associated to.
     * @param timestamp         is the time of the measurement as timestamp in nanosecons.
     * @param rssi              is the measured value of received signal strength.
     * @param txLevel           is the transmit power level of the sending device.
     * @param txCorrectionLevel is the dynamic correction factor for the tx level.
     */
    public Measurement(final T id,
                       final long timestamp,
                       final int rssi,
                       final int txLevel,
                       final int txCorrectionLevel) {

        this.id = id;
        this.timestamp = timestamp;
        this.rssi = rssi;
        this.txLevel = txLevel;
        this.txCorrectionLevel = txCorrectionLevel;
    }

    public T getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getRssi() {
        return rssi;
    }

    public int getTxLevel() {
        return txLevel;
    }

    public int getTxCorrectionLevel() {
        return txCorrectionLevel;
    }
}
