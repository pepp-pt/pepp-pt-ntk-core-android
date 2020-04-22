package org.pepppt.core.tan;

/**
 * This class represents a TAN that can be requested by ProximityTracingService.requestTan.
 */
public class Tan {
    private String tanData;
    private long expiration;

    /**
     * Returns the TAN data.
     * @return The TAN data.
     */
    public String getTanData() {
        return tanData;
    }

    /**
     * Returns the expiration time of the TAN.
     * @return The expiration time.
     */
    public long getExpiration() {
        return expiration;
    }

    private Tan() {
    }

    public Tan(String data, long expiration) {
        tanData = data;
        this.expiration = expiration;
    }
}
