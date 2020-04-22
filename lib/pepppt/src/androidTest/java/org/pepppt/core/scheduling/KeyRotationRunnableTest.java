package org.pepppt.core.scheduling;

import org.junit.Test;
import org.pepppt.core.storage.EBID;
import org.pepppt.core.util.Units;

import java.util.UUID;

import static org.junit.Assert.*;

public class KeyRotationRunnableTest {

    @Test
    public void needsRotation() {
        long now = System.currentTimeMillis();

        //
        // ebid is about to expire in 5 minutes => no rotation needed.
        //
        assertFalse(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now + 5 * Units.MINUTES),
                now,
                now - 1 * Units.HOURS,
                30 * Units.MINUTES, 60 * Units.SECONDS));

        //
        // ebid is about to expire in 2 minutes => no rotation needed.
        //
        assertFalse(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now + 2 * Units.MINUTES),
                now,
                now - 1 * Units.HOURS,
                30 * Units.MINUTES, 60 * Units.SECONDS));

        //
        // ebid is about to expire in 1 minute => rotation is needed.
        //
        assertTrue(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now + 1 * Units.MINUTES),
                now,
                now - 1 * Units.HOURS,
                30 * Units.MINUTES, 60 * Units.SECONDS));

        //
        // ebid is about to expire now => rotation is needed.
        //
        assertTrue(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now),
                now,
                now - 1 * Units.HOURS,
                30 * Units.MINUTES, 60 * Units.SECONDS));

        //
        // ebid has expired => rotation is needed.
        //
        assertTrue(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now - 3 * Units.MINUTES),
                now,
                now - 1 * Units.HOURS,
                30 * Units.MINUTES, 60 * Units.SECONDS));

        //
        // ebid has expired => rotation is needed.
        //
        assertTrue(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now - 12 * Units.HOURS),
                now,
                now - 1 * Units.HOURS,
                30 * Units.MINUTES, 60 * Units.SECONDS));

        //
        // ebid has expired => rotation is needed.
        //
        assertTrue(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now - 100 * Units.HOURS),
                now,
                now - 1 * Units.HOURS,
                30 * Units.MINUTES, 60 * Units.SECONDS));


        //
        // ebid has expired but it still in the rest period => no rotation allowed
        //
        assertFalse(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now - 100 * Units.HOURS),
                now,
                now - 15 * Units.MINUTES,
                30 * Units.MINUTES, 60 * Units.SECONDS));

        //
        // ebid has expired but it still in the rest period => no rotation allowed
        //
        assertFalse(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now - 100 * Units.HOURS),
                now,
                now - 28 * Units.MINUTES,
                30 * Units.MINUTES, 60 * Units.SECONDS));

        //
        // ebid has expired but it still in the rest period => no rotation allowed
        //
        assertFalse(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now - 100 * Units.HOURS),
                now,
                now - 29 * Units.MINUTES,
                30 * Units.MINUTES, 60 * Units.SECONDS));

        //
        // ebid has expired but it still in the rest period => no rotation allowed
        //
        assertTrue(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now - 100 * Units.HOURS),
                now,
                now - 30 * Units.MINUTES,
                30 * Units.MINUTES, 60 * Units.SECONDS));

        //
        // ebid has expired but it still in the rest period => no rotation allowed
        //
        assertTrue(KeyRotationRunnable.shouldRotateNow(
                new EBID(UUID.randomUUID(), now - 100 * Units.HOURS),
                now,
                now - 24 * Units.HOURS,
                30 * Units.MINUTES, 60 * Units.SECONDS));

    }
}