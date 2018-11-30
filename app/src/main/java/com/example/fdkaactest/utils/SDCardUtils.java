package com.example.fdkaactest.utils;

import android.os.Environment;

public class SDCardUtils {

    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }
}
