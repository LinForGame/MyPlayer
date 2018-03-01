package com.example.why.myplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by WHY on 2018/2/27.
 */

public class MyMediaPlayerService extends Service {
    public final static int playModeOnce = 0;
    public final static int playModeOneRepeat = 1;
    public final static int playModeOrder = 2;
    public final static int playModeAllRepeat = 3;
    public final static int playModeRandom = 4;
    private int playMode=0;//0 = 单曲单次， 1 = 单曲循环，2 = 顺序循环，3 = 全部循环，4 = 随机
    private int timeInterval=0;
    private TimerTask timerTask;
    private Timer timer;
    private File file;
    private String currentDirectory=null;
    private List<String> playList;
    private int currrentIndex;
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
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        switch (playMode){
                            case playModeOnce:
                                return;
                            case playModeOneRepeat:

                                break;
                            case playModeOrder:
                                if(currrentIndex<(playList.size()-1)) {
                                    currrentIndex++;
                                } else {
                                    return;
                                }
                                break;
                            case playModeAllRepeat:
                                if(currrentIndex<(playList.size()-1)) {
                                    currrentIndex++;
                                } else {
                                    currrentIndex = 0;
                                }
                                break;
                            case playModeRandom:
                                Random rand = new Random();
                                currrentIndex = rand.nextInt(playList.size());
                                break;
                        }
                        playMusicBySelf(playList.get(currrentIndex));
                    }
                };
                timer = new Timer();
                timer.schedule(timerTask,timeInterval*1000);



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
    public void playMusicByUI(String path) {
        if(timer!=null){
            timer.cancel();
            timer.purge();
            timer = null;
        }

        Log.d("111","path:"+path);
        if(currentDirectory==null){
            Log.d("111","playMusic.currentDirectory==null");

            file = new File(path);
            currentDirectory = file.getParent();

            File[] files = new File(currentDirectory).listFiles();
            playList = new ArrayList<>();
            for(int i = 0; i < files.length; i++){
                if(files[i].isFile()&&files[i].canRead()){
                    String str = files[i].getAbsolutePath();
                    playList.add(str);
                }
            }
            filesSort(playList);
            currrentIndex=playList.indexOf(path);
            Log.d("111","currentIndex="+currrentIndex);
        }
        else
        {
            file = new File(path);
            Log.d("111","playMusic.currentDirectory="+currentDirectory);

            if(file.getParent().equals(currentDirectory)) {
                Log.d("111","pfile.getParent().equals(currentDirectory)==true");
                currrentIndex=playList.indexOf(path);
                Log.d("111","currentIndex="+currrentIndex);
            }
            else
            {
                Log.d("111","file.getParent().equals(currentDirectory)==false");
                currentDirectory = file.getParent();
                File[] files = new File(currentDirectory).listFiles();
                playList = new ArrayList<>();
                for(int i = 0; i < files.length; i++){
                    if(files[i].isFile()&&files[i].canRead()){
                        String str = files[i].getAbsolutePath();
                        playList.add(str);
                    }
                }
                filesSort(playList);
                currrentIndex=playList.indexOf(path);
                Log.d("111","currentIndex="+currrentIndex);
            }
        }

        if(!isPlaying) {

            isPlaying=true;

            try {
                mediaPlayer.setDataSource(playList.get(currrentIndex));

            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.prepareAsync();
        }else{
            mediaPlayer.stop();
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(playList.get(currrentIndex));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.prepareAsync();
        }
    }
    public void playMusicBySelf(String path){
        isPlaying = true;
        if(timer!=null){
            timer.cancel();
            timer.purge();
            timer = null;
        }
        try {
            mediaPlayer.setDataSource(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.prepareAsync();
    }
    public void stopMusic(){
        mediaPlayer.stop();
    }

    public void pauseMusic(){
        mediaPlayer.pause();
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    public void setTimeInterval(int timeInterval) {
        this.timeInterval = timeInterval;
    }

    public List<Map<String,Object>> AudioSort(String path){
        List<Map<String,Object>> audioLists = new ArrayList<>();
        File[] files = new File(path).listFiles();
        for(int i = 0; i < files.length; i++){




        }
        return audioLists;
    }
    public int AudioSortBySuffix(String name){
        int type = 0;

        return type;
    }
    public void filesSort(List<String> filesList) {
        Collections.sort(filesList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
    }
}
