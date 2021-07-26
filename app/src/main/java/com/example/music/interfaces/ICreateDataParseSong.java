package com.example.music.interfaces;



import com.example.music.model.SongsList;

import java.util.ArrayList;

public interface ICreateDataParseSong {
    void onDataPassSong(String name, String path, boolean checkSong);

    void fullSongList(ArrayList<SongsList> songList, int position);

    String queryText();
    int getPositionSong();
    boolean isSong();

    void playCheckSong(boolean checkSong);

    void currentSong(SongsList songsList);
    void getLength(int length);
}
