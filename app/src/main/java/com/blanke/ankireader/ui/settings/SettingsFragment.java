package com.blanke.ankireader.ui.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.blanke.ankireader.Config;
import com.blanke.ankireader.R;
import com.blanke.ankireader.weiget.ChoseTextSizeDialog;
import com.blanke.ankireader.weiget.IntPreference;

/**
 * Created by blanke on 2017/6/8.
 */

public class SettingsFragment extends PreferenceFragment {

    private ListPreference choseMode;
    private PreferenceScreen danmuScreen;
    private IntPreference danmuSize;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);
        choseMode = (ListPreference) findPreference(getString(R.string.key_float_mode));
        danmuScreen = (PreferenceScreen) findPreference(getString(R.string.key_float_mode_danmu));
        choseMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int index = choseMode.findIndexOfValue(newValue.toString());
                danmuScreen.setEnabled(index == 0);
                return true;
            }
        });
        //mock change event
        choseMode.getOnPreferenceChangeListener()
                .onPreferenceChange(choseMode, choseMode.getValue());
        //弹幕配置
        danmuSize = (IntPreference) findPreference(getString(R.string.key_danmu_textsize));
        danmuSize.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final int size = danmuSize.getValue(Config.DEFAULT_DANMU_SIZE_DP);
                ChoseTextSizeDialog.show(getActivity(), R.string.text_danmu_textsize,
                        size, new ChoseTextSizeDialog.onChoseTextSizeListener() {
                            @Override
                            public void onChoseSize(int size) {
                                danmuSize.setValue(size);
                            }
                        }
                );
                return true;
            }
        });
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        // Initiating Dialog's layout when any sub PreferenceScreen clicked
        if (preference.getClass() == PreferenceScreen.class) {
            // Retrieving the opened Dialog
            Dialog dialog = ((PreferenceScreen) preference).getDialog();
            if (dialog == null) return false;
            if (!TextUtils.isEmpty(preference.getTitle())) {
                getActivity().setTitle(preference.getTitle());
            }
            initDialogLayout(dialog);   // Initiate the dialog's layout
        }
        return true;
    }

    private void initDialogLayout(Dialog dialog) {
        View fragmentView = getView();

        // Get absolute coordinates of the PreferenceFragment
        int fragmentViewLocation[] = new int[2];
        fragmentView.getLocationOnScreen(fragmentViewLocation);

        // Set new dimension and position attributes of the dialog
        WindowManager.LayoutParams wlp = dialog.getWindow().getAttributes();
        wlp.x = fragmentViewLocation[0]; // 0 for x
        wlp.y = fragmentViewLocation[1]; // 1 for y
        wlp.width = fragmentView.getWidth();
        wlp.height = fragmentView.getHeight();

        dialog.getWindow().setAttributes(wlp);

        // Set flag so that you can still interact with objects outside the dialog
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
    }
}
