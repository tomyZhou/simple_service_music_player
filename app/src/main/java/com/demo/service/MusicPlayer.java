package com.demo.service;

import android.util.Log;

public class MusicPlayer {

    public static final String TAG = MusicPlayer.class.getSimpleName();

    public void init(){
        Log.d(TAG,"正在初始化播放器");
    }

    public void start(){
        Log.d(TAG,"开始播放..........");
    }

    public void stop(){
        Log.d(TAG,"停止播放..........");
    }

    public void next(){
        Log.d(TAG,"下一曲");
    }

    public void previous(){
        Log.d(TAG,"上一曲");
    }
}
