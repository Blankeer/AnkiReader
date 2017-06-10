package com.blanke.ankireader.play;

import android.app.Service;

import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.config.PlayConfig;

import java.lang.ref.WeakReference;

/**
 * Created by blanke on 2017/5/14.
 */

public abstract class BasePlayHelper {
    protected WeakReference<Service> service;
    protected PlayConfig playConfig;

    public BasePlayHelper(WeakReference<Service> service) {
        this.service = service;
    }

    public void setPlayConfig(PlayConfig playConfig) {
        this.playConfig = playConfig;
    }

    /**
     * play 某个 note
     */
    public abstract void play(Note note) throws Exception;

    public abstract void pause();

    public abstract void stop();

    public abstract void reset();

    public abstract void destroy();

}
