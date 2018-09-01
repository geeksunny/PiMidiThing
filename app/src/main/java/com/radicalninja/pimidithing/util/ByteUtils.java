package com.radicalninja.pimidithing.util;

public class ByteUtils {

    public static int firstOccuranceOfByte(final byte[] bytes, final byte byteToFind) {
        for (int i = 0; i < bytes.length; i++) {
            final byte b = bytes[i];
            if (b == byteToFind) {
                return i;
            }
        }
        return -1;
    }

    public static int lastOccuranceOfByte(final byte[] bytes, final byte byteToFind) {
        for (int i = bytes.length - 1; i >= 0; i++) {
            final byte b = bytes[i];
            if (b == byteToFind) {
                return i;
            }
        }
        return -1;
    }

}
