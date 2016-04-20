package com.example.arthur.skdemo.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.AssetManager;

import com.example.arthur.skdemo.R;
import com.example.arthur.skdemo.data.SKApplication;
import com.google.common.io.ByteStreams;
import com.skobbler.ngx.SKDeveloperKeyException;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.navigation.SKAdvisorSettings;
import com.skobbler.ngx.util.SKLogging;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Arthur on 4/17/2016.
 *
 */
public class SKUtils {

    /**
     * Initializes the SKMaps framework
     */
    public static boolean initializeLibrary(final Activity context) {
        SKLogging.enableLogs(true);

        // get object holding map initialization settings
        SKMapsInitSettings initMapSettings = new SKMapsInitSettings();

        final String  mapResourcesPath = ((SKApplication)context.getApplicationContext()).getSharedPreferences().getStringPreference("mapResourcesPath");
        // set path to map resources and initial map style
        initMapSettings.setMapResourcesPaths(mapResourcesPath, new SKMapViewStyle(mapResourcesPath + context.getString(R.string.day_dir), context.getString(R.string.day_file)));

        final SKAdvisorSettings advisorSettings = initMapSettings.getAdvisorSettings();
        advisorSettings.setAdvisorConfigPath(mapResourcesPath +"/Advisor");
        advisorSettings.setResourcePath(mapResourcesPath +"/Advisor/Languages");
        advisorSettings.setLanguage(SKAdvisorSettings.SKAdvisorLanguage.LANGUAGE_EN);
        advisorSettings.setAdvisorVoice("en");
        initMapSettings.setAdvisorSettings(advisorSettings);

        try {
            SKMaps.getInstance().initializeSKMaps(context, initMapSettings);
            return true;
        }catch (SKDeveloperKeyException exception){
            exception.printStackTrace();
            showApiKeyErrorDialog(context);
            return false;
        }
    }

    /**
     * Shows the api key not set dialog.
     */
    public static void showApiKeyErrorDialog(Activity currentActivity) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                currentActivity);

        alertDialog.setTitle("Error");
        alertDialog.setMessage("API_KEY not set");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(
                currentActivity.getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });

        alertDialog.show();
    }

    /**
     * Copies files from assets to destination folder
     * @param assetManager
     * @param sourceFolder
     * @throws IOException
     */
    public static void copyAssetsToFolder(AssetManager assetManager, String sourceFolder, String destinationFolder)
            throws IOException {
        final String[] assets = assetManager.list(sourceFolder);

        final File destFolderFile = new File(destinationFolder);
        if (!destFolderFile.exists()) {
            destFolderFile.mkdirs();
        }
        copyAsset(assetManager, sourceFolder, destinationFolder, assets);
    }

    /**
     * Copies files from assets to destination folder
     * @param assetManager
     * @param sourceFolder
     * @param assetsNames
     * @throws IOException
     */
    public static void copyAsset(AssetManager assetManager, String sourceFolder, String destinationFolder,
                                 String... assetsNames) throws IOException {

        for (String assetName : assetsNames) {
            OutputStream destinationStream = new FileOutputStream(new File(destinationFolder + "/" + assetName));
            String[] files = assetManager.list(sourceFolder + "/" + assetName);
            if (files == null || files.length == 0) {

                InputStream asset = assetManager.open(sourceFolder + "/" + assetName);
                try {
                    ByteStreams.copy(asset, destinationStream);
                } finally {
                    asset.close();
                    destinationStream.close();
                }
            }
        }
    }

}
