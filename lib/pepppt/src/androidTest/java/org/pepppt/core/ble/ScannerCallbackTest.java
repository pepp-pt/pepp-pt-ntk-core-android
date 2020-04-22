package org.pepppt.core.ble;

import org.junit.Test;
import org.pepppt.core.ble.ScannerCallback;

import java.util.UUID;

import static org.junit.Assert.*;

public class ScannerCallbackTest {

    @Test
    public void byteArrayToHex() {
        byte[] bytearray = new byte[] { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47, 0x48 };
        String expected = "4142434445464748";
        String s = ScannerCallback.byteArrayToHex(bytearray);
        assertEquals(s, expected);
    }

    @Test
    public void stringTest1() {
        String s1 = "842c61d96afb20407c4bf408135ec393";
        UUID uuid1 = UUID.fromString("842c61d9-6afb-2040-7c4b-f408135ec393");
        UUID uuid2 = ScannerCallback.createUUIDFromByteString(s1);
        assertEquals(uuid1.toString(), uuid2.toString());
    }

}