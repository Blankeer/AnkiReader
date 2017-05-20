package com.blanke.ankireader.play.notify;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;
import android.text.Html;

import com.blanke.ankireader.R;
import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.play.BasePlayHelper;
import com.blanke.ankireader.play.PlayerService;
import com.blanke.ankireader.ui.MainActivity;

import java.lang.ref.WeakReference;

import static com.blanke.ankireader.play.PlayerService.ACTION_EXIT;
import static com.blanke.ankireader.play.PlayerService.ACTION_TOGGLE_PLAY_PAUSE;

/**
 * 通知栏播放
 * Created by blanke on 2017/5/14.
 */

public class NotificationPlayHelper extends BasePlayHelper {

    private static final int NOTIFICATION_ID = 888;
    private Note lastNode;

    public NotificationPlayHelper(WeakReference<Service> service) {
        super(service);
        initNotification();
    }

    public static NotificationPlayHelper getInstance(Service service) {
        return new NotificationPlayHelper(new WeakReference<>(service));
    }

    private void initNotification() {

    }

    @Override
    public void play(Note note) throws Exception {
//        Logger.d("send notification..." + note.getId());
        lastNode = note;
        if (getService() != null && note != null) {
            getService().startForeground(NOTIFICATION_ID,
                    getNotificationText(note.getFront(), note.getBack()));
        }
    }

    @Override
    public void pause() {
        try {
            play(lastNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
    }

    @Override
    public void destroy() {
        this.service.clear();
    }

    private Service getService() {
        return this.service.get();
    }

    private Notification getNotificationText(CharSequence title, CharSequence content) {
        if (getService() == null) {
            return null;
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getService());
        builder.setContentTitle(title);
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.speakers);
        android.support.v4.app.NotificationCompat.BigTextStyle style = new android.support.v4.app.NotificationCompat.BigTextStyle();
        style.bigText(Html.fromHtml(String.valueOf(content)));
        style.setBigContentTitle(title);
        builder.setStyle(style);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setShowWhen(false);
        PendingIntent mPlayPauseIntent = getIntent(ACTION_TOGGLE_PLAY_PAUSE);
        int playPauseIcon = R.mipmap.play;
        String info = "开始";
        PlayerService playerService = (PlayerService) getService();
        if (playerService.getCurrentState()
                == PlayerService.PlayState.PLAYING) {
            playPauseIcon = R.mipmap.pause;
            info = "暂停";
        }
        String stopText = "停止";
        PendingIntent mStopIntent = getIntent(ACTION_EXIT);
        builder.addAction(playPauseIcon, info, mPlayPauseIntent);
        builder.addAction(R.mipmap.stop, stopText, mStopIntent);
        builder.setContentIntent(getContentIntent());
        return builder.build();
    }

    private PendingIntent getContentIntent() {
        final Intent contentIntent = new Intent(getService(), MainActivity.class);
        return PendingIntent.getActivity(getService(), 0, contentIntent, 0);
    }

    private PendingIntent getIntent(String action) {
        final Intent intent = new Intent(getService(), PlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(getService(), 0, intent, 0);
    }
}
