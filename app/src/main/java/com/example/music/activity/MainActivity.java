package com.example.music.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
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

import com.example.music.Key;
import com.example.music.R;
import com.example.music.broadcast.MusicReceiver;
import com.example.music.database.AllSongOperations;
import com.example.music.database.FavoritesOperations;
import com.example.music.fragment.AllSongFragment;
import com.example.music.fragment.FavSongFragment;
import com.example.music.fragment.MediaPlaybackFragment;
import com.example.music.interfaces.ICreateDataParseFav;
import com.example.music.interfaces.ICreateDataParseMedia;
import com.example.music.interfaces.ICreateDataParseSong;
import com.example.music.model.SongsList;
import com.example.music.service.MediaService;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;


@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements
        View.OnClickListener,
        ICreateDataParseSong,
        ICreateDataParseFav,
        ICreateDataParseMedia {

    public static final String TAG = "music_aaa";

    private Menu menu;

    private ImageButton mBtnPlayPause;
    private ImageView mBtnReplay;
    private ImageButton mBtnPrev;
    private ImageButton mBtnNext;
    private ImageButton mBtnDisLike;
    private ImageButton mBtnLike;
    private ImageView mBtnShuffle;

    private TextView mTitle;
    private TextView mSubtitle;
    private ImageView mPlayPauseSong;
    private ImageView mImgSong;

    private LinearLayout mPlayerSheetAll;

    public static SeekBar mSeekbarController;
    private DrawerLayout mDrawerLayout;
    public static TextView mCurrentTime;
    private TextView mTotalTime;

    public static LinearLayout mPlayerLayout;

    private ArrayList<SongsList> mSongsList;
    private RecyclerView mRecyclerView;
    private int mCurrentPosition;
    private String mSearchText = "";
    private SongsList mCurrentSong;

    private FrameLayout mFragmentAllSong;
    private FrameLayout mFragmentMediaPlay;

    private ArrayList<SongsList> mDataSongList;

    private boolean mCheckFlag = false; 
    private boolean mRepeatFlag = false;
    private boolean mPlayContinueFlag = true;
    private boolean mFavFlag = true;
    private boolean mPlayListFlag = false;
    private boolean mLikeFlag = false;
    private boolean mDislikeFlag = false;
    private boolean mCheckLikeFlag = false;
    private boolean mCkeckPlay = true;
    private boolean mCheckScreen = false;
    private boolean mCheckPlayerSheet = false;
    private boolean mcheckPlayMusic = false;

    private Toolbar mToolbar;

    private final int MY_PERMISSION_REQUEST = 100;
    private int mAllSongLength;

    private FavoritesOperations mFavoritesOperations;
    private AllSongOperations mAllSongOperations;


    private Intent mIntentService;
    private MusicReceiver mReceiver = new MusicReceiver();

    public static MediaPlayer mMediaPlayer;
    public static Handler mHandler;
    public static Runnable mRunnable;

    private boolean mIsBinder = false;
    private MediaService mMediaService;

    private Display mDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDisplay = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        if (mDisplay.getRotation() == Surface.ROTATION_90
                || mDisplay.getRotation() == Surface.ROTATION_270) {

            setContentView(R.layout.activity_main_landscape);
            mCheckScreen = true;

        } else {

            setContentView(R.layout.activity_main);
            mCheckScreen = false;

        }
        init();
        grantedPermission();
        getMusic();
        playMusic();
        broadcast();
    }

    @SuppressLint("NonConstantResourceId")
    private void init() {
        mBtnPlayPause = findViewById(R.id.img_btn_play);
        mBtnPrev = findViewById(R.id.img_btn_previous);
        mBtnNext = findViewById(R.id.img_btn_next);
        mBtnReplay = findViewById(R.id.img_btn_replay);
        mBtnDisLike = findViewById(R.id.img_btn_dislike);
        mBtnLike = findViewById(R.id.img_btn_like);
        mBtnShuffle = findViewById(R.id.img_btn_shuffle);

        mPlayerLayout = findViewById(R.id.ll_include_controls);

        mFragmentAllSong = findViewById(R.id.fragment);
        mFragmentMediaPlay = findViewById(R.id.fragment_media);

        mCurrentTime = findViewById(R.id.tv_current_time);
        mTotalTime = findViewById(R.id.tv_total_time);

        mTitle = findViewById(R.id.tv_music_name);
        mSubtitle = findViewById(R.id.tv_music_subtitle);
        mPlayPauseSong = findViewById(R.id.play_pause_song);
        mImgSong = findViewById(R.id.iv_music_list);
        mPlayerSheetAll = findViewById(R.id.linear_play_sheet_all);

        mSeekbarController = findViewById(R.id.seekbar_controller);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        mToolbar = findViewById(R.id.toolbar);
        mHandler = new Handler();
        mMediaPlayer = new MediaPlayer();


        mToolbar.setTitleTextColor(getResources().getColor(R.color.light_color));
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);

        AllSongFragment createAllSongs = new AllSongFragment();

        mFavoritesOperations = new FavoritesOperations(this);
        mAllSongOperations = new AllSongOperations(this);

        if (mDisplay.getRotation() == Surface.ROTATION_90 ||
                mDisplay.getRotation() == Surface.ROTATION_270){

            mCheckScreen = true;
            checkScreen();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, new AllSongFragment())
                    .addToBackStack("Fragment")
                    .commit();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_media, new MediaPlaybackFragment())
                    .addToBackStack("Fragment_media")
                    .commit();
            mPlayerLayout.setVisibility(View.VISIBLE);
        } else {
            mCheckScreen = false;
            checkScreen();
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
            mPlayerLayout.setVisibility(View.GONE);
        }

        mBtnNext.setOnClickListener(this);
        mBtnPrev.setOnClickListener(this);
        mBtnReplay.setOnClickListener(this);
        mBtnPlayPause.setOnClickListener(this);
        mBtnLike.setOnClickListener(this);
        mBtnDisLike.setOnClickListener(this);
        mBtnShuffle.setOnClickListener(this);
        mPlayPauseSong.setOnClickListener(this);
        mPlayerSheetAll.setOnClickListener(this);


        navigationView.setNavigationItemSelectedListener(item -> {
            mDrawerLayout.closeDrawers();
            switch (item.getItemId()) {
                case R.id.listen_now:{
                    attachMusic(mSongsList.get(mCurrentPosition).getTitle(), mSongsList.get(mCurrentPosition).getPath());
                    musicNextPre(mSongsList, mCurrentPosition);
                    mPlayerLayout.setVisibility(View.VISIBLE);
                    break;
                }
                case R.id.favorite: {
                    //checkFragmentFav = true;
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new FavSongFragment()).addToBackStack("fragment").commit();
                    break;
                }
                case R.id.library:{
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).addToBackStack("fragment").commit();
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
                int image = cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART);
                int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                while (cursor.moveToNext()) {
                    mDataSongList.add(new SongsList(cursor.getString(songTitle)
                            , cursor.getString(songSubTitle)
                            , getTimeFormatted(Long.parseLong(cursor.getString(duration)))
                            , cursor.getString(path)
                            , R.drawable.ic_music_player
                            , 0
                            , 0
                            , 0));
                }
            }
            for (int i = 0; i < mDataSongList.size(); i++) {
                mAllSongOperations.addAllSong(mDataSongList.get(i));
            }
        } else {
            mDataSongList.addAll(mAllSongOperations.getAllSong());
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

    private void playMusic() {
        mTitle.setText(mDataSongList.get(0).getTitle());
        mSubtitle.setText(mDataSongList.get(0).getSubTitle());
        mImgSong.setImageResource(mDataSongList.get(0).getImage());
    }

    private void broadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Key.ACTION_PLAY_SONG);
        intentFilter.addAction(Key.ACTION_NEXT_SONG);
        intentFilter.addAction(Key.ACTION_PREVIOUS_SONG);
        registerReceiver(mReceiver, intentFilter);
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
                mSearchText = newText;
                queryText();
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
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
            case R.id.linear_play_sheet_all:{
                if (!mcheckPlayMusic) {
                    mSongsList = mDataSongList;
                    mCurrentPosition = 0;
                    mcheckPlayMusic = true;
                }
                MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                mediaPlayFragment.setArguments(getBundle(mSongsList.get(mCurrentPosition)));
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mediaPlayFragment).commit();
                mMediaPlayer.start();
                playCycle();
                mPlayerSheetAll.setVisibility(View.GONE);
                mPlayerLayout.setVisibility(View.VISIBLE);
                mCheckPlayerSheet = true;

                break;
            }
            case R.id.play_pause_song:{
                intentService(mCurrentPosition);
                if (!mcheckPlayMusic) {
                    attachMusic(mDataSongList.get(0).getTitle(), mDataSongList.get(0).getPath());
                    Log.d(TAG, "onClick: " + mDataSongList.get(0).getTitle());
                    mDataSongList.get(0).setPlay(1);
                    mAllSongOperations.updateSong(mDataSongList.get(0));
                    mPlayPauseSong.setImageResource(R.drawable.ic_pause_black);
                    mSongsList = mDataSongList;
                    mCurrentPosition = 0;
                    mcheckPlayMusic = true;
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).addToBackStack("fragment").commit();
                } else {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                        mSongsList.get(mCurrentPosition).setPlay(0);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));
                        mPlayPauseSong.setImageResource(R.drawable.ic_play_black);
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
                    } else if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                        mSongsList.get(mCurrentPosition).setPlay(1);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));
                        mPlayPauseSong.setImageResource(R.drawable.ic_pause_black);
                        playCycle();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
                    }
                }
                break;
            }
            case R.id.img_btn_play:
                intentService(mCurrentPosition);
                if (mCheckFlag) {
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();

                        mSongsList.get(mCurrentPosition).setPlay(0);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                        mBtnPlayPause.setImageResource(R.drawable.ic_play_black);
                        mBtnPlayPause.setBackground(getDrawable(R.color.background));
                    } else if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();

                        mSongsList.get(mCurrentPosition).setPlay(1);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                        mBtnPlayPause.setImageResource(R.drawable.pause_icon);
                        mBtnPlayPause.setBackground(getDrawable(R.drawable.background_play_pause));
                        playCycle();
                    }
                } else {
                    Toast.makeText(this, "Select a Song . .", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.img_btn_replay:
                if (mRepeatFlag) {
                    Toast.makeText(this, "Replaying Removed..", Toast.LENGTH_SHORT).show();
                    mBtnReplay.setImageResource(R.drawable.ic_repeat);
                    mMediaPlayer.setLooping(false);
                    mRepeatFlag = false;
                } else {
                    Toast.makeText(this, "Replaying Added..", Toast.LENGTH_SHORT).show();
                    mBtnReplay.setImageResource(R.drawable.ic_repeat_one);
                    mMediaPlayer.setLooping(true);
                    mRepeatFlag = true;
                }
                break;
            case R.id.img_btn_previous:
                mBtnReplay.setImageResource(R.drawable.ic_repeat);
                if (mCheckFlag) {
                    if (mSeekbarController.getProgress() > 3000) {
                        attachMusic(mSongsList.get(mCurrentPosition).getTitle(), mSongsList.get(mCurrentPosition).getPath());
                    } else {
                        if (mCurrentPosition > 0 ) {

                            mSongsList.get(mCurrentPosition).setPlay(0);
                            mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                            mCurrentPosition = mCurrentPosition - 1;
                            setCountPlay(mSongsList.get(mCurrentPosition));
                            attachMusic(mSongsList.get(mCurrentPosition).getTitle(), mSongsList.get(mCurrentPosition).getPath());
                            musicNextPre(mSongsList, mCurrentPosition);
                        } else {
                            mSongsList.get(mCurrentPosition).setPlay(0);
                            mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                            mCurrentPosition = mSongsList.size() - 1;
                            setCountPlay(mSongsList.get(mCurrentPosition));
                            musicNextPre(mSongsList, mCurrentPosition);
                            attachMusic(mSongsList.get(mCurrentPosition).getTitle(), mSongsList.get(mCurrentPosition).getPath());
                        }
                    }
                }
                break;
            case R.id.img_btn_next:
                mBtnReplay.setImageResource(R.drawable.ic_repeat);
                if (mCheckFlag) {
                    if (mCurrentPosition + 1 < mSongsList.size()) {
                        mSongsList.get(mCurrentPosition).setPlay(0);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                        mCurrentPosition += 1;
                        attachMusic(mSongsList.get(mCurrentPosition).getTitle(), mSongsList.get(mCurrentPosition).getPath());
                        musicNextPre(mSongsList, mCurrentPosition);
                        setCountPlay(mSongsList.get(mCurrentPosition));
                    } else {
                        mSongsList.get(mCurrentPosition).setPlay(0);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                        mCurrentPosition = 0;
                        musicNextPre(mSongsList, mCurrentPosition);
                        attachMusic(mSongsList.get(mCurrentPosition).getTitle(), mSongsList.get(mCurrentPosition).getPath());
                        setCountPlay(mSongsList.get(mCurrentPosition));
                        mCheckFlag = true;
                    }
                } else {
                    Toast.makeText(this, "Select the Song ..", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.img_btn_shuffle:{
                mBtnShuffle.setImageResource(R.drawable.ic_shuffle_black);
                break;
            }
            case R.id.img_btn_like:{
                if (mLikeFlag) {
                    mLikeFlag = false;
                    mFavoritesOperations.removeSong(mSongsList.get(mCurrentPosition).getTitle());
                    mSongsList.get(mCurrentPosition).setLike(0);
                    mSongsList.get(mCurrentPosition).setCountOfPlay(0);
                    mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                    intentService(mCurrentPosition);
                    mBtnLike.setImageResource(R.drawable.ic_like);

                } else {
                    mLikeFlag = true;
                    mDislikeFlag = false;

                    mSongsList.get(mCurrentPosition).setLike(1);
                    if (! mFavoritesOperations.checkFavorites(mSongsList.get(mCurrentPosition).getTitle())){
                        favMusic(mSongsList.get(mCurrentPosition));
                    }

                    intentService(mCurrentPosition);

                    mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                    mBtnLike.setImageResource(R.drawable.ic_like_black);
                    mBtnDisLike.setImageResource(R.drawable.ic_dislike);
                }
                break;
            }
            case R.id.img_btn_dislike:
                if (mDislikeFlag) {
                    mDislikeFlag = false;
                    mSongsList.get(mCurrentPosition).setLike(0);

                    mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                    intentService(mCurrentPosition);

                    mBtnDisLike.setImageResource(R.drawable.ic_dislike);
                } else {
                    mDislikeFlag = true;
                    mLikeFlag = false;

                    mFavoritesOperations.removeSong(mSongsList.get(mCurrentPosition).getTitle());
                    mSongsList.get(mCurrentPosition).setLike(2);
                    if (mSongsList.get(mCurrentPosition).getCountOfPlay()>=3) {
                        mSongsList.get(mCurrentPosition).setCountOfPlay(0);
                    }
                    mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                    intentService(mCurrentPosition);

                    mBtnDisLike.setImageResource(R.drawable.ic_dislike_black);
                    mBtnLike.setImageResource(R.drawable.ic_like);
                }
                break;
        }
    }

    private void attachMusic(String name, String path) {
        mBtnPlayPause.setImageResource(R.drawable.ic_play_black);
        mToolbar.setTitle(name);
        mToolbar.setTitleTextColor(R.color.light_color);
        mFavFlag = true;
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
            setControls();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(mp -> {
            mBtnPlayPause.setImageResource(R.drawable.ic_play_black);
            if (mPlayContinueFlag) {
                if (mCurrentPosition + 1 < mSongsList.size()) {
                    mSongsList.get(mCurrentPosition).setPlay(0);
                    mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                    mCurrentPosition += 1;
                    attachMusic(mSongsList.get(mCurrentPosition).getTitle(), mSongsList.get(mCurrentPosition).getPath());
                    musicNextPre(mSongsList, mCurrentPosition);
                    setCountPlay(mSongsList.get(mCurrentPosition));
                    if (getSupportFragmentManager().getBackStackEntryCount() == 1 && !mCheckScreen) {
                        getPositionSong();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
                    }
                } else {
                    mSongsList.get(mCurrentPosition).setPlay(0);
                    mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                    mCurrentPosition = 0;
                    setCountPlay(mSongsList.get(mCurrentPosition));
                    musicNextPre(mSongsList, mCurrentPosition);
                    attachMusic(mSongsList.get(mCurrentPosition).getTitle(), mSongsList.get(mCurrentPosition).getPath());
                    if (getSupportFragmentManager().getBackStackEntryCount() == 1 && !mCheckScreen) {
                        getPositionSong();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
                    }
                }
            }
        });
    }

    private void setControls() {
        mSeekbarController.setMax(mMediaPlayer.getDuration());
        playCycle();
        mCheckFlag = true;
        if (mMediaPlayer.isPlaying()) {
            mBtnPlayPause.setImageResource(R.drawable.pause_icon);
            mBtnPlayPause.setBackground(getDrawable(R.drawable.background_play_pause));
            mTotalTime.setText(getTimeFormatted(mMediaPlayer.getDuration()));
        }
        mSeekbarController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mMediaPlayer.seekTo(progress);
                    mCurrentTime.setText(getTimeFormatted(progress));
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
            mSeekbarController.setProgress(mMediaPlayer.getCurrentPosition());
            mCurrentTime.setText(getTimeFormatted(mMediaPlayer.getCurrentPosition()));
            if (mMediaPlayer.isPlaying()) {
                mRunnable = MainActivity::playCycle;
                mHandler.postDelayed(mRunnable, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTimeFormatted(long milliSeconds) {
        String finalTimerString = "";
        String secondsString;

        int hours = (int) (milliSeconds / 3600000);
        int minutes = (int) (milliSeconds % 3600000) / 60000;
        int seconds = (int) ((milliSeconds % 3600000) % 60000 / 1000);

        if (hours > 0)
            finalTimerString = hours + ":";

        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        return finalTimerString;
    }

    private void musicNextPre(ArrayList<SongsList> songListNext, int position){
        if (mCkeckPlay) {
            if (mCheckScreen) {
                MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                mediaPlayFragment.setArguments(getBundle(songListNext.get(position)));
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_media, mediaPlayFragment)
                        .addToBackStack("fragment_media").commit();
            } else {
                if (mCheckPlayerSheet) {
                    MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                    mediaPlayFragment.setArguments(getBundle(songListNext.get(position)));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, mediaPlayFragment)
                            .addToBackStack("fragment").commit();
                } else {
                    mTitle.setText(songListNext.get(position).getTitle());
                    mSubtitle.setText(songListNext.get(position).getSubTitle());
                    mImgSong.setImageResource(songListNext.get(position).getImage());
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, new AllSongFragment())
                            .addToBackStack("fragment").commit();
                }
            }
            intentService(mCurrentPosition);
        }
    }

    private void setCountPlay(SongsList songsList) {
        songsList.setCountOfPlay(songsList.getCountOfPlay() + 1);
        songsList.setPlay(1);
        if (songsList.getCountOfPlay()>=3) {
            songsList.setLike(1);
            if (!mFavoritesOperations.checkFavorites(songsList.getTitle())){
                favMusic(songsList);
            }
        }
        mAllSongOperations.updateSong(songsList);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        while (count>2) {
            getSupportFragmentManager().popBackStack();
            count--;
        }
        // super.onBackPressed();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
        mCheckPlayerSheet = false;
        mCkeckPlay = false;
        mPlayerSheetAll.setVisibility(View.VISIBLE);
        mPlayerLayout.setVisibility(View.GONE);
    }

    private Bundle getBundle(SongsList currSong){
        Bundle bundle = new Bundle();
        bundle.putInt(Key.CONST_IMAGE, currSong.getImage());
        bundle.putInt(Key.CONST_LIKE, currSong.isLike());
        bundle.putString(Key.CONST_TITLE, currSong.getTitle());
        bundle.putString(Key.CONST_SUBTITLE, currSong.getSubTitle());
        bundle.putString(Key.PATH_SONG, currSong.getPath());
        return bundle;
    }

    private void intentService(int position) {
        mIntentService = new Intent(this, MediaService.class);
        mIntentService.putExtra(Key.KEY_POSITION, position);
        // sendBroadcast(mIntentService);
        //bindService(mIntentService, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(mIntentService);
    }


    private void favMusic(SongsList favSongList) {
        mFavoritesOperations.addSongFav(favSongList);
    }

    @Override
    public void onDataPass(String name, String path) {
        Toast.makeText(this, name, Toast.LENGTH_LONG).show();
        attachMusic(name, path);
        mCkeckPlay = true;
        mPlayerLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void getLength(int length) {
        this.mAllSongLength = length;
    }

    @Override
    public void onDataPassSong(String name, String path, String subtile, int image, boolean checkSong) {
        Toast.makeText(this, name, Toast.LENGTH_LONG).show();
        if (checkSong) {
            attachMusic(name, path);
        }
        mCkeckPlay = true;
        //mPlayerLayout.setVisibility(View.VISIBLE);
        mTitle.setText(name);
        mSubtitle.setText(subtile);
        mImgSong.setImageResource(image);
        mPlayPauseSong.setImageResource(R.drawable.ic_pause_black);
    }

    @Override
    public void fullSongList(ArrayList<SongsList> songList, int position) {
        this.mSongsList = songList;
        this.mCurrentPosition = position;
        this.mPlayListFlag = songList.size() == mAllSongLength;
        this.mPlayContinueFlag = true;
        this.mCheckLikeFlag = true;
    }

    @Override
    public String queryText() {
        return mSearchText.toLowerCase();
    }

    @Override
    public int getPositionSong() {
        return mCurrentPosition;
    }

    @Override
    public boolean isSong() {
        if (mMediaPlayer.isPlaying()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean checkScreen() {
        return mCheckScreen;
    }

    @Override
    public void playCheckSong(boolean checkSong) {
        if (!checkSong) {
            mBtnPlayPause.setImageResource(R.drawable.ic_play_black);
            mBtnPlayPause.setBackground(getDrawable(R.color.background));
        } else {
            mBtnPlayPause.setImageResource(R.drawable.pause_icon);
            mBtnPlayPause.setBackground(getDrawable(R.drawable.background_play_pause));
        }
    }

    @Override
    public void currentSong(SongsList songsList) {
        this.mCurrentSong = songsList;
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
        mMediaPlayer.release();
        mHandler.removeCallbacks(mRunnable);
        unregisterReceiver(mReceiver);
        mAllSongOperations.updatePlaySong(mSongsList);
        // stopService(mIntentService);
    }

    @Override
    public void isLike(int like) {
        switch (like) {
            case 0:
                mLikeFlag = false;
                mDislikeFlag = false;
                mBtnLike.setImageResource(R.drawable.ic_like);
                mBtnDisLike.setImageResource(R.drawable.ic_dislike);
                break;
            case 1:
                mLikeFlag = true;
                mDislikeFlag = false;
                mBtnLike.setImageResource(R.drawable.ic_like_black);
                mBtnDisLike.setImageResource(R.drawable.ic_dislike);
                break;
            case 2:
                mLikeFlag = false;
                mDislikeFlag = true;
                mBtnLike.setImageResource(R.drawable.ic_like);
                mBtnDisLike.setImageResource(R.drawable.ic_dislike_black);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
