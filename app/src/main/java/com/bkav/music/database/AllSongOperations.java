package com.bkav.music.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.bkav.music.model.SongsList;

import java.util.ArrayList;

public class AllSongOperations {
    public static final String TAG = "ALL Song Database";

    SQLiteOpenHelper dbAllSongHelper;
    SQLiteDatabase sqLiteDatabaseAllSong;

    private static final String[] allColumns = {
            DatabaseHandler.COLUMN_ID,
            DatabaseHandler.COLUMN_TITLE,
            DatabaseHandler.COLUMN_SUBTITLE,
            DatabaseHandler.COLUMN_DURATION,
            DatabaseHandler.COLUMN_PATH,
            DatabaseHandler.COLUMN_IMAGE,
            DatabaseHandler.COLUMN_OF_PLAY,
            DatabaseHandler.COLUMN_IS_LIKE,
            DatabaseHandler.COLUMN_PLAY
    };

    public AllSongOperations(Context context) {
        dbAllSongHelper = new DatabaseHandler(context);
    }

    public void open() {
        Log.i(TAG, " Database Opened");
        sqLiteDatabaseAllSong = dbAllSongHelper.getWritableDatabase();
    }

    public void close() {
        Log.i(TAG, "Database Closed");
        dbAllSongHelper.close();
    }

    public void addAllSong(SongsList songsList) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.COLUMN_TITLE, songsList.getTitle());
        values.put(DatabaseHandler.COLUMN_SUBTITLE, songsList.getSubTitle());
        values.put(DatabaseHandler.COLUMN_DURATION, songsList.getDuration());
        values.put(DatabaseHandler.COLUMN_PATH, songsList.getPath());
        values.put(DatabaseHandler.COLUMN_IMAGE, songsList.getImage());
        values.put(DatabaseHandler.COLUMN_OF_PLAY, songsList.getCountOfPlay());
        values.put(DatabaseHandler.COLUMN_IS_LIKE, songsList.isLike());
        values.put(DatabaseHandler.COLUMN_PLAY, songsList.getPlay());

        sqLiteDatabaseAllSong.insertWithOnConflict(
                DatabaseHandler.TABLE_ALL_SONGS,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE);
        close();
    }

    public void replaceSong(SongsList songsList) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.COLUMN_TITLE, songsList.getTitle());
        values.put(DatabaseHandler.COLUMN_SUBTITLE, songsList.getSubTitle());
        values.put(DatabaseHandler.COLUMN_DURATION, songsList.getDuration());
        values.put(DatabaseHandler.COLUMN_PATH, songsList.getPath());
        values.put(DatabaseHandler.COLUMN_IMAGE, songsList.getImage());
        values.put(DatabaseHandler.COLUMN_OF_PLAY, songsList.getCountOfPlay());
        values.put(DatabaseHandler.COLUMN_IS_LIKE, songsList.isLike());
        values.put(DatabaseHandler.COLUMN_PLAY, songsList.getPlay());
        
        sqLiteDatabaseAllSong.replace(DatabaseHandler.TABLE_ALL_SONGS, null, values);
        
        close();
    }

    public void updateSong(SongsList songsList) {
        open();
        String whereClause =
                DatabaseHandler.COLUMN_TITLE + "=?";
        String[] whereArgs = new String[]{songsList.getTitle()};
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.COLUMN_IS_LIKE, songsList.isLike());
        values.put(DatabaseHandler.COLUMN_OF_PLAY, songsList.getCountOfPlay());
        values.put(DatabaseHandler.COLUMN_PLAY, songsList.getPlay());

        sqLiteDatabaseAllSong.update(DatabaseHandler.TABLE_ALL_SONGS, values, whereClause, whereArgs);

        close();
    }
    public void updatePlaySong(ArrayList<SongsList> songsList) {
        open();
        String whereClause =
                DatabaseHandler.COLUMN_TITLE + "=?";
        for (SongsList song : songsList) {
            String[] whereArgs = new String[]{song.getTitle()};
            ContentValues values = new ContentValues();
            song.setPlay(0);
            values.put(DatabaseHandler.COLUMN_PLAY, song.getPlay());
            sqLiteDatabaseAllSong.update(DatabaseHandler.TABLE_ALL_SONGS, values, whereClause, whereArgs);
        }
        close();
    }

    public ArrayList<SongsList> getAllSong() {
        open();
        Cursor cursor = sqLiteDatabaseAllSong.query(DatabaseHandler.TABLE_ALL_SONGS, allColumns,
                null, null, null, null, null);
        ArrayList<SongsList> allSongs = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                SongsList songsList = new SongsList(cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_TITLE))
                        , cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_SUBTITLE))
                        , cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_DURATION))
                        , cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_PATH))
                        , cursor.getLong(cursor.getColumnIndex(DatabaseHandler.COLUMN_IMAGE))
                        , cursor.getInt(cursor.getColumnIndex(DatabaseHandler.COLUMN_OF_PLAY))
                        , cursor.getInt(cursor.getColumnIndex(DatabaseHandler.COLUMN_IS_LIKE))
                        , cursor.getInt(cursor.getColumnIndex(DatabaseHandler.COLUMN_PLAY)));
                allSongs.add(songsList);
            }
        }
        close();
        return allSongs;
    }
    public void removeAllSong(String songTitle) {
        open();
        String whereClause =
                DatabaseHandler.COLUMN_TITLE + "=?";
        String[] whereArgs = new String[]{songTitle};

        sqLiteDatabaseAllSong.delete(DatabaseHandler.TABLE_SONGS, whereClause, whereArgs);
        close();
    }

}

