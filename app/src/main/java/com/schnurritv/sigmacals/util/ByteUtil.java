package com.schnurritv.sigmacals.util;

import java.nio.ByteBuffer;

public class ByteUtil {

    public static final byte STOP_SIGN = 0x0A; // literally the byte for \n. I hope this is okay, since there are no multi line edits here

    public static int getInt(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("Byte array must have exactly 4 bytes.");
        }

        // from https://www.baeldung.com/java-byte-array-to-number
        int value = 0;
        for(byte b : bytes)
            value = (value << 8) + (b & 0xFF);

        return value;
    }

    public static int getInt(byte byte0, byte byte1, byte byte2, byte byte3) {
        return getInt(new byte[]{byte0, byte1, byte2, byte3});
    }

    public static String getString(int startIndex, byte[] data) {
        int dataLength = data.length;

        byte[] stringBytes = new byte[dataLength - startIndex];

        if (dataLength - startIndex >= 0)
            System.arraycopy(data, startIndex, stringBytes, 0, dataLength - startIndex);

        return new String(stringBytes);
    }
    public static long getLong(byte[] byteArray) {
        if (byteArray.length != 8) {
            throw new IllegalArgumentException("Input byte array must have a length of 8");
        }

        long value = 0;
        for (int i = 0; i < 8; i++)
            value = (value << 8) | (byteArray[i] & 0xFFL);

        return value;
    }

    public static long getLong(byte byte0, byte byte1, byte byte2, byte byte3, byte byte4, byte byte5, byte byte6, byte byte7) {
        return getLong(new byte[]{byte0, byte1, byte2,byte3,byte4,byte5,byte6,byte7});
    }

    public static byte[] getBytes(long value) {
        byte[] byteArray = new byte[8];

        for (int i = 7; i >= 0; i--) {
            byteArray[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        return byteArray;
    }
}
