package com.blanke.ankireader.play;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.blanke.ankireader.BuildConfig;
import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.config.PlayConfig;
import com.blanke.ankireader.data.AnkiManager;
import com.blanke.ankireader.event.PausePlayEvent;
import com.blanke.ankireader.event.StartPlayEvent;
import com.blanke.ankireader.event.StopPlayEvent;
import com.blanke.ankireader.http_server.RequestLoginHandler;
import com.blanke.ankireader.play.float_text.FloatTextPlayHelper;
import com.blanke.ankireader.play.float_text.view.BaseFloatView;
import com.blanke.ankireader.play.float_text.view.DanmuFloatView;
import com.blanke.ankireader.play.float_text.view.TextFloatView;
import com.blanke.ankireader.play.music.MusicPlayHelper;
import com.blanke.ankireader.play.notify.NotificationPlayHelper;
import com.blanke.ankireader.receiver.HeadsetDetectReceiver;
import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;
import com.yanzhenjie.andserver.website.AssetsWebsite;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class PlayerService extends Service {


    public enum PlayState {
        NORMAL,
        PLAYING,
        PAUSED,
    }

    public static final String ACTION_TOGGLE_PLAY_PAUSE = "1";
    public static final String ACTION_EXIT = "2";
    private PlayConfig mPlayConfig;
    private List<BasePlayHelper> playHelpersConsumer = new ArrayList<>();//按照顺序进行消费
    private PlayState currentState = PlayState.NORMAL;

    private MusicPlayHelper musicPlayHelper;
    private NotificationPlayHelper notificationPlayHelper;
    private FloatTextPlayHelper floatTextPlayHelper;
    private Handler handler;
    private static boolean running = false;
    private HeadsetDetectReceiver headsetDetectReceiver;

    private PowerManager.WakeLock wakeLock;
    public static Note currentNote;

    private Server httpServer;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        EventBus.getDefault().register(this);
        running = true;
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, BuildConfig.APPLICATION_ID);
        headsetDetectReceiver = new HeadsetDetectReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headsetDetectReceiver, intentFilter);
        initHttpServer();
    }

    private void initHttpServer() {
        Server.Listener mListener = new Server.Listener() {
            @Override
            public void onStarted() {
                Log.d("http", "onStarted");
            }

            @Override
            public void onStopped() {
                Log.d("http", "onStopped");
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
                Log.e("http", e.getMessage());
            }
        };
        AssetsWebsite website = new AssetsWebsite(getAssets(), "");
        AndServer andServer = new AndServer.Build()
                .registerHandler("text", new RequestLoginHandler())
                .website(website)
                .listener(mListener)
                .build();
        this.httpServer = andServer.createServer();
        this.httpServer.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();            //播放信息
        if (action != null) {
            if (action.equals(ACTION_TOGGLE_PLAY_PAUSE)) {//toggle
                toggle();
            } else if (action.equals(ACTION_EXIT)) {
                this.stopSelf();
            }
        } else {//first init
            mPlayConfig = PlayConfig.loadConfig(this);
            destroyPlayHelpers();
            initPlayHelpers();
            play();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initPlayHelpers() {
        playHelpersConsumer = new ArrayList<>();
        if (mPlayConfig.isNotificationSwitch()) {
            notificationPlayHelper = NotificationPlayHelper.getInstance(this);
            notificationPlayHelper.setPlayConfig(mPlayConfig);
            playHelpersConsumer.add(notificationPlayHelper);
        }
        if (mPlayConfig.getFloatMode() != PlayConfig.FloatMode.Null) {
            floatTextPlayHelper = FloatTextPlayHelper.getInstance(this);
            BaseFloatView floatView;
            if (mPlayConfig.getFloatMode() == PlayConfig.FloatMode.Common) {
                floatView = new TextFloatView(this);
            } else {
                floatView = new DanmuFloatView(this);
            }
            floatView.setConfig(mPlayConfig);
            floatTextPlayHelper.setFloatView(floatView);
            playHelpersConsumer.add(floatTextPlayHelper);
        }
        if (mPlayConfig.isPlaySwitch()) {
            musicPlayHelper = MusicPlayHelper.getInstance(this);
            musicPlayHelper.setPlayConfig(mPlayConfig);
            playHelpersConsumer.add(musicPlayHelper);
        }
    }

    private void toggle() {
        if (currentState == PlayState.NORMAL ||
                currentState == PlayState.PAUSED) {
            play();
        } else if (currentState == PlayState.PLAYING) {
            pause();
        }
    }

    /**
     * 播放音乐
     */
    private void play() {
        if (currentState == PlayState.PLAYING) {
            return;
        }
        currentState = PlayState.PLAYING;
        wakeLock.acquire();
        Observable.create(new ObservableOnSubscribe<Note>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<Note> e) throws Exception {
                List<Note> notes = null;
                if (mPlayConfig.isTtsSwitch()) {//读取所有
                    notes = AnkiManager.getNotesByDeckId(mPlayConfig.getPlayDeckIds());
                } else {//读取只有 anki 音频的
                    notes = AnkiManager.getAllHasMediaNotesByDeckId(mPlayConfig.getPlayDeckIds());
                }
                if (notes.size() == 0) {
                    throw new IllegalArgumentException("牌组识别为空!");
                }
//                Logger.d("start emit ");
                EventBus.getDefault().post(new StartPlayEvent());
                int i = getNextIndex(mPlayConfig.isPlayReverse() ? notes.size() : -1,
                        notes.size());
                while (currentState == PlayState.PLAYING) {
                    for (int j = 0; j < mPlayConfig.getPlayLoopCount(); j++) {//循环 x 次
                        Note note = notes.get(i);
                        currentNote = note;
                        e.onNext(note);
                        Thread.sleep(mPlayConfig.getPlayIntervalTime());
                    }
                    i = getNextIndex(i, notes.size());
                }
            }

            private int getNextIndex(int i, int count) {
                if (mPlayConfig.getPlayMode() == PlayConfig.PlayMode.Loop) {//循环
                    if (mPlayConfig.isPlayReverse()) {
                        return (i - 1) % count;
                    }
                    return (i + 1) % count;
                } else if (mPlayConfig.getPlayMode() == PlayConfig.PlayMode.Random) {//随机
                    return getRandomIndex(count);
                }
                return (i + 1) % count;
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.trampoline())//在上一个默认线程 newThread run
                .subscribe(new Consumer<Note>() {
                    @Override
                    public void accept(@NonNull Note note) throws Exception {
                        consumPlayHelper(note);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull final Throwable throwable) throws Exception {
                        throwable.printStackTrace();
                        stopPlayHelpers();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                StackTraceElement trace = throwable.getStackTrace()[0];
                                Toast.makeText(PlayerService.this, "发生了一个错误,错误详情(可以截图反馈到酷安):"
                                                + "\n" + throwable.getMessage() + ":\n"
                                                + trace.getClassName() + ":" + trace.getLineNumber(),
                                        Toast.LENGTH_LONG).show();
                            }
                        });
                        stopSelf();
                    }
                });
    }


    /**
     * 暂停音乐
     */
    private void pause() {
        currentState = PlayState.PAUSED;
        pausePlayHelpers();
    }

    @Subscribe
    public void onEventStop(StopPlayEvent event) {
        stopSelf();
    }

    @Subscribe
    public void onEventPause(PausePlayEvent event) {
        pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentState = PlayState.NORMAL;
        destroyPlayHelpers();
        EventBus.getDefault().unregister(this);
        running = false;
        EventBus.getDefault().post(new StopPlayEvent());
        if (headsetDetectReceiver != null) {
            unregisterReceiver(headsetDetectReceiver);
            headsetDetectReceiver = null;
        }
        if (wakeLock != null) {
            wakeLock.release();
        }
        if (httpServer != null) {
            httpServer.stop();
        }
    }

    /**
     * 按照顺序进行消费
     *
     * @param note
     * @throws Exception
     */
    private void consumPlayHelper(Note note) throws Exception {
        for (BasePlayHelper basePlayHelper : playHelpersConsumer) {
            basePlayHelper.play(note);
        }
    }

    /**
     * 按照顺序进行销毁
     */
    private void destroyPlayHelpers() {
        for (BasePlayHelper basePlayHelper : playHelpersConsumer) {
            basePlayHelper.destroy();
        }
    }

    /**
     * 按照顺序进行pause
     */
    private void pausePlayHelpers() {
        for (BasePlayHelper basePlayHelper : playHelpersConsumer) {
            basePlayHelper.pause();
        }
    }

    /**
     * 按照顺序进行stop
     */
    private void stopPlayHelpers() {
        for (BasePlayHelper basePlayHelper : playHelpersConsumer) {
            basePlayHelper.stop();
        }
    }

    public PlayState getCurrentState() {
        return currentState;
    }

    public static boolean isRunning() {
        return running;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int getRandomIndex(int end) {
        return new Random().nextInt(end);
    }
}
