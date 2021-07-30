package com.bkav.music.interfaces;

import com.bkav.music.model.SongsList;

import java.util.ArrayList;

public interface ICreateDataParseSong {
    void onDataPassSong(String name, String path, String subtitle, long image, boolean checkSong);

    void fullSongList(ArrayList<SongsList> songList, int position);

    void playCheckSong(boolean checkSong);

    void currentSong(SongsList songsList);
    void getLength(int length);

    String queryText();
    int getPositionSong();
    boolean isSong();
    boolean checkScreen();
}
