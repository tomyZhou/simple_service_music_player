复习Service用法，使用了两个Servcie，一个负责扫描sd卡音乐文件，一个负责播放音乐。

//ScanMusicService.jva

package com.demo.service.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class ScanMusicService extends Service {
    public final static String TAG = ScanMusicService.class.getSimpleName();

    protected static final int SEARCH_MUSIC_SUCCESS = 1;// 搜索成功标记
    private boolean stop = false;

    private ArrayList songs = new ArrayList();
    private OnScanListener scanListener;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {


            if (msg.what == 1) {
                scanListener.onScanOver(songs);
            } else if (msg.obj != null) {
                Log.d(TAG, "handleMessage" + msg.obj);
                scanListener.OnScaning((String) (msg.obj));
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "ScanMusicService onBind------------");
        stop = false;
        return new MyBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "ScanMusicService onCreate------------");
    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "----------------onUnbind");
        return super.onUnbind(intent);
    }

    public void setScanListener(OnScanListener listener) {
        this.scanListener = listener;
    }

    public void startScan() {

        new Thread(new Runnable() {
            File file = Environment.getExternalStorageDirectory();

            @Override
            public void run() {
                search(file);
                handler.sendEmptyMessage(SEARCH_MUSIC_SUCCESS);
            }
        }).start();
    }

    private void search(File file) {

        if (stop) {
            return;
        }

        if (file != null) {
            Message message = handler.obtainMessage();
            message.obj = file.getAbsolutePath();

            handler.sendMessage(message);
            Log.d(TAG, "onScan " + message.obj);

            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File child : files) {
                        search(child);
                    }
                }
            } else {
                {
                    if (file.getAbsolutePath().endsWith(".mp3")) {
                        songs.add(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    public class MyBinder extends Binder {
        public ScanMusicService getService() {
            return ScanMusicService.this;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "----------onDestory");
        stop = true;
    }

    public interface OnScanListener {

        /**
         * 当前正在扫描哪个文件
         *
         * @return
         */
        public void OnScaning(String path);

        /**
         * 扫描结束
         *
         * @return
         */
        public void onScanOver(ArrayList<String> songs);
    }
}

-----------------------------------------------------------------------------------------------
//PlayService.java

package com.demo.service.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.demo.service.MusicPlayer;
import com.demo.service.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayService extends Service {
    public static final String TAG = PlayService.class.getSimpleName();

    private MusicPlayer player;
    private MediaPlayer mediaPlayer;
    private List<String> songs;
    private OnPlayListener mOnPlayListener;
    private int musicIndex = 0;
    private SharedPreferences sp;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "---------onCreate");
        Log.d(TAG, "初始化播放器,只执行一次");

        sp = getApplicationContext().getSharedPreferences("song",MODE_PRIVATE);
        musicIndex = sp.getInt("musicIndex",0);
        //MediaPlayer.create(this,R.raw.sad);
        mediaPlayer = new MediaPlayer();
        player = new MusicPlayer();
        /*
         * 要用MediaPlayer来创建，不能用MediaPlayer的对象来创建 // 不用带后缀 mediaPlayer = new
         * MediaPlayer(); MediaPlayer.create(this, R.raw.test);
         */

        /*
         * try { mediaPlayer.setDataSource("/sdcard/music/lost times.mp3");
         * mediaPlayer.prepare();
         *
         *
         * //方法二，从网上的链接获取歌曲 try { mediaPlayer.setDataSource(
         * "http://www.yousss.com/uploadfile/mp3/2007-11/20071129134414713.mp3"
         * );
         */

        // mediaPlayer.setLooping(true);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                next();
            }
        });
    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d(TAG, "--------------------onStart");
        mediaPlayer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("TAG", "-------------onStartCommand,执行多次");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void setMusic(List<String> songs) {
        this.songs = songs;

        try {
            mediaPlayer.setDataSource(songs.get(musicIndex));
            mediaPlayer.prepare();
        } catch (Exception e) {
            Log.d("hint", "can't get to the song");
            e.printStackTrace();
        }
    }

    public void setOnPlayListener(OnPlayListener onPlayListener) {
        this.mOnPlayListener = onPlayListener;
    }

    /**
     * 播放
     */
    public void start() {
        mediaPlayer.start();
        checkNowPlay();
    }

    /**
     * 下一曲
     */
    public void next() {
        player.next();
        if (mediaPlayer != null && musicIndex < songs.size()) {
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(songs.get(++musicIndex));
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                checkNowPlay();
                sp.edit().putInt("musicIndex",musicIndex).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 上一曲
     */
    public void previous() {
        player.previous();

        if (mediaPlayer != null && musicIndex > 0) {
            try {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.setDataSource(songs.get(--musicIndex));
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
                checkNowPlay();
                sp.edit().putInt("musicIndex",musicIndex).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * 检查当前播放的哪首音乐
     */
    private void checkNowPlay() {
        if (mOnPlayListener != null) {
            File file = new File(songs.get(musicIndex));
            String name = file.getName();
            mOnPlayListener.nowPlayName(name);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "----------onDestory");
        player.stop();
        player = null;

        mediaPlayer.stop();
    }


    public class MyBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    public interface OnPlayListener {
        public void nowPlayName(String name);
    }


}

-------------------------------------------------------------------------------------------
//MainActivity.java

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

    }


    private ServiceConnection musicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mService = ((PlayService.MyBinder) binder).getService();
            mService.setMusic(songPathList);
            mService.setOnPlayListener(new PlayService.OnPlayListener() {
                @Override
                public void nowPlayName(String name) {
                    tv_now_play.setVisibility(View.VISIBLE);
                    tv_now_play.setText(name);
                }
            });
            mService.start();
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
        mService = null;
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
                if (songPathList != null && songPathList.size() > 0) {
                    Intent bindService = new Intent(MainActivity.this, PlayService.class);
                    isConnected = bindService(bindService, musicServiceConnection, BIND_AUTO_CREATE);
                }

                break;
            case R.id.unbindService:
                if (isScanConntected) {
                    unbindService(scanMusicServiceConnection);
                    isScanConntected = false;
                }

                if (isConnected) {
                    unbindService(musicServiceConnection);
                    isConnected = false;
                }
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

        if (list != null && list.size() > 0) {
            songPathList = list;
            tv_title.setVisibility(View.VISIBLE);
            tv_title.setText("一共(" + songPathList.size() + ")首音乐");
        } else {
            scanSongs();
        }


    }
}


![截图](https://github.com/tomyZhou/simple_service_music_player/blob/master/Screenshot_20190426_090454.jpg)