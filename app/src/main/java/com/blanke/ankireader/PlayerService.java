package com.blanke.ankireader;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.blanke.ankireader.bean.Note;

import java.util.List;
import java.util.Random;

public class PlayerService extends Service {
    public static final String TOGGLE_LOOP_READ = 9 + "";
    public static final String ACTION_STOP = 10 + "";
    private static final int NOTIFICATION_ID = 100;
    private MediaPlayer mediaPlayer; // 媒体播放器对象
    private String currentMusicpath;            // 音乐文件路径
    private String action;
    private boolean isPause = true;        // 暂停状态
    private int current = 0;        // 记录当前正在播放的音乐
    private List<String> mp3Paths;    //存放Mp3Info对象的集合
    private int status = 5;            //播放状态，默认为自定义
    private int playLoopCount;//循环播放次数
    private int playSleepTime;//播放间隔时间，毫秒
    private int playMode;//播放模式
    private int currentMusicLoopCount = 0;//当前歌曲循环的次数
    private List<Note> notes;
    private boolean isStartLoopRead = false;
    private Handler delayedPlayhandler;
    private PlayConfig mPlayConfig;
    private WindowManager windowManager;
    private TextView floatView;

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        delayedPlayhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                play(0);
            }
        };
        mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                if (isPause) {
                    return;
                }
                if (status == 5) {//自定义播放
//                    Logger.d("current " + currentMusicpath + "," + "currentMusicLoopCount=" + currentMusicLoopCount);
                    if (currentMusicLoopCount < playLoopCount - 1) {
                        currentMusicLoopCount++;
                    } else {
                        currentMusicLoopCount = 0;
                        if (playMode == 0) {
                            current = (current + 1) % mp3Paths.size();
                        } else {
                            current = getRandomIndex(mp3Paths.size());
                        }
                        currentMusicpath = mp3Paths.get(current);
                    }
                    delayedPlayhandler.sendEmptyMessageDelayed(1, playSleepTime);
                }
            }
        });
    }

    private void sendNotification() {
        String title = "title";
        String content = "content";
        if (current < 0 || notes == null || notes.size() <= current) {
//            return;
        } else {
            Note note = notes.get(current);
            content = note.getBack();
            title = note.getFront();
            String msg = title + " : " + content + "\n";
//            Logger.d("send udp " + title);
//            XUdp xudp = XUdp.getUdpClient();
//            xudp.sendMsg(new UdpMsg(msg,
//                    new TargetInfo("255.255.255.255", 6788), TcpMsg.MsgType.Send));
            if (mPlayConfig.tcpPort > 0) {
//                XTcpClient xtcp = XTcpClient.getTcpClient(
//                        new TargetInfo(mPlayConfig.tcpIp, mPlayConfig.tcpPort));
//                xtcp.sendMsg(msg);
            }

        }
        setFloatText("<big><i><b>" + title + "</b></i></big><br/>" + content);
        startForeground(NOTIFICATION_ID, getBigTextNotify(title, content));
    }

    private Notification getBigTextNotify(CharSequence title, CharSequence content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
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
        PendingIntent mPlayPauseIntent = getIntent(TOGGLE_LOOP_READ);
        int playPauseIcon = R.mipmap.play;
        String info = "开始";
        if (isStartLoopRead) {
            playPauseIcon = R.mipmap.pause;
            info = "暂停";
        }
        String stopText = "停止";
        PendingIntent mStopIntent = getIntent(ACTION_STOP);
        builder.addAction(playPauseIcon, info, mPlayPauseIntent);
        builder.addAction(R.mipmap.stop, stopText, mStopIntent);
        builder.setContentIntent(getContentIntent());
        return builder.build();
    }

    private PendingIntent getContentIntent() {
        final Intent contentIntent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 0, contentIntent, 0);
    }

    private PendingIntent getIntent(String action) {
        final Intent intent = new Intent(this, PlayerService.class);
        intent.setAction(action);
        return PendingIntent.getService(getApplicationContext(), 0, intent, 0);
    }

    protected int getRandomIndex(int end) {
        int index = new Random().nextInt(end);
        return index;
    }

    private void initConfig() {
        notes = AnkiManager.getAllHasMediaNotesByDeck(mPlayConfig.playDeck);
//        Logger.d(notes);
        if (notes.size() == 0) {
            Toast.makeText(this, "并没有东西让我播放", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            stopSelf();
            return;
        }
        mp3Paths = AnkiManager.getMediaPath(notes);
        playLoopCount = mPlayConfig.playCount;
        playSleepTime = mPlayConfig.playSleepTime;
        playMode = mPlayConfig.playMode;
        current = 0;
        if (playMode == 1) {//随机
            current = getRandomIndex(mp3Paths.size());
        }
        currentMusicpath = mp3Paths.get(current);
        isStartLoopRead = false;
        currentMusicLoopCount = 0;
        if (mPlayConfig.isShowFloatView) {
            showFloatView();
        } else {
            removeFloatView();
        }
        togglePausePlay();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        action = intent.getAction();            //播放信息
        if (action != null) {
            if (action.equals(TOGGLE_LOOP_READ)) {//自定义
                togglePausePlay();
            } else if (action.equals(ACTION_STOP)) {
                if (isStartLoopRead) {
                    togglePausePlay();
                }
            }
        } else {//第一次进来初始化
            PlayConfig pc = (PlayConfig) intent.getSerializableExtra("config");
            if (pc != null) {
                mPlayConfig = pc;
//                pause();
                delayedPlayhandler.removeMessages(1);
                initConfig();
            } else {
                if (mPlayConfig == null) {
                    mPlayConfig = new PlayConfig();
                }
            }
        }
//        Logger.d("onStartCommand  " + action);
        sendNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    private void togglePausePlay() {
        isStartLoopRead = !isStartLoopRead;
        sendNotification();
        if (!isStartLoopRead) {//pause
            pause();
            delayedPlayhandler.removeMessages(1);
            removeFloatView();
        } else {//play
            currentMusicLoopCount = 0;
            showFloatView();
            play(0);
        }
    }

    /**
     * 播放音乐
     */
    private void play(int currentTime) {
        if (!isStartLoopRead) {
            return;
        }
//        Logger.d("currentMusic=" + currentMusicpath);
        sendNotification();
        isPause = false;
        try {
            mediaPlayer.reset();// 把各项参数恢复到初始状态
            mediaPlayer.setDataSource(currentMusicpath);
            mediaPlayer.prepare(); // 进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));// 注册一个监听器
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 暂停音乐
     */
    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
        sendNotification();
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        removeFloatView();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void removeFloatView() {
        if (floatView != null) {
            windowManager.removeView(floatView);
            floatView = null;
        }
    }

    private void showFloatView() {
        if (floatView != null || !mPlayConfig.isShowFloatView) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (Settings.canDrawOverlays(getApplicationContext())) {
                addFloatView();
            } else {
                Toast.makeText(this, "请在设置中授予悬浮窗权限，重新播放", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        } else {
            addFloatView();
        }
    }

    private void setFloatText(String text) {
        if (floatView != null && mPlayConfig.isShowFloatView) {
            floatView.setText(Html.fromHtml(text));
        }
    }

    private void addFloatView() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatView = new TextView(getApplicationContext());
        floatView.setTextColor(Color.parseColor("#ffffff"));
        floatView.setBackgroundColor(Color.parseColor("#77000000"));
        floatView.setTextSize(17);
        floatView.setMinWidth(300);
        floatView.setMinHeight(100);
        floatView.setMaxHeight(700);
        floatView.setGravity(Gravity.CENTER);
        floatView.setPadding(15, 5, 15, 5);
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.x = 0;
        params.y = 0;
        floatView.setOnTouchListener(new View.OnTouchListener() {
            float downX, downY;
            int paramX, paramY;
            int downCount;
            long lastDownTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Logger.d(event.getAction() + " downCount " + downCount);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = event.getRawX();
                    downY = event.getRawY();
                    paramX = params.x;
                    paramY = params.y;
                    downCount++;
                    long nowTime = System.currentTimeMillis();
                    if (downCount == 1) {
                        lastDownTime = nowTime;
                    } else if (downCount == 2) {//双击事件
                        if (nowTime - lastDownTime < 600) {
                            togglePausePlay();
                            lastDownTime = 0;
                            downCount = 0;
                        } else {
                            downCount = 1;
                            lastDownTime = nowTime;
                        }
                    }
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float nowX = event.getRawX();
                    float nowY = event.getRawY();
                    params.x = (int) (paramX + nowX - downX);
                    params.y = (int) (paramY + nowY - downY);
//                    Logger.d("downx " + downX + ",downy=" + downY + ",nowx " + nowX + ",nowy=" + nowY);
                    if (floatView != null) {
                        windowManager.updateViewLayout(floatView, params);
                    }
                }
                return false;
            }
        });
        windowManager.addView(floatView, params);
    }

    /**
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     */
    private final class PreparedListener implements OnPreparedListener {
        private int currentTime;

        public PreparedListener(int currentTime) {
            this.currentTime = currentTime;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            if (!isStartLoopRead) {
                return;
            }
            mediaPlayer.start(); // 开始播放
            if (currentTime > 0) { // 如果音乐不是从头播放
                mediaPlayer.seekTo(currentTime);
            }
        }
    }

}
