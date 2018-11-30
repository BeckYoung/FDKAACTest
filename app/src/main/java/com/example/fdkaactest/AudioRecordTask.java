package com.example.fdkaactest;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.example.fdkaactest.fdkaac.FDKCodec;
import com.example.fdkaactest.utils.FileUtil;

import org.greenrobot.eventbus.EventBus;

public class AudioRecordTask implements Runnable {
    private final String TAG=AudioRecordTask.class.getSimpleName();
    private boolean isAudioRunning = false; //是否正在录音

    private int sampleRateInHz = 44100; //声音的采样率
    // 双声道
    private int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int minBufferSize;
    private int channelCount=2; // 默认为双声道
    private int bufferSize = 4096;

    public AudioRecordTask() {
        minBufferSize = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        Log.d(TAG,"minBufferSize="+minBufferSize);

        // 设置音量
        //audioTrack.setVolume(2f) ;

    }

    @Override
    public void run() {
        FDKCodec fdkCodec=FDKCodec.getInstance();
        // AudioRecord(设置缓冲区为最小缓冲区的2倍，至少要等于最小缓冲区)
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRateInHz,
                channelConfig,
                audioFormat, minBufferSize);
        if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.d(TAG, " AudioRecord init failed");
            return;
        }
        channelCount=audioRecord.getChannelCount();
        audioRecord.startRecording();
        isAudioRunning=true;
        MainActivity.MainActMessage mainActMessage=new MainActivity.MainActMessage();
        mainActMessage.setActionType(MainActivity.AUDIO_RUNNING);
        EventBus.getDefault().post(mainActMessage);

        int bitRate=sampleRateInHz*channelCount*3/2; //编码比特率
        //int bitRate=64000; //编码比特率
        int frameLength=fdkCodec.initEncoder(sampleRateInHz,channelCount,bitRate);
        if(frameLength>0){
            bufferSize=frameLength*channelCount*2;
        }
        short[] readBuffer = new short[bufferSize];

        byte[] encodedData;
        FileUtil fileAAC=new FileUtil(FileUtil.AAC_EXTER);
        fileAAC.init();
        FileUtil filePCM=new FileUtil(FileUtil.PCM_EXTER);
        filePCM.init();
        while (isAudioRunning) {

            int readCount = audioRecord.read(readBuffer, 0, bufferSize);
            Log.d(TAG,"AudioRecord bufferSize="+(bufferSize)+"readCount="+readCount);
            if(readCount>0){
                filePCM.write(readBuffer);
            }
            encodedData=fdkCodec.encode(readBuffer,readCount);
            if(encodedData!=null){
                fileAAC.write(encodedData);
            }else {
                Log.d(TAG,"encode fail");
            }

        }
        // flush decode buffer data
//        encodedData=fdkCodec.encode(null);
//        if(encodedData!=null){
//            fileAAC.write(encodedData);
//        }

        Log.d(TAG,"AudioRecord stop");
//        audioRecord.stop();
        audioRecord.release();
        fdkCodec.releaseEncoder();
        fileAAC.release();
        filePCM.release();
        MainActivity.MainActMessage messageStop=new MainActivity.MainActMessage();
        messageStop.setActionType(MainActivity.AUDIO_STOP);
        EventBus.getDefault().post(messageStop);

    }

    public void setAudioRunning(boolean audioRunning) {
        isAudioRunning = audioRunning;
    }

    public boolean isAudioRunning() {
        return isAudioRunning;
    }
}