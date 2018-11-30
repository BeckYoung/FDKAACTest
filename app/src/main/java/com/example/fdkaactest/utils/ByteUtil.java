package com.example.fdkaactest.utils;

/**
 * Created by luismaria on 2018/8/11.
 */

public class ByteUtil {

    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static byte[] shorts2Bytes(short[] src) {
        byte[] target = new byte[src.length * 2];
        for (int i = 0; i < src.length; i++) {
            target[2 * i] = (byte) (src[i] >> 8);
            target[2 * i + 1] = (byte) (src[i] & 0xff);
        }
        return target;
    }


    public static String bytes2HexString(byte[] src) {
        char[] hexChar = new char[src.length * 2];
        for (int i = 0; i < src.length; i++) {
            hexChar[2 * i] = HEX_CHAR[(src[i] & 0xf0) >> 4];
            hexChar[2 * i + 1] = HEX_CHAR[src[i] & 0x0f];
        }
        return new String(hexChar);
    }

}
