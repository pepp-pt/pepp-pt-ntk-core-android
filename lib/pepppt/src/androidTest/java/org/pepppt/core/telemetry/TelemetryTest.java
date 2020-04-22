package org.pepppt.core.telemetry;

import android.util.Log;

import org.junit.Test;
import org.pepppt.core.exceptions.InvalidBackendEndpointException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TelemetryTest {
    private static final String TAG = Telemetry.class.getSimpleName();

    @Test
    public void updateExceptionListTest() {
        Exception ex = new InvalidBackendEndpointException("ulli");
        assertTrue(Telemetry.updateExceptionList(ex));
        assertFalse(Telemetry.updateExceptionList(ex));
    }

    @Test
    public void testStackTrace() {
        try {
            nested1();
        } catch (InvalidBackendEndpointException e) {
            String s = Telemetry.getStripedStackTrace(e.getStackTrace());
            Log.i(TAG, s);
        }
        Log.i(TAG, "exit");
    }

    private void nested1() throws InvalidBackendEndpointException {
        nested2();
    }

    private void nested2() throws InvalidBackendEndpointException {
        nested3();
    }

    private void nested3() throws InvalidBackendEndpointException {
        nested4();
    }

    private void nested4() throws InvalidBackendEndpointException {
        throw new InvalidBackendEndpointException("ulli");
    }
}

