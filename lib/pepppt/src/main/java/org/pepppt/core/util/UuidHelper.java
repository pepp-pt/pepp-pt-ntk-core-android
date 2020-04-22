package org.pepppt.core.util;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Helper class to convert a UUID from an to bytes.
 */
public class UuidHelper {

    /**
     * Converts a UUID into a byte array.
     * @param uuid The UUID.
     * @return The converted byte array.
     */
    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    /**
     * Converts a byte array into a UUID.
     * @param bytes The byte array.
     * @return The converted UUID.
     */
    public static UUID getUUIDFromBytes(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long high = byteBuffer.getLong();
        long low = byteBuffer.getLong();

        return new UUID(high, low);
    }
}
