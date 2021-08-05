package com.example.music.service;

import static com.example.music.Key.CHANNEL_ID;

import android.app.Notification;
import android.app.NotificationManager;
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

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.music.Key;
import com.example.music.R;
import com.example.music.activity.MainActivity;
import com.example.music.broadcast.MusicReceiver;
import com.example.music.database.AllSongOperations;
import com.example.music.interfaces.INotification;
import com.example.music.model.Song;

import java.util.ArrayList;


@SuppressWarnings("ALL")
public class MusicService extends Service {

    public static final int NOTIFY_ID = 1;

    private int mImgLike;
    private int mImgDislike;
    private int mImgPlayPause;
    private AllSongOperations mAllSongOperations;
    private ArrayList<Song> mSongsList;

    public MediaPlayer mediaPlayer;

    private String mTitle;
    private String mSubtitle;
    private long mImage;
    private int mLike;
    private int mPlay;
    private int mPosition;

    private MainActivity mainActivity;
    private INotification mINotification;

    class MusicServiceBinder extends Binder {
        public MusicService getService(){
            return MusicService.this;
        }
    }

    private boolean mIsNotification = false;
    private boolean mIsContext = false;

    public MusicService() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mIsContext = true;
        mIsNotification = true;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setINotification (INotification iNotification) {
        this.mINotification = iNotification;
    }
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        if (mIsContext) {
            mINotification = (INotification) newBase;
            mIsContext = false;
        }
        mAllSongOperations = new AllSongOperations(newBase);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSongsList = new ArrayList<>();
        mSongsList = mAllSongOperations.getAllSong();

        mPosition = intent.getIntExtra(Key.KEY_POSITION, 0);

        Log.d("MinhMX", "onStartCommand: " + mPosition);

        Song song = mSongsList.get(mPosition);
        sendNotification(this, song, mPosition);

        return START_NOT_STICKY;
    }

    public void sendNotification(Context context, Song song, int position) {
        switch (song.isLike()) {
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
                throw new IllegalStateException("Unexpected value: " + song.isLike());
        }

        if (song.getPlay() == 0) {
            Log.d("aaa", "sendNotification: " + mediaPlayer.isPlaying());
            mImgPlayPause = R.drawable.play_icon;
        } else {
            Log.d("aaa", "sendNotification: " + mediaPlayer.isPlaying());
            mImgPlayPause = R.drawable.pause_icon;
        }

        Log.d("MinhMX", "sendNotification: " + mINotification);

        Intent intentNextSong = new Intent(context, MusicReceiver.class)
                .setAction(Key.ACTION_NEXT_SONG).putExtra(Key.KEY_POSITION, position);
        PendingIntent pendingIntentNext =
                PendingIntent.getBroadcast(context, 1, intentNextSong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlaySong = new Intent(context, MusicReceiver.class)
                .setAction(Key.ACTION_PLAY_SONG).putExtra(Key.KEY_POSITION, position);

        PendingIntent pendingIntentPlay =
                PendingIntent.getBroadcast(context, 1, intentPlaySong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPreSong = new Intent(context, MusicReceiver.class)
                .setAction(Key.ACTION_PREVIOUS_SONG).putExtra(Key.KEY_POSITION, position);
        PendingIntent pendingIntentPre =
                PendingIntent.getBroadcast(context, 1, intentPreSong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentLikeSong = new Intent(context, MusicReceiver.class)
                .setAction(Key.ACTION_LIKE_SONG).putExtra(Key.KEY_POSITION, position);
        PendingIntent pendingIntentLike =
                PendingIntent.getBroadcast(context, 1, intentLikeSong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentDislikeSong = new Intent(context, MusicReceiver.class)
                .setAction(Key.ACTION_DISLIKE_SONG).putExtra(Key.KEY_POSITION, position);
        PendingIntent pendingIntentDislike =
                PendingIntent.getBroadcast(context, 1, intentDislikeSong, PendingIntent.FLAG_UPDATE_CURRENT);
//        Log.d("position", "sendNotification: " + position);
//        Intent intent = new Intent(context, MusicReceiver.class);
//         sendBroadcast(intentPlaySong);
//        Intent intentActivity = new Intent(context, class);
//        PendingIntent pendingIntentActivity = PendingIntent.getActivity(context, 1, intentActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_music_player);
        Notification notificationMusic = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSubText("MinhMX")
                .setContentTitle(song.getTitle())
                .setContentText(song.getSubTitle())
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

        if (mINotification != null) {
            mINotification.nextPlay(position);
        }
        if (mIsNotification) {
            startForeground(NOTIFY_ID, notificationMusic);
            mIsNotification = false;
        } else {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFY_ID, notificationMusic);
        }
    }


    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }


    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void play(){
        mediaPlayer.start();
    }

    public void pause(){
        mediaPlayer.pause();
    }

    public boolean isPlaying() {
        if(mediaPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    public void looping(boolean isLoop) {
        mediaPlayer.setLooping(isLoop);
    }

    public int nextSong() {
        mPosition++;
        return mPosition;
    }

    public int previousSong() {
        mPosition--;
        return mPosition;
    }

    public void playMedia(Song song){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MinhMX", "onDestroy: " + mPosition);
        stopForeground(true);
    }
}