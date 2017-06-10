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

/**
 * Created by blanke on 2017/6/8.
 */

public class ChoseTextSizeDialog {
    public interface onChoseTextSizeListener {
        void onChoseSize(int size);
    }

    public static void show(final Context context, @StringRes int titleId, int defaultSize, final onChoseTextSizeListener listener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_textsize_chose, null);
        final TextView dialogTextsizeTvTest = (TextView) view.findViewById(R.id.dialog_textsize_tv_test);
        final SeekBar dialogTextsizePb = (SeekBar) view.findViewById(R.id.dialog_textsize_sb);
        dialogTextsizePb.setMax(Config.DANMU_SIZE_DP_MAX);
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
        new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setCancelable(true)
                .setView(view)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onChoseSize(dialogTextsizePb.getProgress());
                        }
                    }
                })
                .show();

    }
}
