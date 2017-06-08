package com.blanke.ankireader.weiget;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.blanke.ankireader.Config;
import com.blanke.ankireader.R;
import com.rtugeek.android.colorseekbar.ColorSeekBar;

/**
 * Created by blanke on 2017/6/8.
 */

public class ChoseTextSizeColorDialog {
    public interface onChoseTextSizeColorListener {
        void onChoseSizeColor(int size, int color);
    }

    public static void show(final Context context, @StringRes int titleId,
                            int defaultSize, int defaultColor,
                            final onChoseTextSizeColorListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_textsize_color_chose, null);
        final TextView dialogTextsizeTvTest = (TextView) view.findViewById(R.id.dialog_text_tv_test);
        final SeekBar dialogTextsizePb = (SeekBar) view.findViewById(R.id.dialog_text_sb);
        final ColorSeekBar colorSeekBar = (ColorSeekBar) view.findViewById(R.id.dialog_text_csb);
        dialogTextsizePb.setMax(Config.DEFAULT_DANMU_SIZE_DP_MAX);
        dialogTextsizePb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                dialogTextsizeTvTest.setTextSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        dialogTextsizePb.setProgress(defaultSize);
        dialogTextsizeTvTest.setTextSize(defaultSize);

        colorSeekBar.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener() {
            @Override
            public void onColorChangeListener(int i, int i1, int color) {
                dialogTextsizeTvTest.setTextColor(color);
            }
        });
        dialogTextsizeTvTest.setTextColor(defaultColor);
        colorSeekBar.setColor(defaultColor);
        new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setCancelable(true)
                .setView(view)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onChoseSizeColor(dialogTextsizePb.getProgress(), colorSeekBar.getColor());
                        }
                    }
                })
                .show();

    }
}
