package com.bkav.music.fragment;


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


import com.bkav.music.database.AllSongOperations;
import com.bkav.music.interfaces.ICallBack;
import com.bkav.music.interfaces.ICreateDataParseFav;
import com.bkav.music.adapter.SongAdapter;
import com.bkav.music.database.FavoritesOperations;
import com.bkav.music.Key;
import com.bkav.music.model.SongsList;
import com.bkav.music.R;
import com.bkav.music.service.MediaService;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class FavSongFragment extends ListFragment implements ICallBack {

    private FavoritesOperations mFavoritesOperations;
    private AllSongOperations mAllSongOperations;


    private ArrayList<SongsList> mFavSong;
    private ArrayList<SongsList> mSearchList;
    private ArrayList<SongsList> mSongList;


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
        for (SongsList songs : mFavSong) {
            String title = songs.getTitle().toLowerCase();
            if (title.contains(text)) {
                mSearchList.add(songs);
            }
        }
        return new SongAdapter(getContext(), mSearchList, this);
    }

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

    private void setCountPlay(String title) {
        for (int i = 0; i<mFavSong.size(); i ++){
            if (mFavSong.get(i).getTitle().equals(title)) {
                SongsList songsList = mFavSong.get(i);
                songsList.setCountOfPlay(songsList.getCountOfPlay() + 1);
                if (songsList.getCountOfPlay() == 3) {
                    songsList.setLike(1);
                    mFavoritesOperations.addSongFav(songsList);
                }
                mAllSongOperations.updateSong(songsList);

                mCreateDataParsed.onDataPass(mFavSong.get(i).getTitle(), mFavSong.get(i).getPath());
                MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
                mediaPlayFragment.setArguments(getBundle(songsList));
                if (mCreateDataParsed.checkScreen()) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_media, mediaPlayFragment)
                            .addToBackStack("fragment_media")
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
    private Bundle getBundle(SongsList currSong) {
        Bundle bundleMedia = new Bundle();
        bundleMedia.putLong(Key.CONST_IMAGE, currSong.getImage());
        bundleMedia.putInt(Key.CONST_LIKE, currSong.isLike());
        bundleMedia.putString(Key.CONST_TITLE, currSong.getTitle());
        bundleMedia.putString(Key.CONST_SUBTITLE, currSong.getSubTitle());
        return bundleMedia;
    }

    private void getIntentService(int postion) {
        Intent intent = new Intent(getContext(), MediaService.class);
        intent.putExtra(Key.KEY_POSITION,postion);
        getActivity().startService(intent);
    }

    private void removeLike(String title) {
        mSongList = new ArrayList<>();
        mSongList = mAllSongOperations.getAllSong();
        for (SongsList songsList : mSongList) {
            if (songsList.getTitle().equals(title)) {
                songsList.setLike(0);
                songsList.setCountOfPlay(0);
                mAllSongOperations.updateSong(songsList);
                break;
            }
        }
    }
}
