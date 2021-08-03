package com.example.music.broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.music.R;
import com.example.music.activity.MainActivity;
import com.example.music.Key;
import com.example.music.database.AllSongOperations;
import com.example.music.model.Song;
import com.example.music.service.MusicService;

import java.util.ArrayList;

public class MusicReceiver extends BroadcastReceiver {

    private Intent mIntentService;
    private AllSongOperations mAllSongOperations;
    private ArrayList<Song> mSongsList;
    private int mPosition;

    public MusicReceiver() {
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onReceive(Context context, Intent intent) {
//        mNotificationService = (INotificationService) context;
        mAllSongOperations = new AllSongOperations(context);
        mSongsList = new ArrayList<>();

        mSongsList = mAllSongOperations.getAllSong();
//        String title = intent.getStringExtra(Key.CONST_TITLE);
//        for (int i=0; i< mSongsList.size(); i++) {
//            Log.d("aaa", "onReceive: " + title);
//            Log.d("aaa", "title " + mSongsList.get(i).getTitle());
//            if (mSongsList.get(i).getTitle().equals(title)){
//                Log.d("aaa", "onReceive: " + mPosition);
//                mPosition = i;
//                break;
//            }
//        }
        int position = intent.getIntExtra(Key.KEY_POSITION, 0);
        Log.d("aaa", "onReceive: " + position);
        Log.d("aaa", "onReceive: " + intent.getAction());

        switch (intent.getAction()) {
            case Key.ACTION_LIKE_SONG:{
                break;
            }
            case Key.ACTION_DISLIKE_SONG:{
                Log.d("music", "onReceive: ");
                break;
            }
            case Key.ACTION_NEXT_SONG:{
                mIntentService = new Intent(context, MusicService.class);
                mIntentService.putExtra(Key.KEY_POSITION, mPosition + 1);
                context.startService(mIntentService);
                break;
            }
            case Key.ACTION_PLAY_SONG:{
                if (MainActivity.mediaPlayer.isPlaying()) {
                    MainActivity.mediaPlayer.pause();
                    MainActivity.playPauseSong.setImageResource(R.drawable.ic_play_black);
                    MainActivity.btnPlayPause.setImageResource(R.drawable.ic_play_black);
                    MainActivity.btnPlayPause.setBackground(context.getDrawable(R.color.background));

                } else {
                    MainActivity.mediaPlayer.start();
                    MainActivity.playCycle();
                    MainActivity.playPauseSong.setImageResource(R.drawable.ic_pause_black);
                    MainActivity.btnPlayPause.setImageResource(R.drawable.pause_icon);
                    MainActivity.btnPlayPause.setBackground(context.getDrawable(R.drawable.background_play_pause));
                }
                //Log.d("receiver", "onReceive: " + title + " " + mPosition);
                mIntentService = new Intent(context, MusicService.class);
                mIntentService.putExtra(Key.KEY_POSITION, mPosition);
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