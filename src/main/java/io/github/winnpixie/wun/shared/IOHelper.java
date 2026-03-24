package io.github.winnpixie.wun.shared;

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
}
