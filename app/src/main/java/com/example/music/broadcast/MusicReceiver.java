package com.example.music.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.music.activity.MainActivity;
import com.example.music.Key;
import com.example.music.database.AllSongOperations;
import com.example.music.model.SongsList;
import com.example.music.service.MediaService;

import java.util.ArrayList;

public class MusicReceiver extends BroadcastReceiver {

    private Intent mIntentService;
    private AllSongOperations mAllSongOperations;
    private ArrayList<SongsList> mSongsList;

    public MusicReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
//        mNotificationService = (INotificationService) context;
        mAllSongOperations = new AllSongOperations(context);
        mSongsList = new ArrayList<>();

//        mSongsList = mAllSongOperations.getAllSong();

        switch (intent.getAction()) {
            case Key.ACTION_NEXT_SONG:{
                break;
            }
            case Key.ACTION_PLAY_SONG:{
                String position = intent.getStringExtra(Key.KEY_POSITION);
                if (MainActivity.mMediaPlayer.isPlaying()) {
                    MainActivity.mMediaPlayer.pause();
                } else {
                    MainActivity.mMediaPlayer.start();
                    MainActivity.playCycle();
                }
                Log.d("receiver", "onReceive: " + position);
                mIntentService = new Intent(context, MediaService.class);
                intent.putExtra(Key.KEY_POSITION, Integer.parseInt(position));
                context.startService(mIntentService);
                break;
            }
            case Key.ACTION_PREVIOUS_SONG:{
                Log.d("receiver", "onReceive: ");
                break;
            }
            default: break;
        }
    }

}