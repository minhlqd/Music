package com.example.music.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.music.activity.MainActivity;
import com.example.music.broadcast.MusicReceiver;
import com.example.music.Key;
import com.example.music.R;

import static com.example.music.Key.CHANNEL_ID;

public class MediaService extends Service {

    int image_like;
    int image_dislike;
    int image_play_pause;

    public MediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            String title = bundle.getString("title");
            String subtitle = bundle.getString("subtitle");
            int image = bundle.getInt("image");
            int like = bundle.getInt("like");
            sendNotification(title, subtitle, like, image);
        }
        return START_NOT_STICKY;
    }

    private void sendNotification(String title, String subtitle, int like, int image) {
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

        if (!MainActivity.mediaPlayer.isPlaying()) {
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
        PendingIntent pendingIntentPlay =
                PendingIntent.getBroadcast(this, 1, intentPlaySong, PendingIntent.FLAG_ONE_SHOT);

        Intent intentPreSong = new Intent(this, MusicReceiver.class)
                .setAction(Key.ACTION_PREVIOUS_SONG);
        PendingIntent pendingIntentPre =
                PendingIntent.getActivity(this, 1, intentPreSong, PendingIntent.FLAG_ONE_SHOT);

//        Intent intentActivity = new Intent(this, MainActivity.class);
//        PendingIntent pendingIntentActivity = PendingIntent.getActivity(this, 1, intentActivity, PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), image);
        Notification notificationMusic = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSubText("MinhMX")
                .setContentTitle(title)
                .setContentText(subtitle)
                .setSmallIcon(R.drawable.ic_music_a)
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