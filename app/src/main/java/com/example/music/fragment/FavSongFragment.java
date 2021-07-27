package com.example.music.fragment;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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


import com.example.music.interfaces.ICallBack;
import com.example.music.interfaces.ICreateDataParseFav;
import com.example.music.adapter.SongAdapter;
import com.example.music.database.FavoritesOperations;
import com.example.music.Key;
import com.example.music.model.SongsList;
import com.example.music.R;
import java.util.ArrayList;

@SuppressWarnings("ALL")
public class FavSongFragment extends ListFragment implements ICallBack {

    private FavoritesOperations mFavoritesOperations;


    public ArrayList<SongsList> mFavSong;
    public ArrayList<SongsList> mSearchList;

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
        Log.d("aaa", "setContent: " + mFavSong.size());
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
            mCreateDataParsed.onDataPass(mFavSong.get(position).getTitle(), mFavSong.get(position).getPath());
        } else {
            mCreateDataParsed.onDataPass(mSearchList.get(position).getTitle(), mSearchList.get(position).getPath());
        }
        Bundle bundleMedia = new Bundle();
        bundleMedia.putInt("image", mFavSong.get(position).getImage());
        bundleMedia.putInt("like", mFavSong.get(position).isLike());
        MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
        mediaPlayFragment.setArguments(bundleMedia);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mediaPlayFragment).addToBackStack("fragment").commit();

        mCreateDataParsed.fullSongList(mFavSong, position);
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
            mFavoritesOperations.removeSong(mFavSong.get(viewHolder.getAdapterPosition()).getTitle());
            mFavSong.remove(viewHolder.getAdapterPosition());
            mFavAdapter.notifyDataSetChanged();
        }
    };

}
