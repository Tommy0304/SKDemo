package com.example.arthur.skdemo.networking;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.arthur.skdemo.data.model.InfoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arthur on 4/20/2016.
 *
 */
public class DatabaseManager extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Map.db";
    public static final String TABLE_NAME = "my_info_point";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_ANNOTATION_ID = "ANNOTATION_ID";
    public static final String COLUMN_TITLE = "TITLE";
    public static final String COLUMN_ADDRESS = "ADDRESS";
    public static final String COLUMN_DESCRIPTION = "DESCRIPTION";
    public static final String COLUMN_LONGITUDE = "LONGITUDE";
    public static final String COLUMN_LATITUDE = "LATITUDE";
    public static final String COLUMN_FAVOURITE = "FAVOURITE";

    private static DatabaseManager instance;

    private SQLiteDatabase db;

    public static DatabaseManager getInstance(Context context) {

        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    private DatabaseManager(Context context) {

        super(context, DATABASE_NAME, null, 1);
        db = this.getWritableDatabase();
        onCreate(db);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ANNOTATION_ID + " INTEGER, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_LONGITUDE + " REAL, "
                + COLUMN_LATITUDE + " REAL, "
                + COLUMN_FAVOURITE + " INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertInfoPoint(InfoPoint infoPoint) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ANNOTATION_ID, infoPoint.annotationId);
        contentValues.put(COLUMN_TITLE, infoPoint.title);
        contentValues.put(COLUMN_ADDRESS, infoPoint.address);
        contentValues.put(COLUMN_DESCRIPTION, infoPoint.description);
        contentValues.put(COLUMN_LONGITUDE, infoPoint.longitude);
        contentValues.put(COLUMN_LATITUDE, infoPoint.latitude);
        contentValues.put(COLUMN_FAVOURITE, infoPoint.favourite);
        return (db.insert(TABLE_NAME, null, contentValues) != -1);
    }

    public List<InfoPoint> getAllInfoPoints() {

        List<InfoPoint> list = new ArrayList<>();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        while (cursor.moveToNext()) {
            list.add(new InfoPoint(cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getDouble(5), cursor.getDouble(6), cursor.getInt(7)));
        }
        cursor.close();

        return list;
    }

    public boolean updateInfoPoint(InfoPoint infoPoint) {

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_TITLE, infoPoint.title);
        contentValues.put(COLUMN_ADDRESS, infoPoint.address);
        contentValues.put(COLUMN_DESCRIPTION, infoPoint.description);
        contentValues.put(COLUMN_LONGITUDE, infoPoint.longitude);
        contentValues.put(COLUMN_LATITUDE, infoPoint.latitude);
        contentValues.put(COLUMN_FAVOURITE, infoPoint.favourite);
        return (db.update(TABLE_NAME, contentValues, COLUMN_ANNOTATION_ID + " = ?", new String[] { Integer.toString(infoPoint.annotationId) }) != 0);
    }

    public boolean deleteInfoPoint(int id) {

        return (db.delete(TABLE_NAME, COLUMN_ANNOTATION_ID + " = ?", new String[] { Integer.toString(id) }) != 0);
    }
}
