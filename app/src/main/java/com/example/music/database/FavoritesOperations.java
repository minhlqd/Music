package com.example.music.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import com.example.music.model.SongsList;

import java.util.ArrayList;
import java.util.Arrays;

public class FavoritesOperations {

    public static final String TAG = "Favorites Database";

    SQLiteOpenHelper dbHandler;
    SQLiteDatabase database;

    private static final String[] allColumns = {
            DatabaseHandler.COLUMN_ID,
            DatabaseHandler.COLUMN_TITLE,
            DatabaseHandler.COLUMN_SUBTITLE,
            DatabaseHandler.COLUMN_PATH,
            DatabaseHandler.COLUMN_IMAGE,
            DatabaseHandler.COLUMN_OF_PLAY,
            DatabaseHandler.COLUMN_IS_LIKE
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

    public void addSongFav(SongsList songsList) {
        open();
        ContentValues values = new ContentValues();
        values.put(DatabaseHandler.COLUMN_TITLE, songsList.getTitle());
        values.put(DatabaseHandler.COLUMN_SUBTITLE, songsList.getSubTitle());
        values.put(DatabaseHandler.COLUMN_PATH, songsList.getPath());
        values.put(DatabaseHandler.COLUMN_IMAGE, songsList.getImage());
        values.put(DatabaseHandler.COLUMN_OF_PLAY, songsList.getCountOfPlay());
        values.put(DatabaseHandler.COLUMN_IS_LIKE, songsList.isLike());

        database.insertWithOnConflict(DatabaseHandler.TABLE_SONGS, null, values, SQLiteDatabase.CONFLICT_REPLACE);

        Log.d(TAG, "addSongFav: " + values);
        close();
    }

    public ArrayList<SongsList> getAllFavorites() {
        open();

        @SuppressLint("Recycle")
        Cursor cursor = database.query(DatabaseHandler.TABLE_SONGS, allColumns,
                null, null, null, null, null);
        ArrayList<SongsList> favSongs = new ArrayList<>();
        Log.d(TAG, "getAllFavorites: " + cursor.getCount());
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                SongsList songsList = new SongsList(cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_TITLE))
                        , cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_SUBTITLE))
                        , cursor.getString(cursor.getColumnIndex(DatabaseHandler.COLUMN_PATH))
                        , cursor.getInt(cursor.getColumnIndex(DatabaseHandler.COLUMN_IMAGE))
                        , cursor.getInt(cursor.getColumnIndex(DatabaseHandler.COLUMN_OF_PLAY))
                        , cursor.getInt(cursor.getColumnIndex(DatabaseHandler.COLUMN_IS_LIKE)));
                favSongs.add(songsList);
            }
        }
        close();
        return favSongs;
    }

    public void removeSong(String songPath) {
        open();
        String whereClause =
                DatabaseHandler.COLUMN_TITLE + "=?";
        String[] whereArgs = new String[]{songPath};
        Log.d(TAG, "removeSong: " + whereClause + Arrays.toString(whereArgs));
        database.delete(DatabaseHandler.TABLE_SONGS, whereClause, whereArgs);
        close();
    }

}






















