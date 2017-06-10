package com.blanke.ankireader.play.music;

import android.app.Service;
import android.media.MediaPlayer;

import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.config.PlayConfig;
import com.blanke.ankireader.play.BasePlayHelper;
import com.blanke.ankireader.utils.TtsUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by blanke on 2017/5/14.
 */

public class MusicPlayHelper extends BasePlayHelper {
    private MediaPlayer mediaPlayer; // 媒体播放器对象
    private TtsUtils ttsUtils;
    private final static long SLEEP_TIME = 400;

    public static MusicPlayHelper getInstance(Service service) {
        return new MusicPlayHelper(new WeakReference<>(service));
    }

    public MusicPlayHelper(WeakReference<Service> service) {
        super(service);
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void setPlayConfig(PlayConfig playConfig) {
        super.setPlayConfig(playConfig);
        if (playConfig.isTtsSwitch()
                && (playConfig.isTtsUseBack() || playConfig.isTtsUseFront())) {
            ttsUtils = TtsUtils.getInstance(service.get());
        }
    }

    @Override
    public void play(Note note) throws Exception {
//        Logger.d("start play music id=" + note.getId() + " name= " + note.getPrimaryMediaPath());
        boolean isPlayTTS = false;
        if (playConfig.isTtsUseAll()) {//所有都使用 tts
            isPlayTTS = true;
        } else if (note.getMediaPaths().size() == 0) {//tts
            isPlayTTS = true;
        }
        if (isPlayTTS) {
            playTTS(note);
        } else {
            playLocalMusic(note);
        }
//        Logger.d("end...");
    }

    //播放 tts
    private void playTTS(Note note) throws InterruptedException {
        if (playConfig.isTtsUseFront()) {
            playTTSReal(note.getFront().toString());
        }
        if (playConfig.isTtsUseBack()) {
            playTTSReal(note.getBack().toString());
        }
    }

    //播放 anki mp3
    private void playLocalMusic(Note note) throws Exception {
        playMusicReal(note);
        while (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Thread.sleep(SLEEP_TIME);
        }
    }

    private void playMusicReal(Note note) throws IOException {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.reset();// 把各项参数恢复到初始状态
        mediaPlayer.setDataSource(note.getPrimaryMediaPath());
        mediaPlayer.prepare(); // 进行缓冲
        mediaPlayer.start();
    }

    private void playTTSReal(String text) throws InterruptedException {
        if (ttsUtils == null) {
            return;
        }
        ttsUtils.speakText(text);
        while (ttsUtils != null && ttsUtils.isSpeaking()) {
            Thread.sleep(SLEEP_TIME);
        }
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
        if (ttsUtils != null) {
            ttsUtils.getTextToSpeech().stop();
        }
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        if (ttsUtils != null) {
            ttsUtils.getTextToSpeech().stop();
        }
    }

    @Override
    public void reset() {
        mediaPlayer.reset();
    }

    @Override
    public void destroy() {
        stop();
        mediaPlayer.release();
        mediaPlayer = null;
        ttsUtils = null;
    }

}
