package com.example.music.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.music.activity.MainActivity;
import com.example.music.broadcast.MusicReceiver;
import com.example.music.Key;
import com.example.music.R;
import com.example.music.database.AllSongOperations;
import com.example.music.model.Song;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.music.Key.CHANNEL_ID;


@SuppressWarnings("ALL")
public class MusicService extends Service {

    private int mImgLike;
    private int mImgDislike;
    private int mImgPlayPause;
    private AllSongOperations mAllSongOperations;
    private ArrayList<Song> mSongsList;

    public MediaPlayer mediaPlayer;

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public class MusicBinder extends Binder {
        public MusicService getSerVice() {
            return MusicService.this;
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
        Song songsList = mSongsList.get(position);
        String title = songsList.getTitle();
        String subtitle = songsList.getSubTitle();
        long image = songsList.getImage();
        int like = songsList.isLike();
        sendNotification(title, subtitle, like, image, position);

        return START_NOT_STICKY;
    }

    private void sendNotification(String title, String subtitle, int like, long image, int position) {
        switch (like) {
            case 0:{
                mImgLike = R.drawable.ic_like;
                mImgDislike = R.drawable.ic_dislike;
                break;
            }
            case 1:{
                mImgLike = R.drawable.ic_like_black;
                mImgDislike = R.drawable.ic_dislike;
                break;
            }
            case 2:{
                mImgLike = R.drawable.ic_like;
                mImgDislike = R.drawable.ic_dislike_black;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + like);
        }

        if (!MainActivity.mediaPlayer.isPlaying()) {
            mImgPlayPause = R.drawable.play_icon;
        } else {
            mImgPlayPause = R.drawable.pause_icon;
        }
        Intent intentNextSong = new Intent(this, MusicReceiver.class)
                .setAction(Key.ACTION_NEXT_SONG).putExtra(Key.KEY_POSITION,position);
       PendingIntent pendingIntentNext =
                PendingIntent.getBroadcast(this, 1, intentNextSong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlaySong = new Intent(this, MusicReceiver.class)
                .setAction(Key.ACTION_PLAY_SONG);

        Log.d("aaa", "sendNotification: " + title);
        intentPlaySong.putExtra(Key.CONST_TITLE, title);

        PendingIntent pendingIntentPlay =
                PendingIntent.getBroadcast(this, 1, intentPlaySong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPreSong = new Intent(this, MusicReceiver.class)
                .setAction(Key.ACTION_PREVIOUS_SONG);
        PendingIntent pendingIntentPre =
                PendingIntent.getActivity(this, 1, intentPreSong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentLikeSong = new Intent(this, MusicReceiver.class)
                .setAction(Key.ACTION_LIKE_SONG);
        PendingIntent pendingIntentLike =
                PendingIntent.getBroadcast(this, 1, intentLikeSong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentDislikeSong = new Intent(this, MusicReceiver.class)
                .setAction(Key.ACTION_DISLIKE_SONG);
        PendingIntent pendingIntentDislike =
                PendingIntent.getBroadcast(this, 1, intentDislikeSong, PendingIntent.FLAG_UPDATE_CURRENT);
//        Log.d("position", "sendNotification: " + position);
//        Intent intent = new Intent(this, MusicReceiver.class);
//         sendBroadcast(intentPlaySong);
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
                .addAction(mImgLike, "like", pendingIntentLike)
                .addAction(R.drawable.previous_icon, "previous",pendingIntentPre)
                .addAction(mImgPlayPause, "pause", pendingIntentPlay)
                .addAction(R.drawable.next_icon, "next", pendingIntentNext)
                .addAction(mImgDislike, "dislike", pendingIntentDislike)

                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1,2,3))
                .build();


        startForeground(1, notificationMusic);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void playMedia(Song song) throws Exception {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}