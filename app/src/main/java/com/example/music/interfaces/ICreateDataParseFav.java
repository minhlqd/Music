package com.example.music.interfaces;


import com.example.music.model.Song;

import java.util.ArrayList;

public interface ICreateDataParseFav {
    void onDataPass(Song song);

    void fullSongList(ArrayList<Song> songList, int position);

    String queryText();
    boolean checkScreen();
}
