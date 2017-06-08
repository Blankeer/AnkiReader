package com.blanke.ankireader.weiget;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by blanke on 2017/6/8.
 */

public class IntPreference extends Preference {
    public IntPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IntPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IntPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IntPreference(Context context) {
        super(context);
    }

    public void setValue(int value) {
        persistInt(value);
    }

    public int getValue(int defaultValue) {
        return getPersistedInt(defaultValue);
    }
}
