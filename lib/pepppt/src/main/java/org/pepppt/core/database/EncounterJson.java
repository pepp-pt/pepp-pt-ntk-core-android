package org.pepppt.core.database;

import androidx.annotation.RestrictTo;

import com.google.gson.Gson;

@SuppressWarnings("FieldCanBeLocal")
@RestrictTo(RestrictTo.Scope.LIBRARY)
class EncounterJson {
    private String receiverid;
    private String transmitterid;
    private String tx;
    private String rss;
    private String timestamp;
    private String model;
    private String swScanMode1;
    private String swScanRate2;
    private String swScanActive;
    private String swAdMode3;
    private String swAdRate;
    private String swAdActive4;
    private String swAdPower5;

    public EncounterJson(String receiverid,
                         String transmitterid,
                         String txpower,
                         String rss,
                         String timestamp,
                         String model,
                         String swScanMode,
                         String swScanRate,
                         String swScanActive,
                         String swAdMode,
                         String swAdRate,
                         String swAdActive,
                         String swAdPower) {
        this.tx = txpower;
        this.rss = rss;
        if (receiverid != null)
            this.receiverid = receiverid;
        else
            this.receiverid = "<not known yet>";
        this.transmitterid = transmitterid;
        this.timestamp = timestamp;
        this.model = model;
        this.swScanMode1 = swScanMode;
        this.swScanRate2 = swScanRate;
        this.swScanActive = swScanActive;
        this.swAdMode3 = swAdMode;
        this.swAdRate = swAdRate;
        this.swAdActive4 = swAdActive;
        this.swAdPower5 = swAdPower;
    }

    public String createJson() {
        return new Gson().toJson(this);
    }
}
