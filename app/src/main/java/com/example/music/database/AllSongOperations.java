package com.example.music.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.example.music.model.Song;

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

    // them bai hat vao database khi lay tu trong may ra
    public void addAllSong(Song songsList) {
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

    // thay the bai hat
    public void replaceSong(Song song) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.COLUMN_TITLE, song.getTitle());
        values.put(DatabaseHandler.COLUMN_SUBTITLE, song.getSubTitle());
        values.put(DatabaseHandler.COLUMN_DURATION, song.getDuration());
        values.put(DatabaseHandler.COLUMN_PATH, song.getPath());
        values.put(DatabaseHandler.COLUMN_IMAGE, song.getImage());
        values.put(DatabaseHandler.COLUMN_OF_PLAY, song.getCountOfPlay());
        values.put(DatabaseHandler.COLUMN_IS_LIKE, song.isLike());
        values.put(DatabaseHandler.COLUMN_PLAY, song.getPlay());
        
        sqLiteDatabaseAllSong.replace(DatabaseHandler.TABLE_ALL_SONGS, null, values);
        
        close();
    }

    // update like, count_of_play,isPlay cua bai hat
    public void updateSong(Song song) {
        open();
        String whereClause =
                DatabaseHandler.COLUMN_TITLE + "=?";
        String[] whereArgs = new String[]{song.getTitle()};
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.COLUMN_IS_LIKE, song.isLike());
        values.put(DatabaseHandler.COLUMN_OF_PLAY, song.getCountOfPlay());
        values.put(DatabaseHandler.COLUMN_PLAY, song.getPlay());

        sqLiteDatabaseAllSong.update(DatabaseHandler.TABLE_ALL_SONGS, values, whereClause, whereArgs);

        close();
    }

    // set isPlay ve 0
    public void updatePlaySong(ArrayList<Song> songsList) {
        open();
        String whereClause =
                DatabaseHandler.COLUMN_TITLE + "=?";
        for (Song song : songsList) {
            String[] whereArgs = new String[]{song.getTitle()};
            ContentValues values = new ContentValues();
            song.setPlay(0);
            values.put(DatabaseHandler.COLUMN_PLAY, song.getPlay());
            sqLiteDatabaseAllSong.update(DatabaseHandler.TABLE_ALL_SONGS, values, whereClause, whereArgs);
        }
        close();
    }

    // lay tat ca bai hat tu database
    public ArrayList<Song> getAllSong() {
        open();
        Cursor cursor = sqLiteDatabaseAllSong.query(DatabaseHandler.TABLE_ALL_SONGS, allColumns,
                null, null, null, null, null);
        ArrayList<Song> allSongs = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Song songsList = new Song(cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_TITLE))
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

    // xoa bai hat khoi database
    public void removeAllSong(String songTitle) {
        open();
        String whereClause =
                DatabaseHandler.COLUMN_TITLE + "=?";
        String[] whereArgs = new String[]{songTitle};

        sqLiteDatabaseAllSong.delete(DatabaseHandler.TABLE_SONGS, whereClause, whereArgs);
        close();
    }

}

