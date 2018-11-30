package com.example.fdkaactest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int AUDIO_RUNNING = 1;
    public static final int AUDIO_STOP = 0;
    private Button btn_voice;
    private TextView tv_audio;
    private Button btn_aac_play;
    private Button btn_pcm_play;
    private AudioRecordTask audioRecordTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission
                .WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager
                    .PERMISSION_GRANTED) {
                requestPermissions(permissions, 1000);
            }
        }

        btn_voice = findViewById(R.id.btn_voice);
        tv_audio = findViewById(R.id.tv_audio);
        btn_aac_play = findViewById(R.id.btn_aac_play);
        btn_pcm_play = findViewById(R.id.btn_pcm_play);

        EventBus.getDefault().register(this);
        audioRecordTask = new AudioRecordTask();
        btn_aac_play.setOnClickListener(this);
        btn_pcm_play.setOnClickListener(this);
        btn_voice.setOnClickListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        int len = permissions.length;
        if (requestCode == 1000) {
            boolean isAll = true; //全部权限是否申请成功
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAll = false;
                }
            }

            if (!isAll) {
                Toast.makeText(this, "权限未全部同意！", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_voice:
                if(audioRecordTask.isAudioRunning()){
                    // 正在录音,停止录音
                    audioRecordTask.setAudioRunning(false);
                }else {
                    Executors.newSingleThreadExecutor().execute(audioRecordTask);
                }
                break;
            case R.id.btn_aac_play:
                break;
            case R.id.btn_pcm_play:
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainMessage(MainActMessage message){
        if(message.getActionType()==AUDIO_RUNNING){
            tv_audio.setVisibility(View.VISIBLE);
            btn_voice.setText(R.string.audio_status_stop);
        }else {
            tv_audio.setVisibility(View.GONE);
            btn_voice.setText(R.string.audio_status_start);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public static class MainActMessage{
        private int actionType;

        public int getActionType() {
            return actionType;
        }

        public void setActionType(int actionType) {
            this.actionType = actionType;
        }
    }
}
