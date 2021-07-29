package com.example.music.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.music.activity.MainActivity;
import com.example.music.broadcast.MusicReceiver;
import com.example.music.Key;
import com.example.music.R;
import com.example.music.database.AllSongOperations;
import com.example.music.model.SongsList;

import java.util.ArrayList;

import static com.example.music.Key.CHANNEL_ID;

public class MediaService extends Service {

    int image_like;
    int image_dislike;
    int image_play_pause;
    private AllSongOperations mAllSongOperations;
    private ArrayList<SongsList> mSongsList;

    public MediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public class MusicBinder extends Binder {
        public MediaService getSerVice() {
            return MediaService.this;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        mAllSongOperations = new AllSongOperations(newBase);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSongsList = new ArrayList<>();
        mSongsList = mAllSongOperations.getAllSong();

        int position = intent.getIntExtra(Key.KEY_POSITION, 0);
        SongsList songsList = mSongsList.get(position);
        String title = songsList.getTitle();
        String subtitle = songsList.getSubTitle();
        String image = songsList.getImage();
        int like = songsList.isLike();
        sendNotification(title, subtitle, like, image, position);

        return START_NOT_STICKY;
    }

    private void sendNotification(String title, String subtitle, int like, String image, int position) {
        switch (like) {
            case 0:{
                image_like = R.drawable.ic_like;
                image_dislike = R.drawable.ic_dislike;
                break;
            }
            case 1:{
                image_like = R.drawable.ic_like_black;
                image_dislike = R.drawable.ic_dislike;
                break;
            }
            case 2:{
                image_like = R.drawable.ic_like;
                image_dislike = R.drawable.ic_dislike_black;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + like);
        }

        if (!MainActivity.mMediaPlayer.isPlaying()) {
            image_play_pause = R.drawable.play_icon;
        } else {
            image_play_pause = R.drawable.pause_icon;
        }
        Intent intentNextSong = new Intent(this, MusicReceiver.class)
                .setAction(Key.ACTION_NEXT_SONG);
        PendingIntent pendingIntentNext =
                PendingIntent.getBroadcast(this, 1, intentNextSong, PendingIntent.FLAG_ONE_SHOT);

        Intent intentPlaySong = new Intent(this, MusicReceiver.class)
                .setAction(Key.ACTION_PLAY_SONG);

        intentPlaySong.putExtra(Key.KEY_POSITION, String.valueOf(position));
        // sendBroadcast(intentPlaySong);
        PendingIntent pendingIntentPlay =
                PendingIntent.getBroadcast(this, 1, intentPlaySong, PendingIntent.FLAG_ONE_SHOT);

        Intent intentPreSong = new Intent(this, MusicReceiver.class)
                .setAction(Key.ACTION_PREVIOUS_SONG);
        PendingIntent pendingIntentPre =
                PendingIntent.getActivity(this, 1, intentPreSong, PendingIntent.FLAG_ONE_SHOT);

//        Log.d("position", "sendNotification: " + position);
//        Intent intent = new Intent(this, MusicReceiver.class);
        // sendBroadcast(intentPlaySong);
//        Intent intentActivity = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntentActivity = PendingIntent.getActivity(this, 1, intentActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_player);

        Notification notificationMusic = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSubText("MinhMX")
                .setContentTitle(title)
                .setContentText(subtitle)
                .setSmallIcon(R.drawable.splash_play_music_192)
                .setLargeIcon(bitmap)
                .addAction(image_like, "like", null)
                .addAction(R.drawable.previous_icon, "previous",pendingIntentPre)
                .addAction(image_play_pause, "pause", pendingIntentPlay)
                .addAction(R.drawable.next_icon, "next", pendingIntentNext)
                .addAction(image_dislike, "dislike", null)

                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1,2,3))
                .build();


        startForeground(1, notificationMusic);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
//        stopForeground();
    }

}