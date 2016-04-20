package com.example.arthur.skdemo.data;

import android.app.Application;

import com.example.arthur.skdemo.data.sharedpreferences.SharedPreferencesManager;
import com.example.arthur.skdemo.networking.DatabaseManager;

/**
 * Created by Arthur on 4/17/2016.
 *
 */
public class SKApplication extends Application {

    private String mMapResourcesDirPath;

    private SharedPreferencesManager mSharedPreferences;

    private DatabaseManager mDataBase;

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedPreferences = new SharedPreferencesManager(this);

        mDataBase = DatabaseManager.getInstance(this);
    }

    public String getMapResourcesDirPath() {
        return mMapResourcesDirPath;
    }

    public void setMapResourcesDirPath(String mapResourcesDirPath) {
        this.mMapResourcesDirPath = mapResourcesDirPath;
    }

    public DatabaseManager getDataBase() {
        return mDataBase;
    }

    public SharedPreferencesManager getSharedPreferences() {
        return mSharedPreferences;
    }
}
