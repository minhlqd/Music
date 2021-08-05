package com.example.music.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.music.database.FavoritesOperations;
import com.example.music.interfaces.ICallBack;
import com.example.music.interfaces.ICreateDataParseSong;
import com.example.music.adapter.SongAdapter;
import com.example.music.database.AllSongOperations;
import com.example.music.Key;
import com.example.music.model.Song;
import com.example.music.R;
import com.example.music.service.MusicService;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class AllSongFragment extends ListFragment implements ICallBack {

    public static final String TAG = "all song";

    public ArrayList<Song> mSongsList;
    public ArrayList<Song> mSearchList;

    private RecyclerView mRecyclerView;

    private ICreateDataParseSong mCreateDataParse;

    boolean mFinalSearchList;


    private ImageView mPlayAllSong;
    private ImageView mMusicImage;
    private TextView mTitle;
    private TextView mSubTitle;

    private AllSongOperations mAllSongOperations;
    private FavoritesOperations mFavoritesOperations;
    private boolean mCheckPlay;

    private SongAdapter mSongAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCreateDataParse = (ICreateDataParseSong) context;
        mAllSongOperations = new AllSongOperations(context);
        mFavoritesOperations = new FavoritesOperations(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_all_songs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = view.findViewById(R.id.recycler_all_song);
        setContent();
    }

    // hien thich danh sach bai hat
    private void setContent() {
        boolean searchedList;
        mSearchList = new ArrayList<>();
        mSongsList = new ArrayList<>();

        mSongsList = mAllSongOperations.getAllSong();

        mSongAdapter = new SongAdapter(getContext(), mSongsList, this);
        if (!mCreateDataParse.queryText().equals("")) {
            mSongAdapter = onQueryTextChange();
            mSongAdapter.notifyDataSetChanged();
            searchedList = true;
        } else {
            searchedList = false;
        }
        mCreateDataParse.getLength(mSongsList.size());

        mFinalSearchList = searchedList;

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mSongAdapter);
    }

    // so sanh voi text tim kiem va tra ve danh sach tuong ung
    public SongAdapter onQueryTextChange() {
        String text = mCreateDataParse.queryText();
        for (Song songs : mSongsList) {
            String title = songs.getTitle().toLowerCase();
            String subTitle = songs.getSubTitle().toLowerCase();
            if (title.contains(text) || subTitle.contains(text)) {
                mSearchList.add(songs);
            }
        }
        return new SongAdapter(getContext(), mSearchList, this);

    }

    // hien thi dialog hoi xem co chon bai nay la bai hat tiep theo duoc phat
    private void showDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.play_next))
                .setCancelable(true)
                .setNegativeButton(R.string.no, (dialog, which) -> {
                })
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    mCreateDataParse.currentSong(mSongsList.get(position), position);
                    // setContent();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // sau khi bam se, gui du lieu cho acitivity de phat bai hat do
    @Override
    public void onClickItem(int position) {
        mAllSongOperations.updatePlaySong(mSongsList);
        if (!mFinalSearchList) {
            setCountPlay(mSongsList.get(position).getTitle());
        } else {
            setCountPlay(mSearchList.get(position).getTitle());
        }
    }

    private Bundle getBundle(Song currSong) {
        Bundle bundleMedia = new Bundle();
        bundleMedia.putLong(Key.CONST_IMAGE, currSong.getImage());
        bundleMedia.putInt(Key.CONST_LIKE, currSong.isLike());
        bundleMedia.putString(Key.CONST_TITLE, currSong.getTitle());
        bundleMedia.putString(Key.CONST_SUBTITLE, currSong.getSubTitle());
        return bundleMedia;
    }

    private void getIntentService(int postion) {
        Intent intent = new Intent(getContext(), MusicService.class);
        intent.putExtra(Key.KEY_POSITION,postion);
        getActivity().startService(intent);
    }

    // tang luot choi nhac va neu bang 3 thi se tu them vao danh sach yeu thich
    private void setCountPlay(String title) {
        for (int i = 0; i<mSongsList.size(); i ++){
            if (mSongsList.get(i).getTitle().equals(title)) {
                Song songsList = mSongsList.get(i);
                songsList.setCountOfPlay(songsList.getCountOfPlay() + 1);
                if (songsList.getCountOfPlay() == 3) {
                    songsList.setLike(1);
                    if (! mFavoritesOperations.checkFavorites(songsList.getTitle())) {
                        mFavoritesOperations.addSongFav(songsList);
                    }
                }
                songsList.setPlay(1);

                mAllSongOperations.updateSong(songsList);

                mCreateDataParse.onDataPassSong(mSongsList.get(i));

                MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                mediaPlayFragment.setArguments(getBundle(songsList));

                // lay du lieu dc gui tu activity va kiem tra xem dt dang xoay theo chieu ngang hay doc
                if (mCreateDataParse.checkScreen()) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_media, mediaPlayFragment)
                            .addToBackStack("fragment")
                            .commit();
                }

                mSongAdapter.notifyDataSetChanged();
                // mRecyclerView.setAdapter(mSongAdapter);
                getIntentService(i);
                mCreateDataParse.fullSongList(mSongsList, i);
                break;
            }
        }
    }

    // giu item se show dialog
    @Override
    public void onLongClickItem(int position) {
        showDialog(position);
    }


    @Override
    public void onStop() {
        super.onStop();
    }
}
