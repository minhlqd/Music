package com.example.music.fragment;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.music.interfaces.ICallBack;
import com.example.music.interfaces.ICreateDataParseSong;
import com.example.music.interfaces.IPlayImage;
import com.example.music.activity.MainActivity;
import com.example.music.adapter.SongAdapter;
import com.example.music.database.AllSongOperations;
import com.example.music.Key;
import com.example.music.model.SongsList;
import com.example.music.R;
import com.example.music.service.MediaService;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class AllSongFragment extends ListFragment implements ICallBack {

    public static final String TAG = "all song";

    public ArrayList<SongsList> mSongsList;
    public ArrayList<SongsList> mSearchList;

    private RecyclerView mRecyclerView;

    private ICreateDataParseSong mCreateDataParse;
    MainActivity mainActivity;

    boolean mFinalSearchList;

    private LinearLayout mLinearPlaySong;

    private ImageView mPlayAllSong;
    private ImageView mMusicImage;
    private ImageView mPlayPause;
    private TextView mTitle;
    private TextView mSubTitle;

    private IPlayImage mPlayImage;
    private AllSongOperations mAllSongOperations;
    
    private boolean mCheckPlay;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCreateDataParse = (ICreateDataParseSong) context;
        mAllSongOperations = new AllSongOperations(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = view.findViewById(R.id.recycler_all_song);
        mLinearPlaySong = view.findViewById(R.id.linear_play_sheet_all);

        mMusicImage = view.findViewById(R.id.iv_music_list);
        mTitle = view.findViewById(R.id.tv_music_name);
        mSubTitle = view.findViewById(R.id.tv_music_subtitle);
        mPlayPause = view.findViewById(R.id.play_pause_song);
        setContent();
    }

    private void setContent() {
        boolean searchedList;
        mSearchList = new ArrayList<>();
        mSongsList = new ArrayList<>();

        mSongsList = mAllSongOperations.getAllSong();

        SongAdapter adapter = new SongAdapter(getContext(), mSongsList, this);
        if (!mCreateDataParse.queryText().equals("")) {
            adapter = onQueryTextChange();
            adapter.notifyDataSetChanged();
            searchedList = true;
        } else {
            searchedList = false;
        }
        mCreateDataParse.getLength(mSongsList.size());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);

        int positionSong = mCreateDataParse.getPositionSong();

        getPlaySong(positionSong);

        if (mCreateDataParse.isSong()) {
            mPlayPause.setImageResource(R.drawable.ic_pause_black);
        } else {
            mPlayPause.setImageResource(R.drawable.ic_play_black);
        }

        mCheckPlay = mCreateDataParse.isSong();

        mPlayPause.setOnClickListener(v -> {
            if (mCheckPlay) {
                mPlayPause.setImageResource(R.drawable.ic_play_black);
                MainActivity.mediaPlayer.pause();
                mCheckPlay = false;
            } else {
                mPlayPause.setImageResource(R.drawable.ic_pause_black);
                MainActivity.mediaPlayer.start();
                MainActivity.playCycle();
                mCheckPlay = true;
            }
            getIntentService(mSongsList.get(positionSong));
        });

        mLinearPlaySong.setOnClickListener(v -> {
            mCreateDataParse.onDataPassSong(mSongsList.get(positionSong).getTitle(), mSongsList.get(positionSong).getPath(), false);
            MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
            mediaPlayFragment.setArguments(getBundle(mSongsList.get(positionSong)));
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mediaPlayFragment).addToBackStack("fragment").commit();

            mCreateDataParse.playCheckSong(mCheckPlay);
            mCreateDataParse.fullSongList(mSongsList, positionSong);

        });
        mFinalSearchList = searchedList;
    }

    private void getPlaySong(int positionSong) {
        mMusicImage.setImageResource(mSongsList.get(positionSong).getImage());
        mTitle.setText(mSongsList.get(positionSong).getTitle());
        mSubTitle.setText(mSongsList.get(positionSong).getSubTitle());
    }

    public SongAdapter onQueryTextChange() {
        String text = mCreateDataParse.queryText();
        for (SongsList songs : mSongsList) {
            String title = songs.getTitle().toLowerCase();
            String subTitle = songs.getSubTitle().toLowerCase();
            if (title.contains(text) || subTitle.contains(text)) {
                mSearchList.add(songs);
            }
        }
        return new SongAdapter(getContext(), mSearchList, this);

    }

    private void showDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.play_next))
                .setCancelable(true)
                .setNegativeButton(R.string.no, (dialog, which) -> {

                })
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    mCreateDataParse.currentSong(mSongsList.get(position));
                    setContent();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onClickItem(int position) {
        if (!mFinalSearchList) {
            mCreateDataParse.onDataPassSong(mSongsList.get(position).getTitle(), mSongsList.get(position).getPath(),true);

        } else {
            mCreateDataParse.onDataPassSong(mSearchList.get(position).getTitle(), mSearchList.get(position).getPath(),true);
        }
        MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
        mediaPlayFragment.setArguments(getBundle(mSongsList.get(position)));
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mediaPlayFragment).addToBackStack("fragment").commit();
        getIntentService(mSongsList.get(position));

        mCreateDataParse.fullSongList(mSongsList, position);
    }

    private Bundle getBundle(SongsList currSong) {
        Bundle bundleMedia = new Bundle();
        bundleMedia.putInt(Key.CONST_IMAGE, currSong.getImage());
        bundleMedia.putInt(Key.CONST_LIKE, currSong.isLike());
        bundleMedia.putString(Key.CONST_TITLE, currSong.getTitle());
        bundleMedia.putString(Key.CONST_SUBTITLE, currSong.getSubTitle());
        return bundleMedia;
    }

    private void getIntentService(SongsList currSong) {
        Intent intent = new Intent(getContext(), MediaService.class);
        intent.putExtras(getBundle(currSong));
        getActivity().startService(intent);
    }

    @Override
    public void onLongClickItem(int position) {
        showDialog(position);
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
