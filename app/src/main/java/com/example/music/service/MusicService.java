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
import android.provider.MediaStore;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.music.Key;
import com.example.music.R;
import com.example.music.activity.MainActivity;
import com.example.music.broadcast.MusicReceiver;
import com.example.music.database.AllSongOperations;
import com.example.music.interfaces.INotification;
import com.example.music.model.Song;

import java.io.IOException;
import java.util.ArrayList;


@SuppressWarnings("ALL")
public class MusicService extends Service {

    public static final int NOTIFY_ID = 1;
    public static final int REQUEST_CODE = 1;

    public static final int CHECK_PLAY = 0;
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

    private boolean mIsNotification = false;
    private boolean mIsContext = false;

    private boolean checkOnCompletionListener = false;

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
        // if (mIsContext) {
        //    mINotification = (INotification) newBase;
        //    mIsContext = false;
        // }
        mAllSongOperations = new AllSongOperations(newBase);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mSongsList = new ArrayList<>();
        mSongsList = mAllSongOperations.getAllSong();

        mPosition = intent.getIntExtra(Key.KEY_POSITION, 0);

        Song song = mSongsList.get(mPosition);
        sendNotification(this, song, mPosition);

        return START_NOT_STICKY;
    }

    public void sendNotification(Context context, Song song, int position) {
        if (mINotification != null) {
            //mSongsList = mAllSongOperations.getAllSong();
            mINotification.onClickNotification(position);
        }
        switch (song.isLike()) {
            case Key.NO_LIKE:{
                mImgLike = R.drawable.ic_like;
                mImgDislike = R.drawable.ic_dislike;
                break;
            }
            case Key.LIKE:{
                mImgLike = R.drawable.ic_like_black;
                mImgDislike = R.drawable.ic_dislike;
                break;
            }
            case Key.DISLIKE:{
                mImgLike = R.drawable.ic_like;
                mImgDislike = R.drawable.ic_dislike_black;
                break;
            }
            default:
                throw new IllegalStateException("Unexpected value: " + song.isLike());
        }
        Log.d("MinhMX", "sendNotification: " + song.getPlay());
//        if (song.getPlay() == CHECK_PLAY) {
//            mImgPlayPause = R.drawable.play_icon;
//        } else {
//            mImgPlayPause = R.drawable.pause_icon;
//        }

        Log.d("MinhMX", "sendNotification: " + mINotification);

        Intent intentNextSong = new Intent(context, MusicReceiver.class)
                .setAction(Key.ACTION_NEXT_SONG).putExtra(Key.KEY_POSITION, position);
        PendingIntent pendingIntentNext =
                PendingIntent.getBroadcast(context, REQUEST_CODE, intentNextSong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlaySong = new Intent(context, MusicReceiver.class)
                .setAction(Key.ACTION_PLAY_SONG).putExtra(Key.KEY_POSITION, position);

        PendingIntent pendingIntentPlay =
                PendingIntent.getBroadcast(context, REQUEST_CODE, intentPlaySong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPreSong = new Intent(context, MusicReceiver.class)
                .setAction(Key.ACTION_PREVIOUS_SONG).putExtra(Key.KEY_POSITION, position);
        PendingIntent pendingIntentPre =
                PendingIntent.getBroadcast(context, REQUEST_CODE, intentPreSong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentLikeSong = new Intent(context, MusicReceiver.class)
                .setAction(Key.ACTION_LIKE_SONG).putExtra(Key.KEY_POSITION, position);
        PendingIntent pendingIntentLike =
                PendingIntent.getBroadcast(context, REQUEST_CODE, intentLikeSong, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentDislikeSong = new Intent(context, MusicReceiver.class)
                .setAction(Key.ACTION_DISLIKE_SONG).putExtra(Key.KEY_POSITION, position);
        PendingIntent pendingIntentDislike =
                PendingIntent.getBroadcast(context, REQUEST_CODE, intentDislikeSong, PendingIntent.FLAG_UPDATE_CURRENT);
//        Log.d("position", "sendNotification: " + position);

        Intent intentActivity = new Intent(context, MainActivity.class).putExtra(Key.KEY_POSITION, position);
        PendingIntent pendingIntentActivity =
                PendingIntent.getActivity(context, REQUEST_CODE, intentActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews notification_small = new RemoteViews(context.getPackageName(), R.layout.notification_small);
        RemoteViews notification_big = new RemoteViews(context.getPackageName(), R.layout.notification_big);

        long image = song.getImage();
        Bitmap bitmap = null;
        try {
            if (context.getContentResolver() != null) {
                bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), song.queryAlbumUri(image));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        notification_small.setImageViewBitmap(R.id.image_music_notification, bitmap);

        notification_big.setTextViewText(R.id.tv_song_name_notification, song.getTitle());
        notification_big.setTextViewText(R.id.tv_song_author_notification, song.getSubTitle());
        notification_big.setImageViewBitmap(R.id.image_music_notification, bitmap);

        notification_small.setOnClickPendingIntent(R.id.icon_next_notification, pendingIntentNext);
        notification_small.setOnClickPendingIntent(R.id.icon_previous_notification, pendingIntentPre);
        notification_small.setOnClickPendingIntent(R.id.icon_play__notification_small, pendingIntentPlay);

        notification_big.setOnClickPendingIntent(R.id.icon_next_notification, pendingIntentNext);
        notification_big.setOnClickPendingIntent(R.id.icon_previous_notification, pendingIntentPre);
        notification_big.setOnClickPendingIntent(R.id.icon_play__notification_small, pendingIntentPlay);
        if (song.getPlay() == CHECK_PLAY) {
            notification_small.setImageViewResource(R.id.icon_play__notification_small, R.drawable.ic_play_black);
            notification_small.setOnClickPendingIntent(R.id.icon_play__notification_small, pendingIntentPlay);
            notification_big.setImageViewResource(R.id.icon_play_notification_big, R.drawable.ic_play_black);
            notification_big.setOnClickPendingIntent(R.id.icon_play_notification_big, pendingIntentPlay);
        } else {
            notification_small.setImageViewResource(R.id.icon_play__notification_small, R.drawable.ic_pause_black);
            notification_small.setOnClickPendingIntent(R.id.icon_play__notification_small, pendingIntentPlay);
            notification_big.setImageViewResource(R.id.icon_play_notification_big, R.drawable.ic_pause_black);
            notification_big.setOnClickPendingIntent(R.id.icon_play_notification_big, pendingIntentPlay);
        }

        Notification notificationMusic = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.splash_play_music_192)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentIntent(pendingIntentActivity)
                .setCustomContentView(notification_small)
                .setCustomBigContentView(notification_big)
                .build();

        mPosition = position;

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
        checkOnCompletionListener = false;
        mediaPlayer.start();
    }

    public void pause(){
        checkOnCompletionListener = false;
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
            checkOnCompletionListener = false;
            mediaPlayer.setDataSource(song.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int completeListenMusic(int postion) {
        mPosition = postion;
        mediaPlayer.setOnCompletionListener(mp -> {
            mPosition = postion + 1;
            if (mPosition>mSongsList.size()) {
                mPosition = 0;
            }
        });
        return mPosition;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}