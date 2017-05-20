package com.blanke.ankireader.play.float_text.view;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.play.PlayConfig;

/**
 * 只显示文本
 * Created by blanke on 2017/5/20.
 */

public class TextFloatView extends AppCompatTextView implements BaseFloatView {
    public TextFloatView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setTextColor(Color.parseColor("#ffffff"));
        setBackgroundColor(Color.parseColor("#77000000"));
        setTextSize(17);
        setMinWidth(300);
        setMinHeight(100);
        setMaxHeight(900);
        setGravity(Gravity.CENTER);
        setPadding(15, 5, 15, 5);
    }

    @Override
    public void setNote(final Note note) {
        post(new Runnable() {
            @Override
            public void run() {
                setText(note.getFullContent());
            }
        });
    }

    @Override
    public void setConfig(PlayConfig playConfig) {
        init();
    }

    @Override
    public void initView(final WindowManager windowManager, final WindowManager.LayoutParams params) {
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(TextFloatView.this, params);
        setOnTouchListener(new View.OnTouchListener() {
            float downX, downY;
            int paramX, paramY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
//                Logger.d(event.getAction() + " downCount " + downCount);
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downX = event.getRawX();
                    downY = event.getRawY();
                    paramX = params.x;
                    paramY = params.y;
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float nowX = event.getRawX();
                    float nowY = event.getRawY();
                    params.x = (int) (paramX + nowX - downX);
                    params.y = (int) (paramY + nowY - downY);
                    windowManager.updateViewLayout(TextFloatView.this, params);
                }
                return true;
            }
        });
    }
}
