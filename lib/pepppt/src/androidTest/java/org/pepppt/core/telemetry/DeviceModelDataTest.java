package org.pepppt.core.telemetry;

import org.junit.Test;

import static org.junit.Assert.*;

public class DeviceModelDataTest {

    private final static String string1 = "Test";
    private final static String string2 = "1234567890";
    private final static String string2_expect = "12345";

    @Test
    public void testSafeString() {
        String safe1 = KPIAuxData.convertToSafeString(string1, 10);
        assertEquals(safe1, string1);
        String safe2 = KPIAuxData.convertToSafeString(string2, 5);
        assertEquals(safe2, string2_expect);
        String safe3 = KPIAuxData.convertToSafeString(string2, 10);
        assertEquals(safe3, string2);
    }
}