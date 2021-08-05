package com.example.music.fragment;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.music.database.AllSongOperations;
import com.example.music.interfaces.ICallBack;
import com.example.music.interfaces.ICreateDataParseFav;
import com.example.music.adapter.SongAdapter;
import com.example.music.database.FavoritesOperations;
import com.example.music.Key;
import com.example.music.model.Song;
import com.example.music.R;
import com.example.music.service.MusicService;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class FavSongFragment extends ListFragment implements ICallBack {

    private FavoritesOperations mFavoritesOperations;
    private AllSongOperations mAllSongOperations;


    private ArrayList<Song> mFavSong;
    private ArrayList<Song> mSearchList;
    private ArrayList<Song> mSongList;


    private RecyclerView mRecyclerView;

    private ICreateDataParseFav mCreateDataParsed;
    boolean mFinalSearchList;
    private SongAdapter mFavAdapter;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(Key.KEY_POSITION, position);
        FavSongFragment tabFragment = new FavSongFragment();
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCreateDataParsed = (ICreateDataParseFav) context;
        mFavoritesOperations = new FavoritesOperations(context);
        mAllSongOperations = new AllSongOperations(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fav, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mRecyclerView = view.findViewById(R.id.list_playlist);
        setContent();
    }

    public void setContent() {
        boolean searchedList;
        mFavSong = new ArrayList<>();
        mSearchList = new ArrayList<>();
        mFavSong = mFavoritesOperations.getAllFavorites();
        mFavAdapter = new SongAdapter(getContext(), mFavSong, this);
        if (!mCreateDataParsed.queryText().equals("")) {
            mFavAdapter = onQueryTextChange();
            mFavAdapter.notifyDataSetChanged();
            searchedList = true;
        } else {
            searchedList = false;
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        new ItemTouchHelper(mSimpleCallback).attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(mFavAdapter);

        mFinalSearchList = searchedList;
    }


    @Override
    public void onClickItem(int position) {
        if (!mFinalSearchList) {
            setCountPlay(mFavSong.get(position).getTitle());
        } else {
            setCountPlay(mSearchList.get(position).getTitle());
        }
    }

    @Override
    public void onLongClickItem(int position) {
        showDialog(mFavSong.get(position).getTitle(), position);
    }

    public SongAdapter onQueryTextChange() {
        String text = mCreateDataParsed.queryText();
        for (Song songs : mFavSong) {
            String title = songs.getTitle().toLowerCase();
            if (title.contains(text)) {
                mSearchList.add(songs);
            }
        }
        return new SongAdapter(getContext(), mSearchList, this);
    }

    // hien dialog hoi co muon xoa bai hat nay ko
    private void showDialog(final String index, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.delete_text))
                .setCancelable(true)
                .setNegativeButton(R.string.no, (dialog, which) -> {
                })
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    mFavoritesOperations.removeSong(index);
                    mCreateDataParsed.fullSongList(mFavSong, position);
                    removeLike(index);
                    setContent();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // them su kien vuot sang trai de xoa
    ItemTouchHelper.SimpleCallback mSimpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull  RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            removeLike(mFavSong.get(viewHolder.getAdapterPosition()).getTitle());
            mFavoritesOperations.removeSong(mFavSong.get(viewHolder.getAdapterPosition()).getTitle());
            mFavSong.remove(viewHolder.getAdapterPosition());
            mFavAdapter.notifyDataSetChanged();
        }
    };


    // tang luot choi
    private void setCountPlay(String title) {
        for (int i = 0; i<mFavSong.size(); i ++){
            if (mFavSong.get(i).getTitle().equals(title)) {
                Song songsList = mFavSong.get(i);
                songsList.setCountOfPlay(songsList.getCountOfPlay() + 1);
                mAllSongOperations.updateSong(songsList);
                mCreateDataParsed.onDataPass(mFavSong.get(i) );
                MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                mediaPlayFragment.setArguments(getBundle(songsList));
                if (mCreateDataParsed.checkScreen()) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_media, mediaPlayFragment)
                            .addToBackStack("fragment")
                            .commit();
                } else {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment, mediaPlayFragment)
                            .addToBackStack("fragment")
                            .commit();
                }

                getIntentService(i);
                mCreateDataParsed.fullSongList(mFavSong, i);
                break;
            }
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

    // xoa khoi bai hat yeu thich
    private void removeLike(String title) {
        mSongList = new ArrayList<>();
        mSongList = mAllSongOperations.getAllSong();
        for (Song songsList : mSongList) {
            if (songsList.getTitle().equals(title)) {
                songsList.setLike(0);
                songsList.setCountOfPlay(0);
                mAllSongOperations.updateSong(songsList);
                break;
            }
        }
    }
}
