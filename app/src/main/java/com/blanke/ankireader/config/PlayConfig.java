package com.blanke.ankireader.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;

import com.blanke.ankireader.Config;
import com.blanke.ankireader.R;

/**
 * Created by blanke on 2017/6/10.
 */

public class PlayConfig {
    public enum FloatMode {
        DanMu,
        Common,
        Null,
    }

    public enum PlayMode {
        Loop,
        Random,
    }

    private FloatMode floatMode;
    private int danmuSize;
    private int danmuColor;
    private int danmuSpeed;
    private int danmuTextLength;
    private int commonTextSize;
    private int commonTextColor;
    private int commonTextBackgroundColor;
    private int commonTextLength;
    private int commonTextGravity;
    private boolean playSwitch;
    private int playLoopCount;
    private int playIntervalTime;
    private PlayMode playMode;
    private boolean playReverse;
    private boolean notificationSwitch;
    private long[] playDeckIds;

    public static PlayConfig loadConfig(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        PlayConfig playConfig = new PlayConfig();
        String temp = preferences.getString(context.getString(R.string.key_float_mode), "0");
        playConfig.floatMode = FloatMode.DanMu;
        if (temp.equals("1")) {
            playConfig.floatMode = FloatMode.Common;
        }
        playConfig.danmuSize = preferences.getInt(context.getString(R.string.key_danmu_textsize),
                Config.DEFAULT_DANMU_SIZE_DP);
        playConfig.danmuColor = preferences.getInt(context.getString(R.string.key_danmu_textcolor),
                ContextCompat.getColor(context, R.color.defaultDanmuColor));
        playConfig.danmuSpeed = preferences.getInt(context.getString(R.string.key_danmu_speed), 5);
        playConfig.danmuTextLength = preferences.getInt(context.getString(R.string.key_danmu_maxlength), 50);
        playConfig.commonTextSize = preferences.getInt(context.getString(R.string.key_common_textsize),
                Config.DEFAULT_DANMU_SIZE_DP);
        playConfig.commonTextColor = preferences.getInt(context.getString(R.string.key_common_textcolor),
                ContextCompat.getColor(context, R.color.defaultCommonTextColor));
        playConfig.commonTextBackgroundColor = preferences
                .getInt(context.getString(R.string.key_common_background),
                        ContextCompat.getColor(context, R.color.defaultCommonBackgroundColor));
        playConfig.commonTextLength = preferences.getInt(context.getString(R.string.key_common_text_maxlength), 50);

        String value = preferences.getString(context.getString(R.string.key_common_text_gravity), "0");
        playConfig.commonTextGravity = Gravity.CENTER;
        if (value.equals("1")) {
            playConfig.commonTextGravity = Gravity.START;
        } else if (value.equals("2")) {
            playConfig.commonTextGravity = Gravity.END;
        }
        playConfig.playSwitch = preferences.getBoolean(context.getString(R.string.key_play_switch), true);
        playConfig.playLoopCount = preferences.getInt(context.getString(R.string.key_play_loop_count), 3);
        playConfig.playIntervalTime = preferences.getInt(context.getString(R.string.key_play_interval_time), 800);
        temp = preferences.getString(context.getString(R.string.key_play_mode), "0");
        playConfig.playMode = PlayMode.Loop;
        if (temp.equals("1")) {
            playConfig.playMode = PlayMode.Random;
        }
        playConfig.playReverse = preferences.getBoolean(context.getString(R.string.key_play_reverse), false);
        playConfig.notificationSwitch = preferences.getBoolean(context.getString(R.string.key_notification_switch), true);
        String tempStr = preferences.getString(context.getString(R.string.key_play_deck_ids), "");
        if (!TextUtils.isEmpty(tempStr)) {
            String[] temps = tempStr.split(",");
            playConfig.playDeckIds = new long[temps.length];
            for (int i = 0; i < temps.length; i++) {
                playConfig.playDeckIds[i] = Long.parseLong(temps[i]);
            }
        }
        return playConfig;
    }

    public static void saveDeckIds(Context context, long... deckIds) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        StringBuilder sb = new StringBuilder();
        if (deckIds != null) {
            for (long deckId : deckIds) {
                sb.append(deckId + ",");
            }
        }
        SharedPreferences.Editor edit = preferences.edit();
        edit.putString(context.getString(R.string.key_play_deck_ids), sb.toString());
        edit.apply();
    }

    public FloatMode getFloatMode() {
        return floatMode;
    }

    public int getDanmuSize() {
        return danmuSize;
    }

    public int getDanmuColor() {
        return danmuColor;
    }

    public int getDanmuSpeed() {
        return danmuSpeed;
    }

    public int getDanmuTextLength() {
        return danmuTextLength;
    }

    public int getCommonTextSize() {
        return commonTextSize;
    }

    public int getCommonTextColor() {
        return commonTextColor;
    }

    public int getCommonTextBackgroundColor() {
        return commonTextBackgroundColor;
    }

    public int getCommonTextLength() {
        return commonTextLength;
    }

    public int getCommonTextGravity() {
        return commonTextGravity;
    }

    public boolean isPlaySwitch() {
        return playSwitch;
    }

    public int getPlayLoopCount() {
        return playLoopCount;
    }

    public int getPlayIntervalTime() {
        return playIntervalTime;
    }

    public PlayMode getPlayMode() {
        return playMode;
    }

    public boolean isPlayReverse() {
        return playReverse;
    }

    public boolean isNotificationSwitch() {
        return notificationSwitch;
    }

    public long[] getPlayDeckIds() {
        return playDeckIds;
    }
}