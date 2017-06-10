package com.blanke.ankireader.play.music;

import android.app.Service;
import android.media.MediaPlayer;

import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.play.BasePlayHelper;
import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by blanke on 2017/5/14.
 */

public class MusicPlayHelper extends BasePlayHelper {
    private MediaPlayer mediaPlayer; // 媒体播放器对象

    public static MusicPlayHelper getInstance(Service service) {
        return new MusicPlayHelper(new WeakReference<>(service));
    }

    public MusicPlayHelper(WeakReference<Service> service) {
        super(service);
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void play(Note note) throws Exception {
        Logger.d("start play music id=" + note.getId() + " name= " + note.getPrimaryMediaPath());
        playMusic(note);
        while (mediaPlayer != null && mediaPlayer.isPlaying()) {
            Thread.sleep(800);
        }
        Logger.d("end...");

    }

    private void playMusic(Note note) throws IOException {
        if (mediaPlayer == null) {
            return;
        }
        mediaPlayer.reset();// 把各项参数恢复到初始状态
        mediaPlayer.setDataSource(note.getPrimaryMediaPath());
        mediaPlayer.prepare(); // 进行缓冲
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    @Override
    public void reset() {
        mediaPlayer.reset();
    }

    @Override
    public void destroy() {
        mediaPlayer.release();
        mediaPlayer = null;
    }

}
