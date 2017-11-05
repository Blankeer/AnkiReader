package com.blanke.ankireader.play.float_text.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.config.PlayConfig;
import com.blanke.ankireader.event.StopPlayEvent;

import org.greenrobot.eventbus.EventBus;

/**
 * 只显示文本
 * Created by blanke on 2017/5/20.
 */

public class TextFloatView extends AppCompatTextView implements BaseFloatView {
    private PlayConfig playConfig;

    public TextFloatView(Context context) {
        super(context);
    }

    private void init(PlayConfig playConfig) {
        this.playConfig = playConfig;
        setTextColor(playConfig.getCommonTextColor());
        setBackgroundColor(playConfig.getCommonTextBackgroundColor());
        setTextSize(playConfig.getCommonTextSize());
        setGravity(playConfig.getCommonTextGravity());
        setMinWidth(300);
        setMinHeight(100);
        setPadding(15, 5, 15, 5);
    }

    @Override
    public void setNote(final Note note) {
        post(new Runnable() {
            @Override
            public void run() {
                int maxLength = playConfig.getCommonTextLength();
                if (playConfig.isTtsUseAll()) {
                    setText(Html.fromHtml(note.getFront().toString() + "<br/>" + note.getSimpleTextWordBack()));
                } else {
                    if (note.getFullContent().length() > maxLength) {
                        setText(note.getFullContent().subSequence(0, maxLength) + "...");
                    } else {
                        setText(note.getFullContent());
                    }
                }
            }
        });
    }

    @Override
    public void setConfig(PlayConfig playConfig) {
        init(playConfig);
    }

    private void onDoubleClick() {
        if (!playConfig.isCommonTextClickStop()) {
            return;
        }
        EventBus.getDefault().post(new StopPlayEvent());
    }

    @Override
    public void initView(final WindowManager windowManager, final WindowManager.LayoutParams params) {
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowManager.updateViewLayout(TextFloatView.this, params);
        setOnTouchListener(new View.OnTouchListener() {
            float downX, downY;
            int paramX, paramY;
            int downCount = 0;
            long lastDownTime;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                            lastDownTime = 0;
                            downCount = 0;
                            onDoubleClick();
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
                    windowManager.updateViewLayout(TextFloatView.this, params);
                }
                return true;
            }
        });
    }
}
