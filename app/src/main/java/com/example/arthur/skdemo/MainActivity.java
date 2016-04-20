package com.example.arthur.skdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.example.arthur.skdemo.data.SKApplication;
import com.example.arthur.skdemo.ui.MapActivity;
import com.example.arthur.skdemo.utils.SKUtils;
import com.skobbler.ngx.SKPrepareMapTextureListener;
import com.skobbler.ngx.SKPrepareMapTextureThread;
import com.skobbler.ngx.util.SKLogging;
import com.skobbler.ngx.versioning.SKMapUpdateListener;
import com.skobbler.ngx.versioning.SKVersioningManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Arthur on 4/19/2016.
 *
 */
public class MainActivity extends AppCompatActivity implements SKPrepareMapTextureListener, SKMapUpdateListener {

    private static final String TAG = "MainActivity ------";

    public static final long KILO = 1024;
    public static final long MEGA = KILO * KILO;

    public static String sMapResourcesDirPath = "";

    public static int mNewMapVersionDetected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String applicationPath = chooseStoragePath(this);

        // determine path where map resources should be copied on the device
        if (applicationPath != null) {
            sMapResourcesDirPath = applicationPath + "/" + "SKMaps/";
        } else {
            Toast.makeText(this, "Application path is empty", Toast.LENGTH_LONG).show();
            finish();
        }
        ((SKApplication)getApplicationContext()).getSharedPreferences().saveStringPreference("mapResourcesPath", sMapResourcesDirPath);
        ((SKApplication)getApplication()).setMapResourcesDirPath(sMapResourcesDirPath);

        if (!new File(sMapResourcesDirPath).exists()) {
            new SKPrepareMapTextureThread(this, sMapResourcesDirPath, "SKMaps.zip", this).start();
            copyOtherResources();
            prepareMapCreatorFile();
        } else {
            Toast.makeText(MainActivity.this, "Map resources copied in a previous run", Toast.LENGTH_SHORT).show();
            prepareMapCreatorFile();
            SKUtils.initializeLibrary(this);
            SKVersioningManager.getInstance().setMapUpdateListener(this);
            goToMap();
        }
    }

    @Override
    public void onMapTexturesPrepared(boolean prepared) {

        SKVersioningManager.getInstance().setMapUpdateListener(this);
        Toast.makeText(MainActivity.this, "Map resources were copied", Toast.LENGTH_SHORT).show();

        if (SKUtils.initializeLibrary(this)) {
            goToMap();
        }
    }


    @Override
    public void onMapVersionSet(int newVersion) {

    }

    @Override
    public void onNewVersionDetected(int newVersion) {
        mNewMapVersionDetected = newVersion;
    }

    @Override
    public void onNoNewVersionDetected() {

    }

    @Override
    public void onVersionFileDownloadTimeout() {

    }

    private void goToMap() {
        finish();
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("mapResourcesPath", sMapResourcesDirPath);
        startActivity(intent);

        Toast.makeText(MainActivity.this, "Map prepared", Toast.LENGTH_SHORT).show();
    }

    public static String chooseStoragePath(Context context) {
        if (getAvailableMemorySize(Environment.getDataDirectory().getPath()) >= 50 * MEGA) {
            if (context != null && context.getFilesDir() != null) {
                return context.getFilesDir().getPath();
            }
        } else {
            if ((context != null) && (context.getExternalFilesDir(null) != null)) {
                if (getAvailableMemorySize(context.getExternalFilesDir(null).toString()) >= 50 * MEGA) {
                    return context.getExternalFilesDir(null).toString();
                }
            }
        }

        SKLogging.writeLog(TAG, "There is not enough memory on any storage, but return internal memory",
                SKLogging.LOG_DEBUG);

        if (context != null && context.getFilesDir() != null) {
            return context.getFilesDir().getPath();
        } else {
            if ((context != null) && (context.getExternalFilesDir(null) != null)) {
                return context.getExternalFilesDir(null).toString();
            } else {
                return null;
            }
        }
    }

    /**
     * get the available internal memory size
     *
     * @return available memory size in bytes
     */
    public static long getAvailableMemorySize(String path) {
        StatFs statFs = null;
        try {
            statFs = new StatFs(path);
        } catch (IllegalArgumentException ex) {
            SKLogging.writeLog("SplashActivity", "Exception when creating StatF ; message = " + ex,
                    SKLogging.LOG_DEBUG);
        }
        if (statFs != null) {
            Method getAvailableBytesMethod = null;
            try {
                getAvailableBytesMethod = statFs.getClass().getMethod("getAvailableBytes");
            } catch (NoSuchMethodException e) {
                SKLogging.writeLog(TAG, "Exception at getAvailableMemorySize method = " + e.getMessage(),
                        SKLogging.LOG_DEBUG);
            }

            if (getAvailableBytesMethod != null) {
                try {
                    SKLogging.writeLog(TAG, "Using new API for getAvailableMemorySize method !!!", SKLogging.LOG_DEBUG);
                    return (Long) getAvailableBytesMethod.invoke(statFs);
                } catch (IllegalAccessException e) {
                    return (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
                } catch (InvocationTargetException e) {
                    return (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
                }
            } else {
                return (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
            }
        } else {
            return 0;
        }
    }

    /**
     * Copy some additional resources from assets
     */
    private void copyOtherResources() {
        new Thread() {

            public void run() {
                try {
                    String tracksPath = sMapResourcesDirPath + "GPXTracks";
                    File tracksDir = new File(tracksPath);
                    if (!tracksDir.exists()) {
                        tracksDir.mkdirs();
                    }
                    SKUtils.copyAssetsToFolder(getAssets(), "GPXTracks", sMapResourcesDirPath + "GPXTracks");

                    String imagesPath = sMapResourcesDirPath + "images";
                    File imagesDir = new File(imagesPath);
                    if (!imagesDir.exists()) {
                        imagesDir.mkdirs();
                    }
                    SKUtils.copyAssetsToFolder(getAssets(), "images", sMapResourcesDirPath + "images");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Copies the map creator file and logFile from assets to a storage.
     */
    private void prepareMapCreatorFile() {
        final Thread prepareGPXFileThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    final String mapCreatorFolderPath = sMapResourcesDirPath + "MapCreator";
                    final File mapCreatorFolder = new File(mapCreatorFolderPath);
                    // create the folder where you want to copy the json file
                    if (!mapCreatorFolder.exists()) {
                        mapCreatorFolder.mkdirs();
                    }
                    SKUtils.copyAsset(getAssets(), "MapCreator", mapCreatorFolderPath, "mapcreatorFile.json");
                    // Copies the log file from assets to a storage.
                    final String logFolderPath = sMapResourcesDirPath + "logFile";
                    final File logFolder = new File(logFolderPath);
                    if (!logFolder.exists()) {
                        logFolder.mkdirs();
                    }
                    SKUtils.copyAsset(getAssets(), "logFile", logFolderPath, "Seattle.log");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        prepareGPXFileThread.start();
    }
}
