package com.example.runnertracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class TrackProvider extends ContentProvider {
    DatabaseHelper databaseHelper;
    SQLiteDatabase db;

    private static final UriMatcher matcher;

    static {
        matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(DatabaseHelper.AUTHORITY, "track", 1);
        matcher.addURI(DatabaseHelper.AUTHORITY, "track/#", 2);
        matcher.addURI(DatabaseHelper.AUTHORITY, "location", 3);
        matcher.addURI(DatabaseHelper.AUTHORITY, "location/#", 4);
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(this.getContext());
        db = databaseHelper.getWritableDatabase();
        return (db != null);
    }

    @Override
    public String getType(Uri uri) {
        if (uri.getLastPathSegment() == null) {
            return "vnd.android.cursor.dir/TrackProvider.data.text";
        } else {
            return "vnd.android.cursor.item/TrackProvider.data.text";
        }
    }

    //method untuk insert track data
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String tableName;
        switch(matcher.match(uri)) {
            case 1:
                tableName = "track";
                break;
            case 3:
                tableName = "location";
                break;
            default:
                tableName = "";
        }

        //insert values ke dalam tabel
        long id = db.insert(tableName, null, values);
        Uri newRowUri = ContentUris.withAppendedId(uri, id);

        getContext().getContentResolver().notifyChange(newRowUri, null);
        return newRowUri;
    }

    //method untuk read track data
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[]
            selectionArgs, String sortOrder) {

        switch(matcher.match(uri)) {
            case 2:
                selection = "trackID = " + uri.getLastPathSegment();
            case 1:
                return db.query("track", projection, selection, selectionArgs, null, null, sortOrder);
            case 4:
                selection = "locationID = " + uri.getLastPathSegment();
            case 3:
                return db.query("location", projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[]
            selectionArgs) {
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

}
