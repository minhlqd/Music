package com.example.music.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.music.interfaces.INotificationService;
import com.example.music.broadcast.MusicReceiver;
import com.example.music.fragment.AllSongFragment;
import com.example.music.fragment.FavSongFragment;
import com.example.music.Key;
import com.example.music.R;
import com.example.music.interfaces.ICreateDataParseFav;
import com.example.music.interfaces.ICreateDataParseSong;
import com.example.music.database.AllSongOperations;
import com.example.music.database.FavoritesOperations;
import com.example.music.fragment.MediaPlaybackFragment;
import com.example.music.model.SongsList;
import com.example.music.service.MediaService;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;


@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        ICreateDataParseSong,
        ICreateDataParseFav,
        MediaPlaybackFragment.createDataParsed,
        INotificationService {

    public static final String TAG = "music_aaa";

    private Menu menu;

    private ImageButton imgBtnPlayPause;
    private ImageView imgBtnReplay;
    private ImageButton imgBtnPrev;
    private ImageButton imgBtnNext;
    private ImageButton imgBtnDisLike;
    private ImageButton imgBtnLike;
    private ImageView imgBtnShuffle;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    public static SeekBar seekbarController;
    private DrawerLayout mDrawerLayout;
    public static TextView tvCurrentTime;
    private TextView tvTotalTime;

    public static LinearLayout mLinearLayout;

    private ArrayList<SongsList> songList;
    private RecyclerView mRecyclerView;
    private int currentPosition;
    private String searchText = "";
    private SongsList currSong;

    private FrameLayout mFragmentAllSong;
    private FrameLayout mFragmentMediaPlay;

    private ArrayList<SongsList> mDataSongList;

    private boolean checkFlag = false, repeatFlag = false, playContinueFlag = true, favFlag = true, playlistFlag = false;
    private boolean likeFlag = false;
    private boolean disLikeFlag = false;
    private boolean checkLikeFlag = false;
    private boolean checkPlay = true;
    private boolean checkFragmentFav = false;

    private Toolbar toolbar;

    private final int MY_PERMISSION_REQUEST = 100;
    private int allSongLength;

    private FavoritesOperations mFavoritesOperations;
    private AllSongOperations mAllSongOperations;


    private Intent mIntentService;
    private MusicReceiver mReceiver = new MusicReceiver();
    private INotificationService iNotificationService;

    public static MediaPlayer mediaPlayer;
    public static Handler handler;
    public static Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        grantedPermission();
        getMusic();
        broadcast();
    }

    @SuppressLint("NonConstantResourceId")
    private void init() {
        imgBtnPrev = findViewById(R.id.img_btn_previous);
        imgBtnNext = findViewById(R.id.img_btn_next);
        imgBtnReplay = findViewById(R.id.img_btn_replay);
        imgBtnDisLike = findViewById(R.id.img_btn_dislike);
        imgBtnLike = findViewById(R.id.img_btn_like);
        imgBtnShuffle = findViewById(R.id.img_btn_shuffle);



        mLinearLayout = findViewById(R.id.ll_include_controls);
        mLinearLayout.setVisibility(View.GONE);


        mFragmentAllSong = findViewById(R.id.fragment);
        mFragmentMediaPlay = findViewById(R.id.fragment_media);


        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);

        seekbarController = findViewById(R.id.seekbar_controller);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        imgBtnPlayPause = findViewById(R.id.img_btn_play);
        toolbar = findViewById(R.id.toolbar);
        handler = new Handler();
        mediaPlayer = new MediaPlayer();

        toolbar.setTitleTextColor(getResources().getColor(R.color.text_color));
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);

        AllSongFragment createAllSongs = new AllSongFragment();

        mFavoritesOperations = new FavoritesOperations(this);
        mAllSongOperations = new AllSongOperations(this);

        Display display = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        if (display.getRotation() == Surface.ROTATION_90 || display.getRotation() == Surface.ROTATION_270){
            RelativeLayout.LayoutParams paramsMediaPlay = (RelativeLayout.LayoutParams) mFragmentMediaPlay.getLayoutParams();
            RelativeLayout.LayoutParams paramsALlSong = (RelativeLayout.LayoutParams) mFragmentAllSong.getLayoutParams();
            paramsALlSong.width = 950;
            paramsMediaPlay.width = 2000;
            mFragmentAllSong.setLayoutParams(paramsALlSong);
            mFragmentMediaPlay.setLayoutParams(paramsMediaPlay);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, new AllSongFragment())
                    .addToBackStack("Fragment")
                    .commit();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_media, new MediaPlaybackFragment())
                    .addToBackStack("Fragment")
                    .commit();
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, new AllSongFragment())
                    .addToBackStack("Fragment")
                    .commit();

            FragmentManager fragmentManager = getSupportFragmentManager();
            MediaPlaybackFragment mediaPlaybackFragment = (MediaPlaybackFragment) fragmentManager.findFragmentById(R.id.fragment_media);

            if (mediaPlaybackFragment != null) {
                FragmentTransaction fragmentTransaction =
                        fragmentManager.beginTransaction();
                fragmentTransaction.remove(mediaPlaybackFragment).commit();
            }
        }

        imgBtnNext.setOnClickListener(this);
        imgBtnPrev.setOnClickListener(this);
        imgBtnReplay.setOnClickListener(this);
        imgBtnPlayPause.setOnClickListener(this);
        imgBtnLike.setOnClickListener(this);
        imgBtnDisLike.setOnClickListener(this);
        imgBtnShuffle.setOnClickListener(this);

        navigationView.setNavigationItemSelectedListener(item -> {
            mDrawerLayout.closeDrawers();
            switch (item.getItemId()) {
                case R.id.listen_now:{
                    attachMusic(songList.get(currentPosition).getTitle(), songList.get(currentPosition).getPath());
                    musicNextPre(songList, currentPosition);
                    mLinearLayout.setVisibility(View.VISIBLE);

                    break;
                }
                case R.id.favorite: {
                    SpannableString spanString = new SpannableString(item.getTitle().toString());
                    spanString.setSpan(new ForegroundColorSpan(Color.YELLOW), 0, spanString.length(), 0);
                    item.setTitle(spanString);
                    //checkFragmentFav = true;
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new FavSongFragment()).addToBackStack("fragment").commit();
                    break;
                }
                case R.id.nav_about: {
                    about();
                    break;
                }
            }

            return true;
        });
    }

    private void grantedPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSION_REQUEST);
            } else {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            }
        }
    }

    private void getMusic() {
        mDataSongList = new ArrayList<>();
        mAllSongOperations = new AllSongOperations(this);
        if (mAllSongOperations.getAllSong().size() == 0) {
            Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = getContentResolver().query(songUri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int songSubTitle = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int path = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                while (cursor.moveToNext()) {
                    mDataSongList.add(new SongsList(cursor.getString(songTitle),
                            cursor.getString(songSubTitle),
                            cursor.getString(path),
                            R.drawable.ic_music_player, 0, 0));
                }
            }
            for (int i = 0; i < mDataSongList.size(); i++) {
                mAllSongOperations.addAllSong(mDataSongList.get(i));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
//                    setPagerLayout();
                } else {
                    Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    finish();
                }
            }
        }
    }


    private void broadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Key.ACTION_PLAY_SONG);
        intentFilter.addAction(Key.ACTION_NEXT_SONG);
        intentFilter.addAction(Key.ACTION_PREVIOUS_SONG);

        registerReceiver(mReceiver, intentFilter);
        iNotificationService = this;
        mReceiver.getInterface(iNotificationService);
    }

    private void about() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about))
                .setMessage(getString(R.string.about_text))
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setQueryHint("Search...");

        searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchText = newText;
                queryText();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
//                setPagerLayout();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(Gravity.START);
                return true;
            case R.id.menu_search:
                Toast.makeText(this, "Search", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_btn_play:
                intentService(songList.get(currentPosition));
                if (checkFlag) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                        imgBtnPlayPause.setImageResource(R.drawable.ic_play_black);
                        imgBtnPlayPause.setBackground(getDrawable(R.color.background));
                    } else if (!mediaPlayer.isPlaying()) {
                        mediaPlayer.start();
                        imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
                        imgBtnPlayPause.setBackground(getDrawable(R.drawable.background_play_pause));
                        playCycle();
                    }
                } else {
                    Toast.makeText(this, "Select a Song . .", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.img_btn_replay:
                if (repeatFlag) {
                    Toast.makeText(this, "Replaying Removed..", Toast.LENGTH_SHORT).show();
                    imgBtnReplay.setImageResource(R.drawable.ic_repeat);
                    mediaPlayer.setLooping(false);
                    repeatFlag = false;
                } else {
                    Toast.makeText(this, "Replaying Added..", Toast.LENGTH_SHORT).show();
                    imgBtnReplay.setImageResource(R.drawable.ic_repeat_one);
                    mediaPlayer.setLooping(true);
                    repeatFlag = true;
                }
                break;
            case R.id.img_btn_previous:
                imgBtnReplay.setImageResource(R.drawable.ic_repeat);
                if (checkFlag) {
                    if (seekbarController.getProgress() > 3000) {
                        attachMusic(songList.get(currentPosition).getTitle(), songList.get(currentPosition).getPath());
                    } else {
                        if (currentPosition > 0 ) {
                            currentPosition = currentPosition - 1;
                            attachMusic(songList.get(currentPosition).getTitle(), songList.get(currentPosition).getPath());
                            musicNextPre(songList, currentPosition);
                        } else {
                            currentPosition = songList.size() - 1;
                            musicNextPre(songList, currentPosition);
                            attachMusic(songList.get(currentPosition).getTitle(), songList.get(currentPosition).getPath());
                        }
                    }
                }
                break;
            case R.id.img_btn_next:
                imgBtnReplay.setImageResource(R.drawable.ic_repeat);
                if (checkFlag) {
                    if (currentPosition + 1 < songList.size()) {
                        attachMusic(songList.get(currentPosition + 1).getTitle(), songList.get(currentPosition + 1).getPath());
                        currentPosition += 1;
                        musicNextPre(songList, currentPosition);
                    } else {
                        currentPosition = 0;
                        musicNextPre(songList, currentPosition);
                        attachMusic(songList.get(currentPosition).getTitle(), songList.get(currentPosition).getPath());
                        checkFlag = true;
                        //Toast.makeText(this, "Playlist Ended", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Select the Song ..", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.img_btn_shuffle:{
                imgBtnShuffle.setImageResource(R.drawable.ic_shuffle_black);
                break;
            }
            case R.id.img_btn_like:{
                Log.d(TAG, "onClick: " + likeFlag);
                if (likeFlag) {
                    likeFlag = false;
                    mFavoritesOperations.removeSong(songList.get(currentPosition).getTitle());
                    songList.get(currentPosition).setLike(0);

                    mAllSongOperations.updateSong(songList.get(currentPosition));

                    intentService(songList.get(currentPosition));
                    imgBtnLike.setImageResource(R.drawable.ic_like);

                } else {
                    likeFlag = true;
                    disLikeFlag = false;

                    favMusic(songList.get(currentPosition));
                    songList.get(currentPosition).setLike(1);

                    intentService(songList.get(currentPosition));
                    Log.d(TAG, "onClick: " + currentPosition);

                    mAllSongOperations.updateSong(songList.get(currentPosition));

                    imgBtnLike.setImageResource(R.drawable.ic_like_black);
                    imgBtnDisLike.setImageResource(R.drawable.ic_dislike);
                }
                break;
            }
            case R.id.img_btn_dislike:
                if (disLikeFlag) {
                    disLikeFlag = false;
                    songList.get(currentPosition).setLike(0);

                    mAllSongOperations.updateSong(songList.get(currentPosition));

                    intentService(songList.get(currentPosition));

                    imgBtnDisLike.setImageResource(R.drawable.ic_dislike);
                } else {
                    disLikeFlag = true;
                    likeFlag = false;

                    mFavoritesOperations.removeSong(songList.get(currentPosition).getTitle());
                    songList.get(currentPosition).setLike(2);

                    mAllSongOperations.updateSong(songList.get(currentPosition));

                    intentService(songList.get(currentPosition));

                    imgBtnDisLike.setImageResource(R.drawable.ic_dislike_black);
                    imgBtnLike.setImageResource(R.drawable.ic_like);
                }
                break;
        }
    }

    private void attachMusic(String name, String path) {
        imgBtnPlayPause.setImageResource(R.drawable.ic_play_black);
        setTitle(name);
        favFlag = true;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            setControls();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(mp -> {
            imgBtnPlayPause.setImageResource(R.drawable.ic_play_black);
            if (playContinueFlag) {
                if (currentPosition + 1 < songList.size()) {
                    attachMusic(songList.get(currentPosition + 1).getTitle(), songList.get(currentPosition + 1).getPath());
                    currentPosition += 1;
                    musicNextPre(songList, currentPosition);
                } else {
                    currentPosition = 0;
                    musicNextPre(songList, currentPosition);
                    attachMusic(songList.get(currentPosition).getTitle(), songList.get(currentPosition).getPath());
                }
            }
        });
    }

    private void setControls() {
        seekbarController.setMax(mediaPlayer.getDuration());
        mediaPlayer.start();
        playCycle();
        checkFlag = true;
        if (mediaPlayer.isPlaying()) {
            imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
//            imgBtnPlayPause.setSelected(imgBtnPlayPause.isSelected());
            imgBtnPlayPause.setBackground(getDrawable(R.drawable.background_play_pause));
            tvTotalTime.setText(getTimeFormatted(mediaPlayer.getDuration()));
        }
        seekbarController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    tvCurrentTime.setText(getTimeFormatted(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public static void playCycle() {
        try {
            seekbarController.setProgress(mediaPlayer.getCurrentPosition());
            tvCurrentTime.setText(getTimeFormatted(mediaPlayer.getCurrentPosition()));
            if (mediaPlayer.isPlaying()) {
                runnable = MainActivity::playCycle;
                handler.postDelayed(runnable, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTimeFormatted(long milliSeconds) {
        String finalTimerString = "";
        String secondsString;

        //Converting total duration into time
        int hours = (int) (milliSeconds / 3600000);
        int minutes = (int) (milliSeconds % 3600000) / 60000;
        int seconds = (int) ((milliSeconds % 3600000) % 60000 / 1000);

        // Adding hours if any
        if (hours > 0)
            finalTimerString = hours + ":";

        // Prepending 0 to seconds if it is one digit
        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        // Return timer String;
        return finalTimerString;
    }

    private void musicNextPre(ArrayList<SongsList> songListNext, int position){
        if (checkPlay) {
            MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
            mediaPlayFragment.setArguments(getBundle(songListNext.get(position)));
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment,
                    mediaPlayFragment).addToBackStack("fragment").commit();

            intentService(songListNext.get(position));

            Log.d(TAG, "musicNextPre: " + likeFlag);
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        while (count>2) {
            getSupportFragmentManager().popBackStack();
            count--;
        }
        super.onBackPressed();
        checkPlay = false;
        Log.d(TAG, "onBackPressed: " + songList);
        mLinearLayout.setVisibility(View.GONE);
    }

    private Bundle getBundle(SongsList currSong){
        Bundle bundle = new Bundle();
        bundle.putInt("image", currSong.getImage());
        bundle.putInt("like", currSong.isLike());
        bundle.putString("title", currSong.getTitle());
        bundle.putString("subtitle", currSong.getSubTitle());
        return bundle;
    }

    private void intentService(SongsList currSong) {
        mIntentService = new Intent(this, MediaService.class);
        mIntentService.putExtras(getBundle(currSong));
        startService(mIntentService);
    }


    private void favMusic(SongsList favSongList) {
        mFavoritesOperations.addSongFav(favSongList);
    }

    @Override
    public void onDataPass(String name, String path) {
        Toast.makeText(this, name, Toast.LENGTH_LONG).show();
        attachMusic(name, path);
        checkPlay = true;
        mLinearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void getLength(int length) {
        this.allSongLength = length;
    }

    @Override
    public void onDataPassSong(String name, String path, boolean checkSong) {
        Toast.makeText(this, name, Toast.LENGTH_LONG).show();
        if (checkSong) {
            attachMusic(name, path);
        }
        checkPlay = true;
        mLinearLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void fullSongList(ArrayList<SongsList> songList, int position) {
        this.songList = songList;
        this.currentPosition = position;
        this.playlistFlag = songList.size() == allSongLength;
        this.playContinueFlag = true;
        this.checkLikeFlag = true;
    }

    @Override
    public String queryText() {
        Log.d(TAG, "queryText: " + searchText);
        return searchText.toLowerCase();
    }

    @Override
    public int getPositionSong() {
        return currentPosition;
    }

    @Override
    public boolean isSong() {
        if (mediaPlayer.isPlaying()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void playCheckSong(boolean checkSong) {
        if (!checkSong) {
            imgBtnPlayPause.setImageResource(R.drawable.ic_play_black);
            imgBtnPlayPause.setBackground(getDrawable(R.color.background));
        } else {
            imgBtnPlayPause.setImageResource(R.drawable.pause_icon);
            imgBtnPlayPause.setBackground(getDrawable(R.drawable.background_play_pause));
        }
    }

    @Override
    public void currentSong(SongsList songsList) {
        this.currSong = songsList;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.release();
        handler.removeCallbacks(runnable);
        unregisterReceiver(mReceiver);
//        stopService(mIntentService);
    }

    @Override
    public void isLike(int like) {
        switch (like) {
            case 0:
                likeFlag = false;
                disLikeFlag = false;
                imgBtnLike.setImageResource(R.drawable.ic_like);
                imgBtnDisLike.setImageResource(R.drawable.ic_dislike);
                break;
            case 1:
                likeFlag = true;
                disLikeFlag = false;
                imgBtnLike.setImageResource(R.drawable.ic_like_black);
                imgBtnDisLike.setImageResource(R.drawable.ic_dislike);
                break;
            case 2:
                likeFlag = false;
                disLikeFlag = true;
                imgBtnLike.setImageResource(R.drawable.ic_like);
                imgBtnDisLike.setImageResource(R.drawable.ic_dislike_black);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void isPlayNotification(boolean isPlay) {
        if (!isPlay) {
            imgBtnPlayPause.setImageResource(R.drawable.ic_play_black);
            imgBtnPlayPause.setBackground(getDrawable(R.color.background));
        }
    }
}
