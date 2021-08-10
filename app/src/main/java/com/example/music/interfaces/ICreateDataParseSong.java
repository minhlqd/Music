package com.example.music.interfaces;

import android.widget.Adapter;

import com.example.music.adapter.SongAdapter;
import com.example.music.model.Song;

import java.util.ArrayList;

public interface ICreateDataParseSong {
    void onDataPassSong(Song song);


    void fullSongList(ArrayList<Song> songList, int position);

    void currentSong(Song song, int position);
    void getLength(int length);

    String queryText();

    boolean isSong();
    boolean checkScreen();
}
