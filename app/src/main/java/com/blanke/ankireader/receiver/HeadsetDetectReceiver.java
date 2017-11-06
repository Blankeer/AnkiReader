package com.blanke.ankireader.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blanke.ankireader.event.StopPlayEvent;

import org.greenrobot.eventbus.EventBus;

public class HeadsetDetectReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
            if (intent.hasExtra("state")) {
                int state = intent.getIntExtra("state", 0);
                if (state == 0) {//耳机拔出
                    EventBus.getDefault().post(new StopPlayEvent());
                }
            }
        }
    }
}  