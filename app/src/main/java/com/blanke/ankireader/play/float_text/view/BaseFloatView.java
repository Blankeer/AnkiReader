package com.blanke.ankireader.play.float_text.view;

import android.view.WindowManager;

import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.play.PlayConfig;

/**
 * Created by blanke on 2017/5/20.
 */

public interface BaseFloatView {
    void setNote(Note note);

    void setConfig(PlayConfig playConfig);

    void initView(WindowManager windowManager,
                  WindowManager.LayoutParams layoutParams);
}
