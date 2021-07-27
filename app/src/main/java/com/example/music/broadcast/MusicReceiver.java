package com.example.music.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.music.activity.MainActivity;
import com.example.music.Key;

public class MusicReceiver extends BroadcastReceiver {



//    public MusicReceiver(Context context){
//        mNotificationService = (INotificationService) context;
//    }
    public MusicReceiver() {
    }


    @Override
    public void onReceive(Context context, Intent intent) {
//        mNotificationService = (INotificationService) context;

        switch (intent.getAction()) {
            case Key.ACTION_NEXT_SONG:{
                break;
            }
            case Key.ACTION_PLAY_SONG:{
                MainActivity.mediaPlayer.pause();
//                mNotificationService.isPlayNotification(false);
                Log.d("receiver", "onReceive: " + Key.ACTION_PLAY_SONG);

                break;
            }
            case Key.ACTION_PREVIOUS_SONG:{
                Log.d("receiver", "onReceive: ");
                break;
            }
        }

    }
}