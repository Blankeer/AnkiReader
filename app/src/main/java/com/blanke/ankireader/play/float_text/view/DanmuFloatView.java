package com.blanke.ankireader.play.float_text.view;

import android.content.Context;
import android.graphics.Color;
import android.view.WindowManager;

import com.anbetter.danmuku.DanMuView;
import com.anbetter.danmuku.model.DanMuModel;
import com.anbetter.danmuku.model.utils.DimensionUtil;
import com.blanke.ankireader.bean.Note;
import com.blanke.ankireader.config.PlayConfig;

/**
 * danmu
 * Created by blanke on 2017/5/20.
 */

public class DanmuFloatView extends DanMuView implements BaseFloatView {
    private int danmuColor = Color.RED;
    private int danmuSize = 50;
    private int danmuSpeed = 1;
    private int danmuMaxLength;

    public DanmuFloatView(Context context) {
        super(context, null);
        init();
    }

    private void init() {
        setBackgroundColor(Color.parseColor("#22000000"));
        prepare();
    }

    @Override
    public void setNote(Note note) {
        add(getDanmu(note));
    }

    @Override
    public void setConfig(PlayConfig playConfig) {
        setBackgroundColor(playConfig.getDanmuBackgroundColor());
        danmuSize = DimensionUtil.spToPx(getContext(), playConfig.getDanmuSize());
        danmuColor = playConfig.getDanmuColor();
        danmuSpeed = playConfig.getDanmuSpeed();
        danmuMaxLength = playConfig.getDanmuTextLength();
    }

    @Override
    public void initView(WindowManager windowManager, WindowManager.LayoutParams params) {
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        windowManager.updateViewLayout(this, params);
    }


    private DanMuModel getDanmu(Note note) {
        DanMuModel danmu = new DanMuModel();
        danmu.setDisplayType(DanMuModel.RIGHT_TO_LEFT);
        danmu.setPriority(DanMuModel.NORMAL);
        danmu.textSize = danmuSize;
        danmu.textColor = danmuColor;
        danmu.setSpeed(danmuSpeed);
        CharSequence text = note.getFullContent();
        if (text.length() > danmuMaxLength) {
            text = text.subSequence(0, danmuMaxLength) + "...";
        }
        danmu.text = text;
        return danmu;
    }
}
