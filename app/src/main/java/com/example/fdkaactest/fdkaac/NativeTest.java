package com.example.fdkaactest.fdkaac;

public class NativeTest {
    private static volatile NativeTest instace;

    static {
        //System.loadLibrary("fdk-aac");
        System.loadLibrary("native-lib");
    }

    public static NativeTest getInstace() {
        if(instace==null){
            synchronized (NativeTest.class){
                if(instace==null){
                    instace=new NativeTest();
                }
            }
        }
        return instace;
    }

    private NativeTest(){

    }

    public native String stringFromJNI();
}
