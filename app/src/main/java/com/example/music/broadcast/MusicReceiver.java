package com.example.music.broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music.R;
import com.example.music.activity.MainActivity;
import com.example.music.Key;
import com.example.music.database.AllSongOperations;
import com.example.music.database.FavoritesOperations;
import com.example.music.interfaces.INotification;
import com.example.music.model.Song;
import com.example.music.service.MusicService;

import java.util.ArrayList;

public class MusicReceiver extends BroadcastReceiver {

    private Intent mIntentService;

    private AllSongOperations mAllSongOperations;
    private FavoritesOperations mFavoritesOperations;

    private ArrayList<Song> mSongsList;
    private int mPosition;

    private INotification iNotification;

    public void setINotification(INotification iNotification) {
        this.iNotification = iNotification;
    }

    public MusicReceiver() {
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
        mAllSongOperations = new AllSongOperations(context);
        mFavoritesOperations = new FavoritesOperations(context);

        mSongsList = new ArrayList<>();

        mSongsList = mAllSongOperations.getAllSong();

        mPosition = intent.getIntExtra(Key.KEY_POSITION, 0);

        Intent intentActivity = new Intent(context, MainActivity.class).setAction(intent.getAction());
        intentActivity.putExtra(Key.KEY_POSITION, mPosition);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        switch (intent.getAction()) {
            case Key.ACTION_LIKE_SONG:{
                Song song = mSongsList.get(mPosition);

                if (song.isLike() == 1) {
                    song.setLike(0);
                    mFavoritesOperations.removeSong(song.getTitle());
                } else {
                    song.setLike(1);
                    mFavoritesOperations.addSongFav(song);
                }

                mAllSongOperations.updateSong(song);
                MainActivity.sMusicService.sendNotification(context, song, mPosition);
                break;
            }
            case Key.ACTION_DISLIKE_SONG:{
                Song song = mSongsList.get(mPosition);
                if (song.isLike() == 2) {
                    song.setLike(0);
                } else {
                    if (song.isLike() == 1) {
                        mFavoritesOperations.removeSong(song.getTitle());
                    }
                    song.setLike(2);
                }
                mAllSongOperations.updateSong(song);
                MainActivity.sMusicService.sendNotification(context, song, mPosition);
                break;
            }
            case Key.ACTION_NEXT_SONG:{
                mSongsList.get(mPosition).setPlay(0);
                mAllSongOperations.updateSong(mSongsList.get(mPosition));

                int position = mPosition + 1;
                if (position == mSongsList.size()) {
                    position = 0;
                }
                Song song = mSongsList.get(position);
                song.setPlay(1);
                song.setCountOfPlay(song.getCountOfPlay() + 1);
                if (song.getCountOfPlay() == 3) {
                    song.setLike(1);
                    mFavoritesOperations.addSongFav(song);
                }

                mAllSongOperations.updateSong(song);
                MainActivity.sMusicService.playMedia(song);
                MainActivity.sMusicService.sendNotification(context, song, position);

                mPosition = position;
                break;
            }
            case Key.ACTION_PLAY_SONG:{
                if (MainActivity.sMusicService.isPlaying()) {
                    mSongsList.get(mPosition).setPlay(0);
                    MainActivity.sMusicService.pause();
                } else {
                    mSongsList.get(mPosition).setPlay(1);
                    MainActivity.sMusicService.play();
                }
                mAllSongOperations.updateSong(mSongsList.get(mPosition));
                MainActivity.sMusicService.sendNotification(context, mSongsList.get(mPosition), mPosition);
                break;
            }
            case Key.ACTION_PREVIOUS_SONG:{
                mSongsList.get(mPosition).setPlay(0);
                mAllSongOperations.updateSong(mSongsList.get(mPosition));

                int position = mPosition - 1;
                if (position == -1) {
                    position = mSongsList.size()-1;
                }

                Song song = mSongsList.get(position);

                mIntentService = new Intent(context, MusicService.class);
                mIntentService.putExtra(Key.KEY_POSITION, position);

                song.setPlay(1);
                song.setCountOfPlay(song.getCountOfPlay() + 1);
                if (song.getCountOfPlay() == 3) {
                    song.setLike(1);
                    mFavoritesOperations.addSongFav(song);
                }
                mAllSongOperations.updateSong(song);
                MainActivity.sMusicService.playMedia(mSongsList.get(position));
                MainActivity.sMusicService.sendNotification(context, song, position);

                mPosition = position;
                break;
            }
            default: break;
        }
    }

}