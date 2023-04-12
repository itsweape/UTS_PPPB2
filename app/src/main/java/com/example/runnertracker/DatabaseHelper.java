package com.example.runnertracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tracks_db";
    private static final int DATABASE_VERSION = 1;

    public static final String TRACK_TABLE = "track";
    public static final String TRACK_ID = "trackID";
    public static final String TRACK_DURATION = "duration";
    public static final String TRACK_DISTANCE = "distance";
    public static final String TRACK_NAME = "name";
    public static final String TRACK_DATE = "date";

    public static final String LOCATION_TABLE = "location";
    public static final String LOCATION_ID = "locationID";
    public static final String LOCATION_JID = "trackID";
    public static final String LOCATION_ALTITUDE = "altitude";
    public static final String LOCATION_LONGITUDE = "longitude";
    public static final String LOCATION_LATITUDE = "latitude";

    public static final String AUTHORITY = "com.example.runnertracker.TrackProvider";

    public static final Uri ALL_URI = Uri.parse("content://"+AUTHORITY+"");
    public static final Uri TRACK_URI = Uri.parse("content://"+AUTHORITY+"/track");
    public static final Uri LOCATION_URI = Uri.parse("content://"+AUTHORITY+"/location");

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE_TRACK = "CREATE TABLE " + TRACK_TABLE + "(" + TRACK_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + TRACK_DURATION + " BIGINT NOT NULL, " + TRACK_DISTANCE + " REAL NOT NULL, " + TRACK_NAME + " varchar(256) NOT NULL DEFAULT 'Recorded Track', " + TRACK_DATE + " DATETIME NOT NULL)";
        sqLiteDatabase.execSQL(CREATE_TABLE_TRACK);

        String CREATE_TABLE_LOCATION = "CREATE TABLE " + LOCATION_TABLE + "(" + LOCATION_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                + LOCATION_JID + " INTEGER NOT NULL, " + LOCATION_ALTITUDE + " REAL NOT NULL, " + LOCATION_LONGITUDE + " REAL NOT NULL, " + LOCATION_LATITUDE + " REAL NOT NULL, "
                + " CONSTRAINT fk1 FOREIGN KEY (LOCATION_JID) REFERENCES track (TRACK_ID) ON DELETE CASCADE)";
        sqLiteDatabase.execSQL(CREATE_TABLE_LOCATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_NAME);
        onCreate(sqLiteDatabase);
    }
}
