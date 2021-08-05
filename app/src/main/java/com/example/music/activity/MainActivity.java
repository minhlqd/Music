package com.example.music.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.Key;
import com.example.music.R;
//import com.example.music.broadcast.MusicReceiver;
import com.example.music.broadcast.MusicReceiver;
import com.example.music.database.AllSongOperations;
import com.example.music.database.FavoritesOperations;
import com.example.music.fragment.AllSongFragment;
import com.example.music.fragment.FavSongFragment;
import com.example.music.fragment.MediaPlaybackFragment;
import com.example.music.interfaces.ICreateDataParseFav;
import com.example.music.interfaces.ICreateDataParseMedia;
import com.example.music.interfaces.ICreateDataParseSong;
import com.example.music.interfaces.INotification;
import com.example.music.model.Song;
import com.example.music.service.MusicService;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements
          View.OnClickListener
        , ICreateDataParseSong
        , ICreateDataParseFav
        , ICreateDataParseMedia
        , INotification{

    public static final String TAG = "MinhMX";

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

    public LinearLayout playerSheetAll;

    public static SeekBar sSeekbarController;
    private DrawerLayout mDrawerLayout;
    public static TextView sCurrentTime;
    private TextView mTotalTime;

    public LinearLayout playerLayout;

    private ArrayList<Song> mSongsList;
    private RecyclerView mRecyclerView;
    private int mCurrentPosition = 0;
    private String mSearchText = "";
    private Song mCurrentSong;

    private FrameLayout mFragmentAllSong;
    private FrameLayout mFragmentMediaPlay;

    private ArrayList<Song> mDataSongList;

    private boolean mCheckFlag = false;
    private boolean mRepeatFlag = false;                //  kiem tra che do lap lai
    private boolean mPlayContinueFlag = true;
    private boolean mPlayListFlag = false;
    private boolean mLikeFlag = false;                  // kiem tra like cua bai hat
    private boolean mDislikeFlag = false;               // kiem tra dislike cua bai hat

    private boolean mCkeckPlay = true;                  // kiem tra xem activity dang hien thi fragment nao

    private boolean mCheckScreen = false;               // kiem tra che do man hinh ( ngang hay doc )

    private boolean mCheckPlayerSheet = false;          // kiem tra player sheet

    private boolean mcheckPlayMusic = false;
    private boolean mIsCheckShuffle = false;            // kiem tra che do ngau nhien bai hat
    private boolean mCheckBackPress = false;            // kiem tra backpress
    private boolean mCheckAcitvity = true;              // kiem tra trang thai cua activity ( onPause hay on onResume )

    private Toolbar mToolbar;

    private final int MY_PERMISSION_REQUEST = 100;
    private int mAllSongLength;

    private FavoritesOperations mFavoritesOperations;   // favorite songs
    private AllSongOperations mAllSongOperations;       // all songs

    private Intent mupdateNotification;                      // intent service

    public static Handler handler;
    public static Runnable runnable;

    private MusicReceiver mReceiver = new MusicReceiver();

    private boolean mIsBinder = false;
    public static MusicService sMusicService;          // service choi nhac

    private Display mDisplay;
    private Intent mIntnetBroadcast;

    private SharedPreferences mPreferences;

    @Override
    protected void onStart() {
        super.onStart();
        sMusicService.setINotification(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDisplay = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        // kiem tra xem dien thoai dang o trang thai nao ( portrait hay landscape)
        if (mDisplay.getRotation() == Surface.ROTATION_90
                || mDisplay.getRotation() == Surface.ROTATION_270) {
            setContentView(R.layout.activity_main_landscape);
            mCheckScreen = true;

        } else {
            setContentView(R.layout.activity_main);
            mCheckScreen = false;

        }

        mAllSongOperations = new AllSongOperations(this);
        sMusicService = new MusicService();

        init();
        grantedPermission();
        getMusic();
        broadcast();

        mPreferences = getSharedPreferences(Key.SHARE_PREFERENCES, MODE_PRIVATE);

        /*if (mPreferences != null) {
            mCurrentPosition = mPreferences.getInt(Key.KEY_POSITION,0);
            Log.d(TAG, "onCreate: " + mCurrentPosition);
            attachMusic(mAllSongOperations.getAllSong().get(mCurrentPosition));
            updateUI(mCurrentPosition);
            mBtnReplay.setImageResource(R.drawable.ic_repeat_one);
            mRepeatFlag = true;
        }*/

        // cap nhat lai trang thai cua UI trc khi xoay man hinh
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(Key.KEY_POSITION);
            Song song = mAllSongOperations.getAllSong().get(mCurrentPosition);
            attachMusic(song);
            song.setPlay(1);
            mAllSongOperations.updateSong(song);
            updateUI(mCurrentPosition);
        } else {
            playMusic(mDataSongList.get(0));
        }

    }

    // anh xa cac view
    @SuppressLint("NonConstantResourceId")
    private void init() {
        // cac button choi nhac
        mBtnPlayPause = findViewById(R.id.img_btn_play);
        mBtnPrev = findViewById(R.id.img_btn_previous);
        mBtnNext = findViewById(R.id.img_btn_next);
        mBtnReplay = findViewById(R.id.img_btn_replay);
        mBtnDisLike = findViewById(R.id.img_btn_dislike);
        mBtnLike = findViewById(R.id.img_btn_like);
        mBtnShuffle = findViewById(R.id.img_btn_shuffle);

        // layout chua cac button choi nhac
        playerLayout = findViewById(R.id.ll_include_controls);

        mFragmentAllSong = findViewById(R.id.fragment);
        mFragmentMediaPlay = findViewById(R.id.fragment_media);

        sSeekbarController = findViewById(R.id.seekbar_controller);

        // textview hien thi thoi gian choi va tong thoi gian cua thanh seekbar
        sCurrentTime = findViewById(R.id.tv_current_time);
        mTotalTime = findViewById(R.id.tv_total_time);

        // cac view thuoc player sheet de hien thi bai hat dang choi o all song fragment
        mTitle = findViewById(R.id.tv_music_name);
        mSubtitle = findViewById(R.id.tv_music_subtitle);
        mPlayPauseSong = findViewById(R.id.play_pause_song);
        mImgSong = findViewById(R.id.iv_music_list);
        playerSheetAll = findViewById(R.id.linear_play_sheet_all);

        NavigationView navigationView = findViewById(R.id.nav_view);
        mDrawerLayout = findViewById(R.id.drawer_layout);

        mToolbar = findViewById(R.id.toolbar);

        handler = new Handler();

        mToolbar.setTitleTextColor(getResources().getColor(R.color.light_color));
        setSupportActionBar(mToolbar);

        // action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);

        AllSongFragment createAllSongs = new AllSongFragment();

        mFavoritesOperations = new FavoritesOperations(this);
        mAllSongOperations = new AllSongOperations(this);

        // xac dinh man hinh ngang hay doc de hien thi cac fragment
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
                    .addToBackStack("Fragment")
                    .commit();
            playerLayout.setVisibility(View.VISIBLE);
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
            playerLayout.setVisibility(View.GONE);
        }

        // bat su kien onclick cho cac button choi nhac
        mBtnNext.setOnClickListener(this);
        mBtnPrev.setOnClickListener(this);
        mBtnReplay.setOnClickListener(this);
        mBtnPlayPause.setOnClickListener(this);
        mBtnLike.setOnClickListener(this);
        mBtnDisLike.setOnClickListener(this);
        mBtnShuffle.setOnClickListener(this);
        mPlayPauseSong.setOnClickListener(this);
        playerSheetAll.setOnClickListener(this);

        // navigation drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            mDrawerLayout.closeDrawers();
            switch (item.getItemId()) {
                case R.id.listen_now:{
                    attachMusic(mSongsList.get(mCurrentPosition));
                    musicNextPre(mSongsList, mCurrentPosition);
                    playerLayout.setVisibility(View.VISIBLE);
                    break;
                }
                case R.id.favorite: {
                    //checkFragmentFav = true;
                    mFavoritesOperations.setPlayMusic(mFavoritesOperations.getAllFavorites());
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new FavSongFragment()).addToBackStack("fragment").commit();
                    break;
                }
                case R.id.library:{
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).addToBackStack("fragment").commit();
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

    // premission
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

    // request permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == MY_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Snackbar snackbar = Snackbar.make(mDrawerLayout, "Provide the Storage Permission", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    finish();
                }
            }
        }
    }

    // lay nhac tu trong database cua may
    private void getMusic() {
        mDataSongList = new ArrayList<>();
        if (mAllSongOperations.getAllSong().size() == 0) {
            Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = getContentResolver().query(songUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int songTitle = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int songSubTitle = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int path = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int duration = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int id = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
                while (cursor.moveToNext()) {
                    Log.d(TAG, "getMusic: " + cursor.getString(id));
                    mDataSongList.add(new Song(cursor.getString(songTitle)
                            , cursor.getString(songSubTitle)
                            , getTimeFormatted(Long.parseLong(cursor.getString(duration)))
                            , cursor.getString(path)
                            , cursor.getLong(id)
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
            mAllSongOperations.updatePlaySong(mDataSongList);
        }
    }

    // display Song dang phat o all song fragment
    private void playMusic(Song song) {
        mTitle.setText(song.getTitle());
        mSubtitle.setText(song.getSubTitle());
        getImageAlbum(this, mImgSong, song.getImage());
        if (sMusicService.isPlaying()) {
            mPlayPauseSong.setImageResource(R.drawable.ic_pause_black);
        } else {
            mPlayPauseSong.setImageResource(R.drawable.ic_play_black);
        }
    }

    // them action va dang ki broadcast
    private void broadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Key.ACTION_PLAY_SONG);
        intentFilter.addAction(Key.ACTION_NEXT_SONG);
        intentFilter.addAction(Key.ACTION_PREVIOUS_SONG);
        intentFilter.addAction(Key.ACTION_LIKE_SONG);
        intentFilter.addAction(Key.ACTION_DISLIKE_SONG);
        registerReceiver(mReceiver, intentFilter);
        // mReceiver.setINotification(this);
    }

    // hien thi thong tin app
    private void about() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.about))
                .setMessage(getString(R.string.about_text))
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // lay text o thanh search va truyen ve all song fragment
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


    // them su kien cho cac id o navigation drawer
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

    // su kien onclick cua cac button
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // layout choi nhac o ben all song fragment
            case R.id.linear_play_sheet_all:{
                if (!mcheckPlayMusic) {
                    mSongsList = mDataSongList;
                    mCurrentPosition = 0;
                    mcheckPlayMusic = true;
                    attachMusic(mSongsList.get(0));
                    MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                    mediaPlayFragment.setArguments(getBundle(mSongsList.get(mCurrentPosition)));
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mediaPlayFragment).commit();
                } else {
                    MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                    mediaPlayFragment.setArguments(getBundle(mSongsList.get(mCurrentPosition)));
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mediaPlayFragment).commit();
                }
                playerSheetAll.setVisibility(View.GONE);
                playerLayout.setVisibility(View.VISIBLE);
                if (sMusicService.isPlaying()) {
                    mBtnPlayPause.setImageResource(R.drawable.pause_icon);
                    mBtnPlayPause.setBackground(getDrawable(R.drawable.background_play_pause));
                } else {
                    mBtnPlayPause.setImageResource(R.drawable.ic_play_black);
                    mBtnPlayPause.setBackground(getDrawable(R.color.background));
                }
                mCkeckPlay = true;
                mCheckBackPress = false;
                mCheckPlayerSheet = true;
                break;
            }

            // play/pause song
            case R.id.play_pause_song:{
                updateNotification(mCurrentPosition);
                if (!mcheckPlayMusic) {
                    attachMusic(mDataSongList.get(0));
                    mPlayPauseSong.setImageResource(R.drawable.ic_pause_black);
                    mSongsList = mDataSongList;
                    mCurrentPosition = 0;
                    mcheckPlayMusic = true;
                } else {
                    if (sMusicService.isPlaying()) {
                        sMusicService.pause();
                        mPlayPauseSong.setImageResource(R.drawable.ic_play_black);
                    } else if (!sMusicService.isPlaying()) {
                        sMusicService.play();
                        mPlayPauseSong.setImageResource(R.drawable.ic_pause_black);
                        playCycle();
                    }
                }
                break;
            }

            // play/pause ben mediafragment
            case R.id.img_btn_play: {
                updateNotification(mCurrentPosition);
                if (mCheckFlag) {
                    if (sMusicService.isPlaying()) {
                        sMusicService.pause();

                        mSongsList.get(mCurrentPosition).setPlay(0);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                        mBtnPlayPause.setImageResource(R.drawable.ic_play_black);
                        mBtnPlayPause.setBackground(getDrawable(R.color.background));
                    } else if (!sMusicService.isPlaying()) {
                        sMusicService.play();

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
            }

            // replay lai bai hat dang dc phat
            case R.id.img_btn_replay: {
                if (mRepeatFlag) {
                    Toast.makeText(this, "Replaying Removed..", Toast.LENGTH_SHORT).show();
                    mBtnReplay.setImageResource(R.drawable.ic_repeat);
                    sMusicService.looping(true);

                    // add vao Preferences
                    SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                    preferencesEditor.putInt(Key.KEY_POSITION, mCurrentPosition);
                    preferencesEditor.apply();

                    mRepeatFlag = false;
                } else {
                    Toast.makeText(this, "Replaying Added..", Toast.LENGTH_SHORT).show();
                    mBtnReplay.setImageResource(R.drawable.ic_repeat_one);
                    sMusicService.looping(false);

                    // clear Preferences
                    SharedPreferences.Editor preferencesEditor = mPreferences.edit();
                    preferencesEditor.clear();
                    preferencesEditor.apply();

                    mRepeatFlag = true;
                }
                break;
            }

            // quay lai bai hat truoc do
            case R.id.img_btn_previous: {
                mBtnReplay.setImageResource(R.drawable.ic_repeat);
                if (mCheckFlag) {

                    // neu bai hat chay dc hon 3s thi phat lai tu dau
                    if (sSeekbarController.getProgress() > 3000) {
                        attachMusic(mSongsList.get(mCurrentPosition));
                    } else {
                        if (mCurrentPosition > 0) {

                            mSongsList.get(mCurrentPosition).setPlay(0);
                            mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                            mCurrentPosition = mCurrentPosition - 1;
                            setCountPlay(mSongsList.get(mCurrentPosition));
                            attachMusic(mSongsList.get(mCurrentPosition));
                            musicNextPre(mSongsList, mCurrentPosition);
                        } else {
                            mSongsList.get(mCurrentPosition).setPlay(0);
                            mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                            mCurrentPosition = mSongsList.size() - 1;
                            setCountPlay(mSongsList.get(mCurrentPosition));
                            musicNextPre(mSongsList, mCurrentPosition);
                            attachMusic(mSongsList.get(mCurrentPosition));
                        }
                    }
                }
                break;
            }

            // chuyen den bai hat tiep theo
            case R.id.img_btn_next: {
                mBtnReplay.setImageResource(R.drawable.ic_repeat);
                if (mCheckFlag) {
                    if (mCurrentPosition + 1 < mSongsList.size()) {
                        mSongsList.get(mCurrentPosition).setPlay(0);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));
                        mCurrentPosition += 1;
                        attachMusic(mSongsList.get(mCurrentPosition));
                        musicNextPre(mSongsList, mCurrentPosition);
                        setCountPlay(mSongsList.get(mCurrentPosition));
                    } else {
                        mSongsList.get(mCurrentPosition).setPlay(0);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                        mCurrentPosition = 0;
                        musicNextPre(mSongsList, mCurrentPosition);
                        attachMusic(mSongsList.get(mCurrentPosition));
                        setCountPlay(mSongsList.get(mCurrentPosition));
                        mCheckFlag = true;
                    }
                } else {
                    Toast.makeText(this, "Select the Song ..", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            // phat ngau nhien bai hat trong list nhac
            case R.id.img_btn_shuffle:{
                if (mIsCheckShuffle) {
                    mBtnShuffle.setImageResource(R.drawable.ic_shuffle);
                    mIsCheckShuffle = false;
                } else {
                    mIsCheckShuffle = true;
                    mBtnShuffle.setImageResource(R.drawable.ic_shuffle_black);
                    sMusicService.getMediaPlayer().setOnCompletionListener(mp -> {
                        mBtnPlayPause.setImageResource(R.drawable.ic_play_black);
                        mCurrentPosition = randomSong(mCurrentPosition);
                        attachMusic(mSongsList.get(mCurrentPosition));
                        musicNextPre(mSongsList, mCurrentPosition);
                        setCountPlay(mSongsList.get(mCurrentPosition));

                    });
                }
                break;
            }

            // like bai hat va them vao danh sach ua thich
            case R.id.img_btn_like:{
                if (mLikeFlag) {
                    mLikeFlag = false;
                    mFavoritesOperations.removeSong(mSongsList.get(mCurrentPosition).getTitle());
                    mSongsList.get(mCurrentPosition).setLike(0);
                    mSongsList.get(mCurrentPosition).setCountOfPlay(0);
                    mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                    updateNotification(mCurrentPosition);
                    mBtnLike.setImageResource(R.drawable.ic_like);

                } else {
                    mLikeFlag = true;
                    mDislikeFlag = false;

                    mSongsList.get(mCurrentPosition).setLike(1);
                    if (! mFavoritesOperations.checkFavorites(mSongsList.get(mCurrentPosition).getTitle())){
                        favMusic(mSongsList.get(mCurrentPosition));
                    }

                    updateNotification(mCurrentPosition);

                    mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                    mBtnLike.setImageResource(R.drawable.ic_like_black);
                    mBtnDisLike.setImageResource(R.drawable.ic_dislike);
                }
                break;
            }

            // dislike bai hat va xoa khoi danh sach yeu thich
            case R.id.img_btn_dislike: {
                if (mDislikeFlag) {
                    mDislikeFlag = false;
                    mSongsList.get(mCurrentPosition).setLike(0);

                    mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                    updateNotification(mCurrentPosition);

                    mBtnDisLike.setImageResource(R.drawable.ic_dislike);
                } else {
                    mDislikeFlag = true;
                    mLikeFlag = false;

                    mFavoritesOperations.removeSong(mSongsList.get(mCurrentPosition).getTitle());
                    mSongsList.get(mCurrentPosition).setLike(2);
                    if (mSongsList.get(mCurrentPosition).getCountOfPlay() >= 3) {
                        mSongsList.get(mCurrentPosition).setCountOfPlay(0);
                    }
                    mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));
                    updateNotification(mCurrentPosition);
                    mBtnDisLike.setImageResource(R.drawable.ic_dislike_black);
                    mBtnLike.setImageResource(R.drawable.ic_like);
                }
                break;
            }
        }
    }

    // phat nhac
    private void attachMusic(Song song){
        mBtnPlayPause.setImageResource(R.drawable.ic_play_black);
        if (!mCheckScreen) {
            mToolbar.setTitle(song.getTitle());
        }
        mToolbar.setTitleTextColor(R.color.light_color);

        sMusicService.playMedia(song);
        setControls();

        // neu ko che do phat ngau nhien thi se next bai nhu bthg
        if (!mIsCheckShuffle) {
            sMusicService.getMediaPlayer().setOnCompletionListener(mp -> {
                mBtnPlayPause.setImageResource(R.drawable.ic_play_black);
                if (mPlayContinueFlag) {
                    if (mCurrentPosition + 1 < mSongsList.size()) {
                        mSongsList.get(mCurrentPosition).setPlay(0);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                        mCurrentPosition += 1;
                        attachMusic(mSongsList.get(mCurrentPosition));
                        musicNextPre(mSongsList, mCurrentPosition);
                        setCountPlay(mSongsList.get(mCurrentPosition));

                        if (mCheckBackPress && !mCheckScreen && mCheckAcitvity) {
                            playMusic(mSongsList.get(mCurrentPosition));
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
                        }
                    } else {
                        mSongsList.get(mCurrentPosition).setPlay(0);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));
                        mCurrentPosition = 0;
                        setCountPlay(mSongsList.get(mCurrentPosition));
                        musicNextPre(mSongsList, mCurrentPosition);
                        attachMusic(mSongsList.get(mCurrentPosition));
                        if (getSupportFragmentManager().getBackStackEntryCount() == 0 && !mCheckScreen) {
                            playMusic(mSongsList.get(mCurrentPosition));
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
                        }
                    }
                }
            });
        }
    }

    // set thoie gian chay tren thanh seek bar
    private void setControls() {
        sSeekbarController.setMax(sMusicService.getMediaPlayer().getDuration());
        playCycle();
        mCheckFlag = true;
        Log.d(TAG, "setControls: " + sMusicService.isPlaying());
        if (sMusicService.isPlaying()) {
            mBtnPlayPause.setImageResource(R.drawable.pause_icon);
            mBtnPlayPause.setBackground(getDrawable(R.drawable.background_play_pause));
            mTotalTime.setText(getTimeFormatted(sMusicService.getMediaPlayer().getDuration()));
            Log.d(TAG, "setControls: " + mTotalTime.getText());
        }
        sSeekbarController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    sMusicService.getMediaPlayer().seekTo(progress);
                    sCurrentTime.setText(getTimeFormatted(progress));
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

    // lay thoi chay
    public static void playCycle() {
        try {
            sSeekbarController.setProgress(sMusicService.getMediaPlayer().getCurrentPosition());
            sCurrentTime.setText(getTimeFormatted(sMusicService.getMediaPlayer().getCurrentPosition()));
            if (sMusicService.isPlaying()) {
                runnable = MainActivity::playCycle;
                handler.postDelayed(runnable, 100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // dinh dang lai thoi gian
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

    // hien thi bai hat tiep theo
    private void musicNextPre(ArrayList<Song> songList, int position){
        if (mCkeckPlay) {
            updateUI(position);
        }
        updateNotification(mCurrentPosition);
    }

    // tang luot choi nhac
    // neu luot choi lon hon 3 tu dong cho vao danh sach yeu thich
    private void setCountPlay(Song song) {
        song.setCountOfPlay(song.getCountOfPlay() + 1);
        song.setPlay(1);
        if (song.getCountOfPlay()>=3) {
            song.setLike(1);
            if (!mFavoritesOperations.checkFavorites(song.getTitle())){
                favMusic(song);
            }
        }
        mAllSongOperations.updateSong(song);
    }

    // ngau nhien bai hat tiep theo
    private int randomSong(int position) {
        Random random = new Random();
        int rdPos = random.nextInt(mSongsList.size());
        while (position == rdPos ) {
            rdPos = random.nextInt(mSongsList.size());
        }
        return rdPos;
    }

    // back ve fragment dau tien
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        while (count>0) {
            getSupportFragmentManager().popBackStack();
            count--;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
        mCheckPlayerSheet = false;
        mCkeckPlay = false;
        playMusic(mSongsList.get(mCurrentPosition));
        playerSheetAll.setVisibility(View.VISIBLE);
        playerLayout.setVisibility(View.GONE);
        if (sMusicService.isPlaying()) {
            mPlayPauseSong.setImageResource(R.drawable.ic_pause_black);
        } else {
            mPlayPauseSong.setImageResource(R.drawable.ic_play_black);
        }
        mCheckBackPress = true;
    }

    // gui du lieu qua bundle
    private Bundle getBundle(Song currSong){
        Bundle bundle = new Bundle();
        bundle.putLong(Key.CONST_IMAGE, currSong.getImage());
        bundle.putInt(Key.CONST_LIKE, currSong.isLike());
        bundle.putString(Key.CONST_TITLE, currSong.getTitle());
        bundle.putString(Key.CONST_SUBTITLE, currSong.getSubTitle());
        bundle.putString(Key.PATH_SONG, currSong.getPath());
        return bundle;
    }

    // cap nhat lai notification
    private void updateNotification(int position) {
        Song song = null;
        if (mSongsList != null) {
            song = mSongsList.get(position);
        } else {
            song = mDataSongList.get(position);
        }
        song.setPlay(1);
        mAllSongOperations.updateSong(song);
        sMusicService.sendNotification(this, song, position);
    }


    // them bai hat vao danh sach yeu thich
    private void favMusic(Song favSongList) {
        mFavoritesOperations.addSongFav(favSongList);
    }

    // phat bai hat duoc gui tu favorite fragment
    @Override
    public void onDataPass(Song song) {
        attachMusic(song);
        mCkeckPlay = true;
        playerLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void getLength(int length) {
        this.mAllSongLength = length;
    }

    // phat bai hat duoc gui tu allsongfragment
    @Override
    public void onDataPassSong(Song song) {
        attachMusic(song);
        mCkeckPlay = true;
        mTitle.setText(song.getTitle());
        mSubtitle.setText(song.getSubTitle());
        getImageAlbum(this, mImgSong, song.getImage());
        mCheckPlayerSheet = false;
        mcheckPlayMusic = true;
        mPlayPauseSong.setImageResource(R.drawable.ic_pause_black);
    }

    // lay path anh cua bat hat tu album
    private Uri queryAlbumUri(long id) {
        final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri,id);
    }

    // hien thi anh bai hai thong qua thu vien Glide
    private void getImageAlbum(Context context, ImageView view, Long image){
        Glide.with(context)
                .load(queryAlbumUri(image))
                .placeholder(R.drawable.ic_music_player)
                .into(view);
    }

    @Override
    public void fullSongList(ArrayList<Song> songList, int position) {
        this.mSongsList = songList;
        this.mCurrentPosition = position;
        this.mPlayListFlag = songList.size() == mAllSongLength;
        this.mPlayContinueFlag = true;
    }

    // gui cau tim kiem sang allsongfragment va favoritefragment
    @Override
    public String queryText() {
        return mSearchText.toLowerCase();
    }


    @Override
    public boolean isSong() {
        if (sMusicService.isPlaying()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean checkScreen() {
        return mCheckScreen;
    }

    // phat bai hat tiep theo da duoc chon o ben allsongfragment
    @Override
    public void currentSong(Song song, int position) {
        this.mCurrentSong = song;
        sMusicService.getMediaPlayer().setOnCompletionListener(mp -> {
            mSongsList.get(mCurrentPosition).setPlay(0);
            mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));
            attachMusic(mCurrentSong);
            playMusic(mCurrentSong);
            setCountPlay(mCurrentSong);
            mCurrentPosition = position;
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, new AllSongFragment())
                    .addToBackStack("fragment")
                    .commit();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mCheckAcitvity) {
            if (mCheckPlayerSheet) {
                MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                mediaPlayFragment.setArguments(getBundle(mSongsList.get(mCurrentPosition)));
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, mediaPlayFragment)
                        .addToBackStack("fragment_media").commit();
                setControls();
            } else {
                playMusic(mSongsList.get(mCurrentPosition));
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new AllSongFragment())
                        .addToBackStack("fragment").commit();
            }
        }
        mCheckAcitvity = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCheckAcitvity = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sMusicService.getMediaPlayer().release();
        handler.removeCallbacks(runnable);
        unregisterReceiver(mReceiver);
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


    // luu trang thai khi xoay man hinh
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Key.KEY_POSITION, mCurrentPosition);
    }

    // update lai ui khi cap nhat lai thong bao
    @Override
    public void nextPlay(int position) {
        this.mCurrentPosition = position;
        Log.d(TAG, "nextPlay: " + mCheckScreen);
        updateUI(position);
    }

    private void updateUI(int position) {
        if (mCheckAcitvity) {
            if (mCheckScreen) {
                MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                if (mSongsList == null) {
                    mSongsList = mAllSongOperations.getAllSong();
                }
                // setTitle(mSongsList.get(position).getTitle());
                setControls();
                mediaPlayFragment.setArguments(getBundle(mSongsList.get(position)));
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_media, mediaPlayFragment)
                        .addToBackStack("fragment_media").commit();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new AllSongFragment())
                        .addToBackStack("fragment").commit();

            } else {
                if (mCheckPlayerSheet) {
                    MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                    mediaPlayFragment.setArguments(getBundle(mSongsList.get(position)));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, mediaPlayFragment)
                            .addToBackStack("fragment_media").commit();
                    setControls();
                } else {
                    if (mSongsList == null) {
                        mSongsList = mAllSongOperations.getAllSong();
                    }
                    mToolbar.setTitle(mSongsList.get(position).getTitle());
                    playMusic(mSongsList.get(position));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, new AllSongFragment())
                            .addToBackStack("fragment").commit();
                }
            }
        }
    }
}
