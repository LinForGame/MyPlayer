package com.example.why.myplayer;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public final static int playModeOnce = 0;
    public final static int playModeOneRepeat = 1;
    public final static int playModeOrder = 2;
    public final static int playModeAllRepeat = 3;
    public final static int playModeRandom = 4;
    private String rootPath;
    private File fatherFileDirectory=null;
    private File[] currentFiles=null;
    private ListView listView;
    private TextView textView;
    private Button button;
    private Button button5;
    private Button button6;
    private Button button7;
    private Intent intent;
    private int timeInterval=0;
    private int playMode=0;//0 = 单曲单次， 1 = 单曲循环，2 = 顺序循环，3 = 全部循环，4 = 随机
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
        button5 = findViewById(R.id.button5);
        button6 = findViewById(R.id.button6);
        button7 = findViewById(R.id.button7);

        intent = new Intent(MainActivity.this,MyMediaPlayerService.class);

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("---","绑定成功 已连接上服务onServiceConnected");
                myMediaPlayerService = ((MyMediaPlayerService.MyIBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d("---","绑定失连！！！ onServiceDisconnected");
            }
        };
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(playMode<4) {
                    playMode++;
                }else{
                    playMode=0;
                }
                switch (playMode){
                    case playModeOnce:
                        button5.setText("单曲单次");
                        break;
                    case playModeOneRepeat:
                        button5.setText("单曲循环");
                        break;
                    case playModeOrder:
                        button5.setText("顺序循环");
                        break;
                    case playModeAllRepeat:
                        button5.setText("全部循环");
                        break;
                    case playModeRandom:
                        button5.setText("随机");
                        break;
                }
                myMediaPlayerService.setPlayMode(playMode);
            }
        });
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(timeInterval<5) {
                    timeInterval++;
                }else{
                    timeInterval=0;
                }
                button6.setText(String.valueOf(timeInterval));
                myMediaPlayerService.setTimeInterval(timeInterval);
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
        Log.d("---","button5: 绑定服务启动！");

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
}

