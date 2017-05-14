package com.blanke.ankireader.play;

import android.content.SharedPreferences;

import com.blanke.ankireader.Config;

import java.io.Serializable;

/**
 * Created by blanke on 16-10-23.
 */

public class PlayConfig implements Serializable {
    public int playCount = 3;//循环次数
    public int playSleepTime = 300;//间隔时间
    public long deckId = -1;//播放列表
    public int playMode = 0;//0 循环,1随机
    public boolean isShowFloatView = true;//显示悬浮窗
    public String tcpIp = "";
    public int tcpPort = 0;
    public boolean isLoopDesc = false;

    public PlayConfig() {
    }

    public void save(SharedPreferences sp) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(Config.KEY_LOOP_COUNT, playCount);
        editor.putInt(Config.KEY_SLEEP_PLAY, playSleepTime);
        editor.putLong(Config.KEY_DECK_ID, deckId);
        editor.putInt(Config.KEY_PLAY_MODE, playMode);
        editor.putBoolean(Config.KEY_SHOW_FLOATVIEW, isShowFloatView);
        editor.putBoolean(Config.KEY_LOOP_DESC, isLoopDesc);
        editor.apply();
    }

    public PlayConfig load(SharedPreferences sp) {
        playCount = sp.getInt(Config.KEY_LOOP_COUNT, 3);
        playSleepTime = sp.getInt(Config.KEY_SLEEP_PLAY, 300);
        deckId = sp.getLong(Config.KEY_DECK_ID, -1);
        playMode = sp.getInt(Config.KEY_PLAY_MODE, 0);
        isShowFloatView = sp.getBoolean(Config.KEY_SHOW_FLOATVIEW, true);
        isLoopDesc = sp.getBoolean(Config.KEY_LOOP_DESC, true);
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
