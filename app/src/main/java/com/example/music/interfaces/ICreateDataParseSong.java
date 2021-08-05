package com.example.music.interfaces;

import com.example.music.model.Song;

import java.util.ArrayList;

public interface ICreateDataParseSong {
    // void onDataPassSong(String name, String path, String subtitle, long image, boolean checkSong);


    void onDataPassSong(Song song);


    void fullSongList(ArrayList<Song> songList, int position);


    void currentSong(Song song, int position);
    void getLength(int length);

    String queryText();

    boolean isSong();
    boolean checkScreen();
}
