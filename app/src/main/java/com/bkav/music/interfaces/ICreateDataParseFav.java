package com.bkav.music.interfaces;


import com.bkav.music.model.SongsList;

import java.util.ArrayList;

public interface ICreateDataParseFav {
    void onDataPass(String name, String path);

    void fullSongList(ArrayList<SongsList> songList, int position);

    String queryText();
    boolean checkScreen();
}
