package org.pepppt.core.storage;

import org.junit.Test;
import org.pepppt.core.util.Units;

import java.util.UUID;

import static org.junit.Assert.*;

public class BatchIdsStorageTest {
    @Test
    public void sort() {
        String uuid1 = "00000001-9e28-9364-e06e-a655d1be3460";
        String uuid2 = "00000002-9e28-9364-e06e-a655d1be3460";
        String uuid3 = "00000003-9e28-9364-e06e-a655d1be3460";
        BatchIdsStorage batch = new BatchIdsStorage();
        batch.add(new EBID(UUID.fromString(uuid1), 400));
        batch.add(new EBID(UUID.fromString(uuid2), 1000));
        batch.add(new EBID(UUID.fromString(uuid3), 800));
        batch.sort();
        assertEquals(batch.getAt(0).getId().toString(), uuid1);
        assertEquals(batch.getAt(1).getId().toString(), uuid3);
        assertEquals(batch.getAt(2).getId().toString(), uuid2);

    }

    @Test
    public void removeExpiredIds() {
        long now = System.currentTimeMillis();
        String uuid1 = "00000001-9e28-9364-e06e-a655d1be3460";
        String uuid2 = "00000002-9e28-9364-e06e-a655d1be3460";
        String uuid3 = "00000003-9e28-9364-e06e-a655d1be3460";
        String uuid4 = "00000004-9e28-9364-e06e-a655d1be3460";
        String uuid5 = "00000005-9e28-9364-e06e-a655d1be3460";
        String uuid6 = "00000006-9e28-9364-e06e-a655d1be3460";
        String uuid7 = "00000007-9e28-9364-e06e-a655d1be3460";
        String uuid8 = "00000008-9e28-9364-e06e-a655d1be3460";
        BatchIdsStorage batch = new BatchIdsStorage();
        batch.add(new EBID(UUID.fromString(uuid1), now));
        batch.add(new EBID(UUID.fromString(uuid2), now + 29 * Units.MINUTES));
        batch.add(new EBID(UUID.fromString(uuid3), now + 31 * Units.MINUTES));
        batch.add(new EBID(UUID.fromString(uuid4), now + 5 * Units.MINUTES));
        batch.add(new EBID(UUID.fromString(uuid5), now + 100 * Units.MINUTES));
        batch.add(new EBID(UUID.fromString(uuid6), now + 2 * Units.MINUTES));
        batch.add(new EBID(UUID.fromString(uuid7), now + 15 * Units.MINUTES));
        batch.removeExpiredIds();
        assertEquals(batch.count(), 2);
        assertEquals(batch.getAt(0).getId().toString(), uuid3);
        assertEquals(batch.getAt(1).getId().toString(), uuid5);
    }
}