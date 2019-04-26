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
