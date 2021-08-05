package com.example.music.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.example.music.model.Song;

import java.util.ArrayList;

public class FavoritesOperations {

    public static final String TAG = "Favorites Database";

    SQLiteOpenHelper dbHandler;
    SQLiteDatabase database;

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

    public FavoritesOperations(Context context) {
        dbHandler = new DatabaseHandler(context);
    }

    public void open() {
        Log.i(TAG, " Database Opened");
        database = dbHandler.getWritableDatabase();
    }

    public void close() {
        Log.i(TAG, "Database Closed");
        dbHandler.close();
    }

    // them bai hat vao danh sach yeu thich
    public void addSongFav(Song song) {
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

        database.insertWithOnConflict(DatabaseHandler.TABLE_SONGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        close();
    }

    // lay tat ca bai hat yeu thich
    public ArrayList<Song> getAllFavorites() {
        open();
        @SuppressLint("Recycle")
        Cursor cursor = database.query(DatabaseHandler.TABLE_SONGS, allColumns,
                null, null, null, null, null);
        ArrayList<Song> favSongs = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                Song song = new Song(cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_TITLE))
                        , cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_SUBTITLE))
                        , cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_DURATION))
                        , cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_PATH))
                        , cursor.getLong(cursor.getColumnIndex(DatabaseHandler.COLUMN_IMAGE))
                        , cursor.getInt(cursor.getColumnIndex(DatabaseHandler.COLUMN_OF_PLAY))
                        , cursor.getInt(cursor.getColumnIndex(DatabaseHandler.COLUMN_IS_LIKE))
                        , cursor.getInt(cursor.getColumnIndex(DatabaseHandler.COLUMN_PLAY)));
                favSongs.add(song);
            }
        }
        close();
        return favSongs;
    }

    // kiem tra xem bai hat da co trong dach sach chua. neu co roi thi ta se ko them vao nua
    public boolean checkFavorites(String title) {
        open();
        @SuppressLint("Recycle")
        Cursor cursor = database.query(DatabaseHandler.TABLE_SONGS, allColumns,
                null, null, null, null, null);
        ArrayList<Song> favSongs = new ArrayList<>();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_TITLE)).equals(title)) {
                    close();
                    return true;
                }
            }
        }
        close();
        return false;
    }

    public void setPlayMusic(ArrayList<Song> songsList) {
        open();
        String whereClause =
                DatabaseHandler.COLUMN_TITLE + "=?";
        for (Song song : songsList) {
            String[] whereArgs = new String[]{song.getTitle()};
            ContentValues values = new ContentValues();
            song.setPlay(0);
            values.put(DatabaseHandler.COLUMN_PLAY, song.getPlay());
            database.update(DatabaseHandler.TABLE_SONGS, values, whereClause, whereArgs);
        }

        close();
    }

    // xoa bai hat khoi danh sach yeu thich
    public void removeSong(String title) {
        open();
        String whereClause =
                DatabaseHandler.COLUMN_TITLE + "=?";
        String[] whereArgs = new String[]{title};
        database.delete(DatabaseHandler.TABLE_SONGS, whereClause, whereArgs);
        close();
    }

}






















