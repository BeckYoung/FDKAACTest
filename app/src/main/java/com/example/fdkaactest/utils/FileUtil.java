package com.example.fdkaactest.utils;

import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by luismaria on 2018/8/11.
 */

public class FileUtil {
    public static final String AAC_EXTER=".aac";
    public static final String PCM_EXTER=".pcm";
    public static String DIR_NAME = "FDKAAC";

    private String dirName;
    private String filePath;
    private FileOutputStream fos;

    public FileUtil(String exter) {
        dirName = DIR_NAME;
        if (SDCardUtils.isSdCardExist()) {
            File file = new File(Environment.getExternalStorageDirectory(),
                    dirName);
            if (!file.exists()) {
                file.mkdir();
            }
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String fileName=sdf.format(new Date())+exter;
            filePath = file.getPath()+File.separator+fileName;
        }
    }

    public void init() {
        try {
            fos = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void write(byte[] data) {
        this.write(data,data.length);
    }

    public void write(byte[] data,int len) {
        try {
            if (fos != null) {
                fos.write(data, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(short[] data) {
        try {
            byte[] ab = ByteUtil.shorts2Bytes(data);
            if (fos != null) {
                fos.write(ab, 0, ab.length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        try {
            if (fos != null) {
                fos.flush();
                fos.close();
                fos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
