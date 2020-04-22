package org.pepppt.core.database;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
@RestrictTo(RestrictTo.Scope.LIBRARY)
class AggregationJson {
    private long startTime;
    private String senderId;
    private int rssiVariance;
    private int meanRssi;
    private int txCorrectionValue;
    private int countOfObservation;
    private int meanTx;

    AggregationJson(
            long startTime,
            String senderId,
            int rssiVariance,
            int meanRssi,
            int txCorrectionValue,
            int countOfObservation,
            int meanTx) {
        this.startTime = startTime;
        this.senderId = senderId;
        this.rssiVariance = rssiVariance;
        this.meanRssi = meanRssi;
        this.txCorrectionValue = txCorrectionValue;
        this.countOfObservation = countOfObservation;
        this.meanTx = meanTx;
    }

    public String createJson() {
        return new Gson().toJson(this);
    }
}
