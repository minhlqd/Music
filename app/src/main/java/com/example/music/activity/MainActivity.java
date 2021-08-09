package com.example.music.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.music.Key;
import com.example.music.R;
import com.example.music.broadcast.MusicReceiver;
import com.example.music.database.AllSongOperations;
import com.example.music.database.FavoritesOperations;
import com.example.music.fragment.AllSongFragment;
import com.example.music.fragment.FavSongFragment;
import com.example.music.fragment.MediaPlaybackFragment;
import com.example.music.interfaces.ICreateDataParseFav;
import com.example.music.interfaces.ICreateDataParseSong;
import com.example.music.interfaces.INotification;
import com.example.music.model.Song;
import com.example.music.service.MusicService;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements
        View.OnClickListener
        , ICreateDataParseSong
        , ICreateDataParseFav
        , INotification{

    public static final String TAG = "MinhMX";
    private Menu menu;
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

    private boolean mPlayContinueFlag = true;

    private boolean mCkeckPlay = true;                  // kiem tra xem activity dang hien thi fragment nao

    private boolean mCheckScreen = false;               // kiem tra che do man hinh ( ngang hay doc )

    private boolean mCheckPlayerSheet = false;          // kiem tra player sheet

    private boolean mcheckPlayMusic = false;
    private boolean mCheckBackPress = false;            // kiem tra backpressed
    private boolean mCheckAcitvity = true;              // kiem tra trang thai cua activity ( onPause hay onResume )

    private boolean mCheckAttach = false;

    private Toolbar mToolbar;

    private final int MY_PERMISSION_REQUEST = 100;
    private int mAllSongLength;

    private FavoritesOperations mFavoritesOperations;   // favorite songs
    private AllSongOperations mAllSongOperations;       // all songs

    private Intent mupdateNotification;                      // intent service

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

        mFavoritesOperations = new FavoritesOperations(this);
        mAllSongOperations = new AllSongOperations(this);
        mSongsList = mAllSongOperations.getAllSong();
        sMusicService = new MusicService();

        init();
        grantedPermission();
        getMusic();
        broadcast();

        mPreferences = getSharedPreferences(Key.SHARE_PREFERENCES, MODE_PRIVATE);

//        Intent intent = this.getIntent();
//        if (intent.getIntExtra(Key.KEY_POSITION, -1) != -1) {
//            mCurrentPosition = intent.getIntExtra(Key.KEY_POSITION, -1);
//            playSong(mCurrentPosition);
//            playMusic(mSongsList.get(mCurrentPosition));
//            Log.d(TAG, "onCreate: " + mSongsList.get(mCurrentPosition).getTitle());
//        }

        // cap nhat lai trang thai cua UI sau khi xoay man hinh
        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(Key.KEY_POSITION);
            attachMusic(mDataSongList.get(mCurrentPosition));
            playSong(mCurrentPosition);
        } else {
             playMusic(mDataSongList.get(0));
        }

    }

    private void playSong(int posistion) {
        Song song = mSongsList.get(posistion);
        song.setPlay(1);
        mAllSongOperations.updateSong(song);
        updateUI(posistion);
    }

    // anh xa cac view
    @SuppressLint("NonConstantResourceId")
    private void init() {

        // layout chua cac button choi nhac
        // playerLayout = findViewById(R.id.ll_include_controls);

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

        mToolbar.setTitleTextColor(getResources().getColor(R.color.light_color));
        setSupportActionBar(mToolbar);

        // action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.menu_icon);

        AllSongFragment createAllSongs = new AllSongFragment();

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
        }


        mPlayPauseSong.setOnClickListener(this);
        playerSheetAll.setOnClickListener(this);

        // navigation drawer
        navigationView.setNavigationItemSelectedListener(item -> {
            mDrawerLayout.closeDrawers();
            switch (item.getItemId()) {
                case R.id.listen_now:{
                    attachMusic(mSongsList.get(mCurrentPosition));
                    musicNextPre(mSongsList, mCurrentPosition);
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
        song.getImageAlbum(this, mImgSong, song.getImage());
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

    //TODO HoanNTg: Xem lại phần này, tại sao dùng adapter xử lý mà phải replace lại AllSongFragment
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
                }
                replaceFragment(mSongsList.get(mCurrentPosition), mCurrentPosition);
                mCheckAttach = false;
                playerSheetAll.setVisibility(View.GONE);
                mCkeckPlay = true;
                mCheckBackPress = false;
                mCheckPlayerSheet = true;
                break;
            }
            // play/pause song
            case R.id.play_pause_song:{
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
                    }
                }
                updateNotification(mCurrentPosition);
                break;
            }
        }
    }

    private void replaceFragment(Song song, int position){
        MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
        mediaPlayFragment.setArguments(getBundle(song, position));
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment, mediaPlayFragment)
                .addToBackStack("fragment")
                .commit();
    }

    // phat nhac
    private void attachMusic(Song song){
        if (!mCheckScreen) {
            mToolbar.setTitle(song.getTitle());
        }
        mCheckAttach = true;
        mToolbar.setTitleTextColor(R.color.light_color);

        sMusicService.playMedia(song);
        // neu ko che do phat ngau nhien thi se next bai nhu bthg
        sMusicService.getMediaPlayer().setOnCompletionListener(mp -> {
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


    // back ve fragment dau tien
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        while (count>0) {
            getSupportFragmentManager().popBackStack();
            count--;
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, new AllSongFragment()).commit();
        playMusic(mSongsList.get(mCurrentPosition));
        playerSheetAll.setVisibility(View.VISIBLE);
        if (sMusicService.isPlaying()) {
            mPlayPauseSong.setImageResource(R.drawable.ic_pause_black);
        } else {
            mPlayPauseSong.setImageResource(R.drawable.ic_play_black);
        }
        mCheckBackPress = true;
        mCheckPlayerSheet = false;
        mCkeckPlay = false;
        mCheckAttach = true;
    }

    // gui du lieu qua bundle
    private Bundle getBundle(Song currSong, int position){
        Bundle bundle = new Bundle();
        bundle.putLong(Key.CONST_IMAGE, currSong.getImage());
        bundle.putInt(Key.CONST_LIKE, currSong.isLike());
        bundle.putString(Key.CONST_TITLE, currSong.getTitle());
        bundle.putString(Key.CONST_SUBTITLE, currSong.getSubTitle());
        bundle.putString(Key.PATH_SONG, currSong.getPath());
        bundle.putInt(Key.KEY_POSITION, position);
        return bundle;
    }

    // cap nhat lai notification
    private void updateNotification(int position) {
        Song song = null;
        if (mSongsList != null) {
            song = mSongsList.get(position);
        }
        if (sMusicService.isPlaying()) {
            song.setPlay(1);
        } else {
            song.setPlay(0);
        }
        mAllSongOperations.updateSong(song);
        sMusicService.sendNotification(this, song, position);
    }


    // them bai hat vao danh sach yeu thich
    private void favMusic(Song favSong) {
        mFavoritesOperations.addSongFav(favSong);
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
        song.getImageAlbum(this, mImgSong, song.getImage());
        mCheckPlayerSheet = false;
        mcheckPlayMusic = true;
        mPlayPauseSong.setImageResource(R.drawable.ic_pause_black);
    }


    @Override
    public void fullSongList(ArrayList<Song> songList, int position) {
        this.mSongsList = songList;
        this.mCurrentPosition = position;
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
                mediaPlayFragment.setArguments(getBundle(mSongsList.get(mCurrentPosition), mCurrentPosition));
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, mediaPlayFragment)
                        .addToBackStack("fragment_media").commit();
            } else {
                if (mSongsList == null) {
                    mSongsList = mAllSongOperations.getAllSong();
                }
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
        Log.d(TAG, "onDestroy: ");
        sMusicService.getMediaPlayer().release();
        unregisterReceiver(mReceiver);
    }

    // luu trang thai khi xoay man hinh
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Key.KEY_POSITION, mCurrentPosition);
    }

    // update lai ui khi cap nhat lai thong bao
    @Override
    public void onClickNotification(int position) {
        this.mCurrentPosition = position;
        updateUI(position);
    }

    private void updateUI(int position) {
        if (mCheckAcitvity) {
            if (mCheckScreen) {
                MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                if (mSongsList == null) {
                    mSongsList = mDataSongList;
                }
                mediaPlayFragment.setArguments(getBundle(mSongsList.get(position), mCurrentPosition));
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_media, mediaPlayFragment)
                        .addToBackStack("fragment_media").commit();
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment, new AllSongFragment())
                        .addToBackStack("fragment").commit();

            } else {
                if (!mCheckPlayerSheet) {
                    if (mSongsList == null) {
                        mSongsList = mDataSongList;
                    }
                    mToolbar.setTitle(mSongsList.get(position).getTitle());
                    playMusic(mSongsList.get(position));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, new AllSongFragment())
                            .addToBackStack("fragment").commit();
                } else {
                    mSongsList = mAllSongOperations.getAllSong();
                    MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                    mediaPlayFragment.setArguments(getBundle(mSongsList.get(position), mCurrentPosition));
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, mediaPlayFragment)
                            .addToBackStack("fragment_media").commit();
                }
            }
        }
    }
}
