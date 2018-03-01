package com.example.why.myplayer;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public final static int playModeOrder = 0;
    public final static int playModeCircle = 1;
    public final static int playModeRandom = 2;
    private boolean isPlaying = false;
    public boolean playRepeat=true;
    private String rootPath;
    private File fatherFileDirectory=null;
    private File[] currentFiles=null;
    private ListView listView;
    private TextView textView;
    private Button button;
    private Button btnPlayMode;
    private Button btnTimeInterval;
    private Button btnPlayRepeat;
    private Button btnNextOne;
    private Button btnPreviouOne;
    private Button btnPauseStart;
    private Intent intent;
    private int timeInterval=0;
    private int playMode=0;
    private ServiceConnection connection;
    private MyMediaPlayerService myMediaPlayerService=null;
    List<Map<String, Object>> fileLists;
    private int ii;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listView);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        btnPlayMode = findViewById(R.id.btnPlayMode);
        btnTimeInterval = findViewById(R.id.btnTimeInterval);
        btnPlayRepeat = findViewById(R.id.btnPlayRepeat);
        btnNextOne = findViewById(R.id.btnNextOne);
        btnPreviouOne = findViewById(R.id.btnPreviouOne);
        btnPauseStart = findViewById(R.id.btnPauseStart);

        intent = new Intent(MainActivity.this,MyMediaPlayerService.class);

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("---","绑定成功 已连接上服务onServiceConnected");
                myMediaPlayerService = ((MyMediaPlayerService.MyIBinder) service).getService();
                myMediaPlayerService.setCallback(new MyMediaPlayerService.Callback() {
                    @Override
                    public void onStateChange(boolean state) {
                        Message msg = new Message();
                        Bundle b = new Bundle();
                        b.putBoolean("state",state);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("---","绑定失连！！！ onServiceDisconnected");
            }
        };
        btnPlayMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playMode<2) {
                    playMode++;
                }else{
                    playMode=0;
                }
                switch (playMode){
                    case playModeOrder:
                        btnPlayMode.setText("顺序循环");
                        break;
                    case playModeCircle:
                        btnPlayMode.setText("全部循环");
                        break;
                    case playModeRandom:
                        btnPlayMode.setText("随机");
                        break;
                }
                myMediaPlayerService.setPlayMode(playMode);
            }
        });
        btnTimeInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timeInterval<5) {
                    timeInterval++;
                }else{
                    timeInterval=0;
                }
                btnTimeInterval.setText(String.valueOf(timeInterval));
                myMediaPlayerService.setTimeInterval(timeInterval);
            }
        });

        btnPlayRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playRepeat = !playRepeat;
                myMediaPlayerService.setPlayRepeat(playRepeat);
                if(playRepeat){
                    btnPlayRepeat.setText("重复");
                }else{
                    btnPlayRepeat.setText("不重复");
                }
            }
        });

        btnPreviouOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMediaPlayerService.playPreviouOne();
            }
        });

        btnNextOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMediaPlayerService.playNextOne();
            }
        });

        btnPauseStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myMediaPlayerService.pauseMusic();
            }
        });
        /**for test**/

        /**for test**/
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){

            rootPath = Environment.getExternalStorageDirectory().getPath();

            File file = new File(rootPath);
            fatherFileDirectory = file;
            currentFiles = file.listFiles();
            inflateListView(currentFiles);

        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d("---","setOnItemClickListener view.id:"+view.getId()+",id:"+id);
                File file = new File(fileLists.get(position).get("filePath").toString());
                if(file.isDirectory()){
                    if(file.listFiles()==null){
                        Log.d("---","file.canRead()=="+file.canRead());
                        Log.d("---","没有权限！获取不到文件对象！！！");
                    }
                    else {
                        fatherFileDirectory = file;
                        fileLists.clear();
                        inflateListView(file.listFiles());
                    }
                }
                else{
                    Log.d("---","是个文件！"+"setOnItemClickListener view.id:"+view.getId()+",id:"+id);
                    if(myMediaPlayerService!=null) {
                        if(file.canRead()) {
                            myMediaPlayerService.playMusicByUI(file.getAbsolutePath());
                        }else{
                            Log.d("---","是个文件！ bfile.canRead() is false！！！");
                        }
                    }else{
                        Log.d("---","是个文件！ but myMediaPlayerService==null！！！");
                    }
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("---","setOnClickListener"+"view.id:"+v.getId());
                if(fatherFileDirectory.getParentFile()!=null) {
                    if(fatherFileDirectory.getParentFile().listFiles()!=null) {
                        fatherFileDirectory = fatherFileDirectory.getParentFile();
                        inflateListView(fatherFileDirectory.listFiles());
                    }else{
                        Log.d("---","无上级目录权限！！！");
                        Toast.makeText(MainActivity.this,"无上级目录权限！！！",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"已经没有上层目录了！！！",Toast.LENGTH_SHORT).show();
                }
            }
        });

        bindService(intent,connection,Service.BIND_AUTO_CREATE);
        Log.d("---","btnPlayMode: 绑定服务启动！");

        }

    private void inflateListView(File[] files) {
        List<Map<String,Object>> directoryList = new ArrayList<>();
        List<Map<String,Object>> subFileList = new ArrayList<>();
        if(fatherFileDirectory!=null){
            textView.setText(fatherFileDirectory.getAbsolutePath());
            Log.d("---","path:"+fatherFileDirectory.getAbsolutePath());
        }

        if (files == null){
            Log.d("---","files==null!!!");
        }
        else if(files.length<0){
            Log.d("---","files.length<0!!!");
        }
        else if(files.length==0){
            Log.d("---","files.length=0!!!");
            SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this, fileLists,
                    R.layout.list_item, new String[]{"fileName"}, new int[]{R.id.textView2});
            listView.setAdapter(simpleAdapter);
        }
        else{
            Log.d("---","files!=null&&files.length>0");

            for (int i = 0; i < files.length; i++) {
                Map<String, Object> audioItem = new HashMap<>();
                audioItem.put("fileName", files[i].getName());
                audioItem.put("filePath", files[i].getAbsolutePath());
                if (files[i].isDirectory()) {
                    audioItem.put("isDirectory", R.drawable.directory_icon_1);
                    directoryList.add(audioItem);
                } else {
                    audioItem.put("isDirectory", R.drawable.file_icon_1);
                    subFileList.add(audioItem);
                }
            }
            filesSort(directoryList);
            filesSort(subFileList);

            fileLists = directoryList;
            fileLists.addAll(subFileList);

            SimpleAdapter simpleAdapter = new SimpleAdapter(MainActivity.this, fileLists,
                    R.layout.list_item, new String[]{"fileName","isDirectory"}, new int[]{R.id.textView2,R.id.imageView});
            listView.setAdapter(simpleAdapter);

        }


    }
    private void filesSort(List<Map<String,Object>> filesList){
        Collections.sort(filesList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return o1.get("fileName").toString().compareTo(o2.get("fileName").toString());
            }
        });

    }
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.getData().getBoolean("state")){
                btnPauseStart.setText("暂停");
            }else{
                btnPauseStart.setText("播放");
            }
        }
    };
}

