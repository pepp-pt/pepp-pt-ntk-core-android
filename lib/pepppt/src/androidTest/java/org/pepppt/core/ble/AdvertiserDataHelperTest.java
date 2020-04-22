package org.pepppt.core.ble;

import org.junit.Test;
import org.pepppt.core.util.UuidHelper;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class AdvertiserDataHelperTest {
    private static final String ebid1 = "f39a9330-1eda-eaf6-58ae-3c3b5106a64d";

    @Test
    public void ebidTest1() {
        UUID uuid1 = UUID.fromString(ebid1);
        byte[] data = UuidHelper.getBytesFromUUID(uuid1);
        UUID ebidc1 = UuidHelper.getUUIDFromBytes(data);
        assertEquals(ebid1, ebidc1.toString());
    }

    @Test
    public void ebidTest3() {
        String s1 = "32c4b3f2376418d58b5c4ff4662e254d";
        UUID uuid1expect = UUID.fromString("32c4b3f2-3764-18d5-8b5c-4ff4662e254d");
        UUID uuid1 = UUID.fromString(s1);
        assertEquals(uuid1.toString(), uuid1.toString());
    }

}