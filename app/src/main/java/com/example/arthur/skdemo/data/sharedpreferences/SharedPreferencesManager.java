package com.example.arthur.skdemo.data.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Arthur on 4/17/2016.
 *
 */
public class SharedPreferencesManager {

    public static final String PREFS_NAME = "demoAppPrefs";

    private SharedPreferences.Editor mPrefsEditor;

    private SharedPreferences mPrefs;

    public SharedPreferencesManager(Context context) {
        mPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mPrefsEditor = mPrefs.edit();
    }

    public int getIntPreference(String key, int defValue) {
        return mPrefs.getInt(key, defValue);
    }

    public String getStringPreference(String key) {
        return mPrefs.getString(key, "");
    }

    public void saveStringPreference(String key, String value){
        mPrefsEditor.putString(key, value);
        mPrefsEditor.commit();
    }

    public void saveIntPreference(String key, int value) {
        mPrefsEditor.putInt(key, value);
        mPrefsEditor.commit();
    }
}
