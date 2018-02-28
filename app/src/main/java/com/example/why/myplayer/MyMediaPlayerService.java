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

/**
 * Created by WHY on 2018/2/27.
 */

public class MyMediaPlayerService extends Service {
    private File file;
    private String currentDirectory=null;
    private List<Map<String,Object>> playList;
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
    public void playMusic(String path) {
        if(currentDirectory==null){
            Log.d("---","playMusic.currentDirectory==null");

            file = new File(path);
            currentDirectory = file.getParent();

            File[] files = new File(currentDirectory).listFiles();
            playList = new ArrayList<>();
            for(int i = 0; i < files.length; i++){

                if(files[i].isFile()&&files[i].canRead()){
                    Map<String,Object> map = new HashMap<>();
                    map.put("fileName",files[i].getAbsolutePath());
                    playList.add(map);
                    filesSort(playList);
                    map = new HashMap<>();
                    map.put("fileName",path);
                    currrentIndex=playList.indexOf(map);
                }
            }
        }
        else
        {
            Log.d("---","playMusic.currentDirectory!=null");
            if(file.getParent().equals(currentDirectory)) {
                Log.d("---","pfile.getParent().equals(currentDirectory)");
                Map<String,Object> map = new HashMap<>();
                map = new HashMap<>();
                map.put("fileName",path);
                currrentIndex=playList.indexOf(map);
            }
            else
            {
                currentDirectory = file.getParent();
                File[] files = new File(currentDirectory).listFiles();
                playList = new ArrayList<>();
                for(int i = 0; i < files.length; i++){

                    if(files[i].isFile()&&files[i].canRead()){
                        Map<String,Object> map = new HashMap<>();
                        map.put("fileName",files[i].getAbsolutePath());
                        playList.add(map);
                        filesSort(playList);
                        map = new HashMap<>();
                        map.put("fileName",path);
                        currrentIndex=playList.indexOf(map);
                    }
                }
            }
        }

        if(!isPlaying) {

            isPlaying=true;

            try {
                mediaPlayer.setDataSource(playList.get(currrentIndex).get("fileName").toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.prepareAsync();
        }else{
            mediaPlayer.stop();
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(playList.get(currrentIndex).get("fileName").toString());
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
    public void filesSort(List<Map<String,Object>> filesList) {
        Collections.sort(filesList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return o1.get("fileName").toString().compareTo(o2.get("fileName").toString());
            }
        });
    }
}
