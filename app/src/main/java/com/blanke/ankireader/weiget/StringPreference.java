package com.blanke.ankireader.weiget;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by blanke on 2017/6/8.
 */

public class StringPreference extends Preference {

    public StringPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StringPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StringPreference(Context context) {
        super(context);
    }

    public void setValue(String value) {
        persistString(value);
    }

    public String getValue(String defaultValue) {
        return getPersistedString(defaultValue);
    }
}
