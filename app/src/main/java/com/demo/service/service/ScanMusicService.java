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
