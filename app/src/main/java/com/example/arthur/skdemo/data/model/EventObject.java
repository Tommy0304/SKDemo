package com.example.arthur.skdemo.data.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Arthur on 4/19/2016.
 *
 */
public class EventObject {

    public static final int INFO_POINT_CANCEL = 0;
    public static final int INFO_POINT_CREATE = 1;
    public static final int INFO_POINT_EDIT = 4;
    public static final int MAP_STYLE_CANCEL = 2;
    public static final int MAP_STYLE_SUBMIT = 3;

    @IntDef(value = {INFO_POINT_CANCEL, INFO_POINT_CREATE, INFO_POINT_EDIT, MAP_STYLE_CANCEL, MAP_STYLE_SUBMIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
    }

    public int tag;
    public int annotationId;
    public String title = null;
    public String address = null;
    public String description = null;
    public double longitude;
    public double latitude;
    public int favourite;
    public int mapStyle;

    public EventObject(@EventType int tag) {
        this.tag = tag;
    }
}
