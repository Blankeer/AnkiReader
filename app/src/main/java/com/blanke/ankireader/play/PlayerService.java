package com.blanke.ankireader.play;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.data.AnkiManager;
import com.blanke.ankireader.play.float_text.FloatTextPlayHelper;
import com.blanke.ankireader.play.float_text.view.BaseFloatView;
import com.blanke.ankireader.play.float_text.view.DanmuFloatView;
import com.blanke.ankireader.play.float_text.view.TextFloatView;
import com.blanke.ankireader.play.music.MusicPlayHelper;
import com.blanke.ankireader.play.notify.NotificationPlayHelper;

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

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
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
            PlayConfig pc = (PlayConfig) intent.getSerializableExtra("config");
            if (pc != null) {
                mPlayConfig = pc;
                destroyPlayHelpers();
                initPlayHelpers();
                play();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void initPlayHelpers() {
        playHelpersConsumer = new ArrayList<>();
        if (mPlayConfig.isShowNotification) {
            notificationPlayHelper = NotificationPlayHelper.getInstance(this);
            notificationPlayHelper.setPlayConfig(mPlayConfig);
            playHelpersConsumer.add(notificationPlayHelper);
        }
        if (mPlayConfig.isShowFloatView) {
            floatTextPlayHelper = FloatTextPlayHelper.getInstance(this);
            BaseFloatView floatView = new DanmuFloatView(this);
            floatView.setConfig(mPlayConfig);
            if (mPlayConfig.floatStyleCommon) {
                floatView = new TextFloatView(this);
            }
            floatTextPlayHelper.setFloatView(floatView);
            playHelpersConsumer.add(floatTextPlayHelper);
        }
        if (mPlayConfig.isPlay) {
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
        Observable.create(new ObservableOnSubscribe<Note>() {

            @Override
            public void subscribe(@NonNull ObservableEmitter<Note> e) throws Exception {
                List<Note> notes = null;
                if (mPlayConfig.isPlay) {
                    notes = AnkiManager.getAllHasMediaNotesByDeckId(mPlayConfig.deckId);
                } else {
                    notes = AnkiManager.getNotesByDeckId(mPlayConfig.deckId);
                }
                if (notes.size() == 0) {
                    throw new IllegalArgumentException("牌组识别为空!");
                }
//                Logger.d("start emit ");
                int i = getNextIndex(mPlayConfig.isLoopDesc ? notes.size() : -1,
                        notes.size());
                while (currentState == PlayState.PLAYING) {
                    for (int j = 0; j < mPlayConfig.playCount; j++) {//循环 x 次
                        Note note = notes.get(i);
                        e.onNext(note);
                        Thread.sleep(mPlayConfig.playSleepTime);
                    }
                    i = getNextIndex(i, notes.size());
                }
            }

            private int getNextIndex(int i, int count) {
                if (mPlayConfig.playMode == PlayConfig.PlayMode.LOOP) {//循环
                    if (mPlayConfig.isLoopDesc) {
                        return (i - 1) % count;
                    }
                    return (i + 1) % count;
                } else if (mPlayConfig.playMode == PlayConfig.PlayMode.RANDOM) {//随机
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

    @Override
    public void onDestroy() {
        currentState = PlayState.NORMAL;
        destroyPlayHelpers();
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int getRandomIndex(int end) {
        return new Random().nextInt(end);
    }
}
