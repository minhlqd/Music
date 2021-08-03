package com.example.music.interfaces;

import com.example.music.model.Song;

import java.util.ArrayList;

public interface ICreateDataParseSong {
    void onDataPassSong(String name, String path, String subtitle, long image, boolean checkSong);

    void fullSongList(ArrayList<Song> songList, int position);

    void playCheckSong(boolean checkSong);

    void currentSong(Song song, int position);
    void getLength(int length);

    String queryText();

    boolean isSong();
    boolean checkScreen();
}
