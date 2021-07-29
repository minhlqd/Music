package com.example.music.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "uet.music.player";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SONGS = "favorites";
    public static final String TABLE_ALL_SONGS = "songs";
    public static final String COLUMN_ID = "songID";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_SUBTITLE = "subtitle";
    public static final String COLUMN_DURATION = "duration";
    public static final String COLUMN_PATH = "songpath";
    public static final String COLUMN_IMAGE = "image";
    public static final String COLUMN_OF_PLAY = "count";
    public static final String COLUMN_IS_LIKE = "isLike";
    public static final String COLUMN_PLAY = "play";

    private static final String TABLE_CREATE = "CREATE TABLE " + TABLE_SONGS + " ("
            + COLUMN_ID + " INTEGER, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_SUBTITLE + " TEXT, "
            + COLUMN_DURATION + " TEXT, "
            + COLUMN_PATH + " TEXT PRIMARY KEY, "
            + COLUMN_IMAGE + " INTEGER, "
            + COLUMN_OF_PLAY + " INTEGER, "
            + COLUMN_IS_LIKE + " INTEGER, "
            + COLUMN_PLAY + " INTEGER " + ")";

    private static final String TABLE_All_SONG_CREATE = "CREATE TABLE " + TABLE_ALL_SONGS + " ("
            + COLUMN_ID + " INTEGER, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_SUBTITLE + " TEXT, "
            + COLUMN_DURATION + " TEXT, "
            + COLUMN_PATH + " TEXT PRIMARY KEY, "
            + COLUMN_IMAGE + " INTEGER, "
            + COLUMN_OF_PLAY + " INTEGER, "
            + COLUMN_IS_LIKE + " INTEGER, "
            + COLUMN_PLAY + " INTEGER " + ")";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL(TABLE_All_SONG_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SONGS);
        db.execSQL(TABLE_CREATE);

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALL_SONGS);
        db.execSQL(TABLE_All_SONG_CREATE);
    }
}
