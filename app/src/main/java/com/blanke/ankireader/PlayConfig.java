package com.blanke.ankireader;

import com.blanke.ankireader.bean.Deck;

import java.io.Serializable;

/**
 * Created by blanke on 16-10-23.
 */

public class PlayConfig implements Serializable {
    public int playCount = 3;//循环次数
    public int playSleepTime = 300;//间隔时间
    public Deck playDeck;//播放列表
    public int playMode = 0;//0 循环,1随机
    public boolean isShowFloatView = true;//显示悬浮窗
    public String tcpIp = "";
    public int tcpPort = 0;

    public PlayConfig() {
    }

    @Override
    public String toString() {
        return "PlayConfig{" +
                "playCount=" + playCount +
                ", playSleepTime=" + playSleepTime +
                ", playDeck=" + playDeck +
                ", playMode=" + playMode +
                ", isShowFloatView=" + isShowFloatView +
                ", tcpIp='" + tcpIp + '\'' +
                ", tcpPort=" + tcpPort +
                '}';
    }
}
