package com.bkav.music.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bkav.music.activity.MainActivity;
import com.bkav.music.Key;
import com.bkav.music.database.AllSongOperations;
import com.bkav.music.model.SongsList;
import com.bkav.music.service.MediaService;

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