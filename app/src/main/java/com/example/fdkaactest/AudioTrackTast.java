package com.example.fdkaactest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioTrackTast implements Runnable {
    private static final String TAG = AudioTrackTast.class.getSimpleName();

    private int sampleRateInHz = 44100;
    private int channelConfig = AudioFormat.CHANNEL_OUT_STEREO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;

    private int minBufferSize;
    private String filePath;
    private int playStatus;
    private AudioTrack audioTrack;

    public AudioTrackTast() {
        minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
        Log.d(TAG, "minBufferSize=" + minBufferSize);
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(filePath)) {
            Log.d(TAG, "pcm file null");
        } else {
            // 播放pcm
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(filePath);
                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
                        channelConfig, audioFormat, minBufferSize, AudioTrack.MODE_STREAM);
                if(audioTrack.getState()!=AudioTrack.STATE_INITIALIZED){
                    Log.d(TAG,"audioTrack not init");
                    return;
                }
                audioTrack.setStereoVolume(2.0F, 2.0F);
                audioTrack.play();
                playStatus=AudioTrack.PLAYSTATE_PLAYING;
                MainActivity.MainActMessage mainActMessage = new MainActivity.MainActMessage();
                mainActMessage.setActionType(MainActivity.AUDIO_PCM_RUNNING);
                EventBus.getDefault().post(mainActMessage);

                byte[] audioData = new byte[minBufferSize];
                int readCount;
                boolean isFileLast=false; // 文件尾
                while (playStatus == AudioTrack.PLAYSTATE_PLAYING) {
                    readCount = inputStream.read(audioData, 0, minBufferSize);
                    Log.d(TAG, "AudioRecord bufferSize=" + (minBufferSize) + ",readCount=" + readCount);

                    
                    if (readCount > 0) {
                        audioTrack.write(audioData, 0, readCount);
                    }else if(readCount==-1){
                        isFileLast=true;
                        playStatus = AudioTrack.PLAYSTATE_STOPPED;
                    }
                }

                if(isFileLast) {
                    audioTrack.stop();
                    audioTrack.release();
                    audioTrack = null;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            MainActivity.MainActMessage mainActMessage = new MainActivity.MainActMessage();
            mainActMessage.setActionType(MainActivity.AUDIO_PCM_STOP);
            EventBus.getDefault().post(mainActMessage);
        }

    }

    public void setPlayStatus(int status){
        this.playStatus=status;
        if(audioTrack!=null){
            if(audioTrack.getPlayState()==AudioTrack.PLAYSTATE_PLAYING){
                audioTrack.stop();
                audioTrack.release();
                audioTrack=null;
            }
        }
    }

    public int getPlayStatus() {
        return playStatus;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
