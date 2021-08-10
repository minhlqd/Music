package com.example.music.fragment;


import static com.example.music.activity.MainActivity.sMusicService;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music.Key;
import com.example.music.R;
import com.example.music.activity.MainActivity;
import com.example.music.database.AllSongOperations;
import com.example.music.database.FavoritesOperations;
import com.example.music.model.Song;
import com.example.music.service.MusicService;

import java.util.ArrayList;
import java.util.Random;

;

@SuppressWarnings("ALL")
public class MediaPlaybackFragment extends ListFragment
        implements PopupMenu.OnMenuItemClickListener
        , View.OnClickListener {

    public ArrayList<Song> mSongsList = new ArrayList<>();

    private int mCurrentPosition;

    private AllSongOperations mAllSongOperations;
    private RecyclerView mRecyclerView;

    private RelativeLayout mMusicDetail;

    private ImageView mImageMusic;
    private ImageView mSmallImageMusic;
    private ImageView mLibaryMusic;
    private ImageView mMenuPopup;
    private TextView mTitle;
    private TextView mSubTitle;
    private TextView mTvPosition;

    private ImageButton mBtnPlayPause;
    private ImageView mBtnReplay;
    private ImageButton mBtnPrev;
    private ImageButton mBtnNext;
    private ImageButton mBtnDisLike;
    private ImageButton mBtnLike;
    private ImageView mBtnShuffle;

    private FrameLayout mLibraryLayout;

    private SeekBar mSeekbarController;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    
    
    
    private int mPosition;

    private boolean mCheckFlag = false;
    private boolean mRepeatFlag = false;                //  kiem tra che do lap lai
    private boolean mPlayContinueFlag = true;
    private boolean mCheckLibrary = false;
    private boolean mLikeFlag = false;                  // kiem tra like cua bai hat
    private boolean mDislikeFlag = false;               // kiem tra dislike cua bai hat

    private boolean mCkeckPlay = true;                  // kiem tra xem activity dang hien thi fragment nao

    private boolean mCheckScreen = false;               // kiem tra che do man hinh ( ngang hay doc )

    private boolean mCheckPlayerSheet = false;          // kiem tra player sheet

    private boolean mcheckPlayMusic = false;
    private boolean mIsCheckShuffle = false;            // kiem tra che do ngau nhien bai hat
    private boolean mCheckBackPress = false;            // kiem tra backpress
    private boolean mCheckAcitvity = true;              // kiem tra trang thai cua activity ( onPause hay on onResume )

    private FavoritesOperations mFavoritesOperations;

    //HoanNTg TODO: Sao service lại phải để 2 biến là static nhỉ
    public static Handler handler;
    public static Runnable runnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler();
    }

    @Override
    public void onResume() {
        super.onResume();
        Display display = ((WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (display.getRotation() == Surface.ROTATION_90 || display.getRotation() == Surface.ROTATION_270) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            // mMusicDetail.setVisibility(View.GONE);
        } else {
            mMusicDetail.setVisibility(View.VISIBLE);
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mFavoritesOperations = new FavoritesOperations(context);
        mAllSongOperations = new AllSongOperations(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mImageMusic = view.findViewById(R.id.image_music);
        mSmallImageMusic = view.findViewById(R.id.iv_music_list);
        mLibaryMusic = view.findViewById(R.id.queue_music);
        mTitle = view.findViewById(R.id.tv_music_name);
        mSubTitle = view.findViewById(R.id.tv_music_subtitle);
        mMusicDetail = view.findViewById(R.id.music_detail);
        mTvPosition = view.findViewById(R.id.position);
        mMenuPopup = view.findViewById(R.id.more_vert);

        // cac button choi nhac
        mBtnPlayPause = view.findViewById(R.id.img_btn_play);
        mBtnPrev = view.findViewById(R.id.img_btn_previous);
        mBtnNext = view.findViewById(R.id.img_btn_next);
        mBtnReplay = view.findViewById(R.id.img_btn_replay);
        mBtnDisLike = view.findViewById(R.id.img_btn_dislike);
        mBtnLike = view.findViewById(R.id.img_btn_like);
        mBtnShuffle = view.findViewById(R.id.img_btn_shuffle);
        

        mLibraryLayout = view.findViewById(R.id.library);

        mSeekbarController = view.findViewById(R.id.seekbar_controller);

        // textview hien thi thoi gian choi va tong thoi gian cua thanh seekbar
        mCurrentTime = view.findViewById(R.id.tv_current_time);
        mTotalTime = view.findViewById(R.id.tv_total_time);

        mSmallImageMusic.setVisibility(View.VISIBLE);
        mTvPosition.setVisibility(View.GONE);

        mMenuPopup.setOnClickListener(v -> {
            showMenuPopup(v);
        });

        mSongsList = mAllSongOperations.getAllSong();

        setControls();
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mCurrentPosition = bundle.getInt(Key.KEY_POSITION);
            Song song = mSongsList.get(mCurrentPosition);

            mTitle.setText(bundle.getString(Key.CONST_TITLE));
            mSubTitle.setText(bundle.getString(Key.CONST_SUBTITLE));

            song.getImageAlbum(getContext(), mImageMusic, bundle.getLong(Key.CONST_IMAGE));
            song.getImageAlbum(getContext(), mSmallImageMusic, bundle.getLong(Key.CONST_IMAGE));

            int like = bundle.getInt(Key.CONST_LIKE);
            likeMuisc(like);
            mIsCheckShuffle = bundle.getBoolean("Shuffle");

            Log.d("aaa", "onViewCreated: + shuffle " + mIsCheckShuffle);

            if (mIsCheckShuffle) {
                mBtnShuffle.setImageResource(R.drawable.ic_shuffle_black);
            }
        }

        completeMusic();

        mLibaryMusic.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnPrev.setOnClickListener(this);
        mBtnReplay.setOnClickListener(this);
        mBtnPlayPause.setOnClickListener(this);
        mBtnLike.setOnClickListener(this);
        mBtnDisLike.setOnClickListener(this);
        mBtnShuffle.setOnClickListener(this);
    }

    // hien menu popup
    private void showMenuPopup(View view){
        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.inflate(R.menu.menu_popup);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorite:{
                Toast.makeText(getContext(), "favorites", Toast.LENGTH_SHORT).show();
                return true;
            }
            case R.id.delete:{
                return false;
            }
        }
        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.queue_music:{
                if (mCheckLibrary) {
                    mImageMusic.setVisibility(View.VISIBLE);
                    mLibraryLayout.setVisibility(View.GONE);
                    mCheckLibrary = false;
                } else {
                    mImageMusic.setVisibility(View.GONE);
                    mLibraryLayout.setVisibility(View.VISIBLE);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.library, new AllSongFragment())
                            .commit();
                    mCheckLibrary = true;
                }
                break;
            }
            // play/pause ben mediafragment
            case R.id.img_btn_play: {
                if (mCheckFlag) {
                    if (sMusicService.isPlaying()) {
                        sMusicService.pause();

                        mSongsList.get(mCurrentPosition).setPlay(0);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                        mBtnPlayPause.setImageResource(R.drawable.ic_play_black);
                        mBtnPlayPause.setBackground(getContext().getDrawable(R.color.background));
                    } else if (!sMusicService.isPlaying()) {
                        sMusicService.play();

                        mSongsList.get(mCurrentPosition).setPlay(1);
                        mAllSongOperations.updateSong(mSongsList.get(mCurrentPosition));

                        mBtnPlayPause.setImageResource(R.drawable.pause_icon);
                        mBtnPlayPause.setBackground(getContext().getDrawable(R.drawable.background_play_pause));
                        playCycle();
                    }
                    updateNotification(mCurrentPosition);
                } else {
                    Toast.makeText(getContext(), "Select a Song . .", Toast.LENGTH_SHORT).show();
                }
                break;
            }

            // replay lai bai hat dang dc phat
            case R.id.img_btn_replay: {
                if (mRepeatFlag) {
                    Toast.makeText(getContext(), "Replaying Removed..", Toast.LENGTH_SHORT).show();
                    mBtnReplay.setImageResource(R.drawable.ic_repeat);
                    sMusicService.looping(true);
                    mRepeatFlag = false;
                } else {
                    Toast.makeText(getContext(), "Replaying Added..", Toast.LENGTH_SHORT).show();
                    mBtnReplay.setImageResource(R.drawable.ic_repeat_one);
                    sMusicService.looping(false);
                    mRepeatFlag = true;
                }
                break;
            }

            // quay lai bai hat truoc do
            case R.id.img_btn_previous: {
                mBtnReplay.setImageResource(R.drawable.ic_repeat);
                if (mCheckFlag) {

                    // neu bai hat chay dc hon 3s thi phat lai tu dau
                    if (mSeekbarController.getProgress() > 3000) {
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
                    Toast.makeText(getContext(), "Select the Song ..", Toast.LENGTH_SHORT).show();
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

                    mBtnLike.setImageResource(R.drawable.ic_like);

                } else {
                    mLikeFlag = true;
                    mDislikeFlag = false;

                    mSongsList.get(mCurrentPosition).setLike(1);
                    if (! mFavoritesOperations.checkFavorites(mSongsList.get(mCurrentPosition).getTitle())){
                        favMusic(mSongsList.get(mCurrentPosition));
                    }

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
        sMusicService.playMedia(song);
        setControls();
        completeMusic();
    }

    private void completeMusic() {
        sMusicService.getMediaPlayer().setOnCompletionListener(mp -> {
            Log.d("aaa", "attachMusic: " + mCurrentPosition);
            if (mPlayContinueFlag) {
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
                    setCountPlay(mSongsList.get(mCurrentPosition));
                    musicNextPre(mSongsList, mCurrentPosition);
                    attachMusic(mSongsList.get(mCurrentPosition));
                }
            }
        });
    }


    // set thoie gian chay tren thanh seek bar
    private void setControls() {
        mSeekbarController.setMax(sMusicService.getMediaPlayer().getDuration());
        playCycle();
        mCheckFlag = true;
        if (sMusicService.isPlaying()) {
            mBtnPlayPause.setImageResource(R.drawable.pause_icon);
            mBtnPlayPause.setBackground(getContext().getDrawable(R.drawable.background_play_pause));
        }
        mTotalTime.setText(getTimeFormatted(sMusicService.getMediaPlayer().getDuration()));
        mSeekbarController.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    sMusicService.getMediaPlayer().seekTo(progress);
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

    // lay thoi chay
    private void playCycle() {
        try {
            mSeekbarController.setProgress(sMusicService.getMediaPlayer().getCurrentPosition());
            mCurrentTime.setText(getTimeFormatted(sMusicService.getMediaPlayer().getCurrentPosition()));
            if (sMusicService.isPlaying()) {
                runnable = () -> playCycle();;
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
        sMusicService.sendNotification(getContext(), song, position);
    }

    // them bai hat vao danh sach yeu thich
    private void favMusic(Song favSong) {
        mFavoritesOperations.addSongFav(favSong);
    }
    private void updateUI(int position) {
        Log.d("MinhMX", "updateUI: " + position);
        MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
        mediaPlayFragment.setArguments(getBundle(mSongsList.get(position)));
        getActivity().getSupportFragmentManager()
                     .beginTransaction()
                     .replace(R.id.fragment, mediaPlayFragment)
                     .addToBackStack("fragment_media").commit();
    }

    private Bundle getBundle(Song currSong){
        Bundle bundle = new Bundle();
        bundle.putLong(Key.CONST_IMAGE, currSong.getImage());
        bundle.putInt(Key.CONST_LIKE, currSong.isLike());
        bundle.putString(Key.CONST_TITLE, currSong.getTitle());
        bundle.putString(Key.CONST_SUBTITLE, currSong.getSubTitle());
        bundle.putString(Key.PATH_SONG, currSong.getPath());
        bundle.putInt(Key.KEY_POSITION, mCurrentPosition);
        bundle.putBoolean("Shuffle", mIsCheckShuffle);
        return bundle;
    }

    private void likeMuisc (int like){
        switch (like) {
            case Key.NO_LIKE:{
                mLikeFlag = false;
                mDislikeFlag = false;
                mBtnLike.setImageResource(R.drawable.ic_like);
                mBtnDisLike.setImageResource(R.drawable.ic_dislike);
                break;
            }
            case Key.LIKE:{
                mLikeFlag = true;
                mDislikeFlag = false;
                mBtnLike.setImageResource(R.drawable.ic_like_black);
                mBtnDisLike.setImageResource(R.drawable.ic_dislike);
                break;
            }
            case Key.DISLIKE: {
                mDislikeFlag = true;
                mLikeFlag = false;
                mBtnLike.setImageResource(R.drawable.ic_like);
                mBtnDisLike.setImageResource(R.drawable.ic_dislike_black);
                break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}

