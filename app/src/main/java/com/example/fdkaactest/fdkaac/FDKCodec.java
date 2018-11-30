package com.example.fdkaactest.fdkaac;

public class FDKCodec {
    private static volatile FDKCodec instance;

    static {
        System.loadLibrary("fdk-aac");
        System.loadLibrary("native-lib");
    }

    public static FDKCodec getInstance() {
        if (instance == null) {
            synchronized (FDKCodec.class) {
                if (instance == null) {
                    instance = new FDKCodec();
                }
            }
        }
        return instance;
    }

    private FDKCodec() {

    }

    public native int initEncoder(int sampleRate,
                            int channelCount, int bitRate);
    public native byte[] encode(short[] input,int len);

    public native void releaseEncoder();

    public native void initDecoder();

    public native byte[] decode(byte[] input);

    public native void releaseDecoder();

}
