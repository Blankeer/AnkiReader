package com.blanke.ankireader.play;

import android.content.SharedPreferences;
import android.graphics.Color;

import com.blanke.ankireader.Config;

import java.io.Serializable;

/**
 * Created by blanke on 16-10-23.
 */

public class PlayConfig implements Serializable {
    public enum PlayMode {
        LOOP,
        RANDOM,
    }

    public static final int DEFAULT_DANMU_COLOR = Color.parseColor("#FF4081");
    public static final int DEFAULT_DANMU_SIZE = 50;
    public static final int DEFAULT_DANMU_BACKGROUD = Color.parseColor("#22ffffff");
    public static final int DEFAULT_DANMU_SPEED = 1;

    public int playCount = 3;//循环次数
    public int playSleepTime = 300;//间隔时间
    public long deckId = -1;//播放列表
    public PlayMode playMode = PlayMode.LOOP;

    public boolean isLoopDesc = false;//倒序播放
    public boolean isShowFloatView = true;//显示悬浮窗
    public boolean floatStyleCommon=false;
    public boolean floatStyleDanmu=true;
    public boolean isShowNotification = true;
    public boolean isPlay = true;

    public int danmuColor = DEFAULT_DANMU_COLOR;
    public int danmuSize = DEFAULT_DANMU_SIZE;
    public int danmuBackgroud = DEFAULT_DANMU_BACKGROUD;
    public int danmuSpeed = DEFAULT_DANMU_SPEED;


    public String tcpIp = "";
    public int tcpPort = 0;

    public PlayConfig() {
    }

    public void save(SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Config.KEY_LOOP_COUNT, playCount);
        editor.putInt(Config.KEY_SLEEP_PLAY, playSleepTime);
        editor.putLong(Config.KEY_DECK_ID, deckId);
        editor.putString(Config.KEY_PLAY_MODE, playMode.toString());
        editor.putBoolean(Config.KEY_LOOP_DESC, isLoopDesc);
        editor.putBoolean(Config.KEY_SHOW_FLOATVIEW, isShowFloatView);
        editor.putBoolean(Config.KEY_FLOAT_STYLE_COMMON,floatStyleCommon);
        editor.putBoolean(Config.KEY_FLOAT_STYLE_DANMU,floatStyleDanmu);
        editor.putBoolean(Config.KEY_SHOW_NOTIFICATION,isShowNotification);
        editor.putBoolean(Config.KEY_IS_PLAY,isPlay);

        editor.putInt(Config.KEY_DANMU_COLOR, danmuColor);
        editor.putInt(Config.KEY_DANMU_SIZE, danmuSize);
        editor.putInt(Config.KEY_DANMU_BACKGROUD, danmuBackgroud);
        editor.putInt(Config.KEY_DANMU_SPEED, danmuSpeed);
        editor.apply();
    }

    public PlayConfig load(SharedPreferences sp) {
        playCount = sp.getInt(Config.KEY_LOOP_COUNT, 3);
        playSleepTime = sp.getInt(Config.KEY_SLEEP_PLAY, 300);
        deckId = sp.getLong(Config.KEY_DECK_ID, -1);
        String playModestr = sp.getString(Config.KEY_PLAY_MODE, PlayMode.LOOP.toString());
        playMode = PlayMode.valueOf(playModestr);
        isLoopDesc = sp.getBoolean(Config.KEY_LOOP_DESC, true);
        isShowFloatView = sp.getBoolean(Config.KEY_SHOW_FLOATVIEW, true);
        floatStyleCommon=sp.getBoolean(Config.KEY_FLOAT_STYLE_COMMON,false);
        floatStyleDanmu=sp.getBoolean(Config.KEY_FLOAT_STYLE_DANMU,true);
        isShowNotification=sp.getBoolean(Config.KEY_SHOW_NOTIFICATION,true);
        isPlay=sp.getBoolean(Config.KEY_IS_PLAY,true);

        danmuColor = sp.getInt(Config.KEY_DANMU_COLOR, DEFAULT_DANMU_COLOR);
        danmuSize = sp.getInt(Config.KEY_DANMU_SIZE, DEFAULT_DANMU_SIZE);
        danmuBackgroud = sp.getInt(Config.KEY_DANMU_BACKGROUD, DEFAULT_DANMU_BACKGROUD);
        danmuSpeed = sp.getInt(Config.KEY_DANMU_SPEED, DEFAULT_DANMU_SPEED);
        return this;
    }

    @Override
    public String toString() {
        return "PlayConfig{" +
                "playCount=" + playCount +
                ", playSleepTime=" + playSleepTime +
                ", deckId=" + deckId +
                ", playMode=" + playMode +
                ", isShowFloatView=" + isShowFloatView +
                ", tcpIp='" + tcpIp + '\'' +
                ", tcpPort=" + tcpPort +
                ", isLoopDesc=" + isLoopDesc +
                '}';
    }
}
