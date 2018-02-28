package com.example.why.myplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by WHY on 2018/2/27.
 */

public class MyMediaPlayerService extends Service {
    private MediaPlayer mediaPlayer;
    private MyIBinder myIBinder = new MyIBinder();
    private boolean isPlaying = false;

    public class MyIBinder extends Binder {
        MyMediaPlayerService getService(){
            return MyMediaPlayerService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("---","MyMediaPlayerService.onBind()");
        return myIBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("---","MyMediaPlayerService.onCreate()");
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isPlaying = false;
                Log.d("---","音乐播放完毕 setOnCompletionListener");
                mp.reset();

            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("---","音乐播放错误！！！ onError From:"+mp.toString()+": what："+what+", extra: "+extra);
               // mp.reset();
                return false;
            }
        });
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.d("---","音乐准备完毕，可以播放了 setOnPreparedListener");
                mp.start();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("---","MyMediaPlayerService.onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("---","MyMediaPlayerService.onDestroy()");
        if(mediaPlayer!=null&&mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        super.onDestroy();
    }
    public void playMusic(String path){
        if(!isPlaying) {

            isPlaying=true;

            try {
                mediaPlayer.setDataSource(path);

            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.prepareAsync();
        }else{
            mediaPlayer.stop();
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.prepareAsync();
        }
    }

    public void stopMusic(){
        mediaPlayer.stop();
    }

    public void pauseMusic(){
        mediaPlayer.pause();
    }
}
