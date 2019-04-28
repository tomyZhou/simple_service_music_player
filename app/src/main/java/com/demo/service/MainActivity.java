package com.demo.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.service.service.PlayService;
import com.demo.service.service.ScanMusicService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private Button startService, stopService, bindService, unbindService, next, previous, scanMusic;
    private TextView tv_title, tv_now_play;
    private List<String> songPathList;

    private PlayService mService;
    private ScanMusicService scanMusicService;
    private boolean isConnected = false;
    private boolean isScanOver = false;
    private boolean isScanConntected = false;
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService = (Button) findViewById(R.id.startService);
        stopService = findViewById(R.id.stopService);
        bindService = findViewById(R.id.bindService);
        unbindService = findViewById(R.id.unbindService);
        next = findViewById(R.id.next);
        previous = findViewById(R.id.previous);
        scanMusic = findViewById(R.id.scanMusic);
        tv_title = findViewById(R.id.tv_title);
        tv_now_play = findViewById(R.id.tv_now_play);

        startService.setOnClickListener(this);
        stopService.setOnClickListener(this);
        bindService.setOnClickListener(this);
        unbindService.setOnClickListener(this);
        next.setOnClickListener(this);
        previous.setOnClickListener(this);
        scanMusic.setOnClickListener(this);

        sp = getApplicationContext().getSharedPreferences("song", MODE_PRIVATE);

        initData();

        bindPlayService();


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    private void bindPlayService() {

        if (songPathList != null && songPathList.size() > 0) {
            Intent bindService = new Intent(MainActivity.this, PlayService.class);
            startService(bindService);
            isConnected = bindService(bindService, musicServiceConnection, BIND_AUTO_CREATE);
        }
    }


    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG,"---------------------onServiceConnected");
            mService = ((PlayService.MyBinder) binder).getService();
            mService.setMusic(songPathList);
            mService.setOnPlayListener(new PlayService.OnPlayListener() {
                @Override
                public void nowPlayName(String name) {
                    tv_now_play.setVisibility(View.VISIBLE);
                    tv_now_play.setText(name);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, name.getClassName() + "系统内存不足时，才会调用。unbindService不会调用。");
        }
    };

    private ServiceConnection scanMusicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            scanMusicService = ((ScanMusicService.MyBinder) service).getService();
            scanMusicService.setScanListener(onScanListener);
            scanMusicService.startScan();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private ScanMusicService.OnScanListener onScanListener = new ScanMusicService.OnScanListener() {
        @Override
        public void OnScaning(String path) {
            tv_title.setText(path);
        }

        @Override
        public void onScanOver(ArrayList<String> songs) {
            songPathList = songs;

            tv_title.setText("一共扫描到（" + songs.size() + "）首音乐");
            isScanOver = true;

            Gson gson = new Gson();
            String data = gson.toJson(songs);
            sp.edit().putString("listStr", data).commit();
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isConnected) {
            unbindService(musicServiceConnection);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startService:
                Log.d("MainActivity", "startService");
                if (songPathList != null && songPathList.size() > 0) {
                    Intent bindService = new Intent(MainActivity.this, PlayService.class);
                    isConnected = bindService(bindService, musicServiceConnection, BIND_AUTO_CREATE);
                }
                break;
            case R.id.stopService:

                Intent stopScanService = new Intent(MainActivity.this, ScanMusicService.class);
                stopService(stopScanService);

                Intent stopService = new Intent(MainActivity.this, PlayService.class);
                stopService(stopService);

                break;
            case R.id.bindService:
//                if (songPathList != null && songPathList.size() > 0 && mService != null) {
//                    Intent bindService = new Intent(MainActivity.this, PlayService.class);
//                    isConnected = bindService(bindService, musicServiceConnection, BIND_AUTO_CREATE);
//                    //使用混合的方法开启服务，
//                    startService(bindService);
//                    mService.start();
//                }

                if (mService != null) {
                    mService.start();
                }

                break;
            case R.id.unbindService:
//                if (isScanConntected) {
//                    unbindService(scanMusicServiceConnection);
//                    isScanConntected = false;
//                }
//
//                if (isConnected) {
//                    unbindService(musicServiceConnection);
//                    isConnected = false;
//                }

                mService.stop();

                break;
            case R.id.next:
                if (isConnected) {
                    mService.next();
                }
                break;
            case R.id.previous:
                if (isConnected) {
                    mService.previous();
                }
                break;
            case R.id.scanMusic:

                tv_title.setVisibility(View.VISIBLE);
                scanSongs();
                break;
        }
    }

    /**
     * 扫描音乐
     */
    private void scanSongs() {
        boolean isConnection = false;
        Log.d(TAG, Environment.getExternalStorageState());
        Log.d(TAG, Environment.MEDIA_MOUNTED);

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Intent scanMusicService = new Intent(MainActivity.this, ScanMusicService.class);
            isScanConntected = bindService(scanMusicService, scanMusicServiceConnection, BIND_AUTO_CREATE);
        } else {
            Toast.makeText(MainActivity.this, "请插入外部存储设备", Toast.LENGTH_SHORT).show();
        }

    }

    private void initData() {
        String data = sp.getString("listStr", "");
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {
        }.getType();
        List<String> list = gson.fromJson(data, listType);

        tv_title.setVisibility(View.VISIBLE);
        if (list != null && list.size() > 0) {
            songPathList = list;
            tv_title.setText("一共(" + songPathList.size() + ")首音乐");
        } else {
            scanSongs();
        }

    }
}
