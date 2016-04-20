package com.example.arthur.skdemo.data.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Arthur on 4/19/2016.
 * 
 */
public class InfoPoint {

    public static final int NOT_FAVOURITE = -1;
    public static final int FAVOURITE = 1;

    @IntDef(value = {NOT_FAVOURITE, FAVOURITE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FavType {
    }

    public int annotationId;
    public String title;
    public String address;
    public String description;
    public double longitude;
    public double latitude;
    public int favourite;

    public InfoPoint(int id, String title, String address, String description, double longitude, double latitude, @FavType int favourite) {
        this.annotationId = id;
        this.title = title;
        this.address = address;
        this.description = description;
        this.longitude = longitude;
        this.latitude = latitude;
        this.favourite = favourite;
    }
}
