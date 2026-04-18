package io.github.winnpixie.wun.shared;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class IOHelper {
    private IOHelper() {
    }

    public static int readPartialByte(byte value, int size, boolean left) {
        int intValue = value;

        if (left) {
            intValue >>>= (8 - size);
        }

        int result = 0;
        for (int i = 0; i < size; i++) {
            if (((intValue >>> i) & 1) == 1) {
                result |= 1 << i;
            }
        }

        return result;
    }

    public static void putUUID(ByteBuffer buffer, UUID uuid) {
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
    }

    public static UUID getUUID(ByteBuffer buffer) {
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    public static void putString(ByteBuffer buffer, String value) {
        putString(buffer, value, StandardCharsets.UTF_8);
    }

    public static void putString(ByteBuffer buffer, String value, Charset charset) {
        byte[] data = value.getBytes(charset);

        buffer.putInt(data.length);
        buffer.put(data);
    }

    public static String getString(ByteBuffer buffer) {
        return getString(buffer, StandardCharsets.UTF_8);
    }

    public static String getString(ByteBuffer buffer, Charset charset) {
        int len = buffer.getInt();
        byte[] data = new byte[len];

        buffer.get(data, 0, len);
        return new String(data, charset);
    }
}
