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

    private FavoritesOperations favoritesOperations;


    public ArrayList<SongsList> songsList;
    public ArrayList<SongsList> newList;

    private RecyclerView mRecyclerView;

    private ICreateDataParseFav createDataParsed;
    boolean finalSearchedList;

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
        createDataParsed = (ICreateDataParseFav) context;
        favoritesOperations = new FavoritesOperations(context);
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
        songsList = new ArrayList<>();
        newList = new ArrayList<>();
        songsList = favoritesOperations.getAllFavorites();
        Log.d("aaa", "setContent: " + songsList.size());
        SongAdapter adapter = new SongAdapter(getContext(), songsList, this);
        if (!createDataParsed.queryText().equals("")) {
            adapter = onQueryTextChange();
            adapter.notifyDataSetChanged();
            searchedList = true;
        } else {
            searchedList = false;
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);

        finalSearchedList = searchedList;
    }


    @Override
    public void onClickItem(int position) {
        if (!finalSearchedList) {
            createDataParsed.onDataPass(songsList.get(position).getTitle(), songsList.get(position).getPath());
        } else {
            createDataParsed.onDataPass(newList.get(position).getTitle(), newList.get(position).getPath());
        }
        Bundle bundleMedia = new Bundle();
        bundleMedia.putInt("image", songsList.get(position).getImage());
        bundleMedia.putInt("like", songsList.get(position).isLike());
        MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
        mediaPlayFragment.setArguments(bundleMedia);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mediaPlayFragment).addToBackStack("fragment").commit();

        createDataParsed.fullSongList(songsList, position);
    }

    @Override
    public void onLongClickItem(int position) {
        showDialog(songsList.get(position).getTitle(), position);
    }

    public SongAdapter onQueryTextChange() {
        String text = createDataParsed.queryText();
        for (SongsList songs : songsList) {
            String title = songs.getTitle().toLowerCase();
            if (title.contains(text)) {
                newList.add(songs);
            }
        }
        return new SongAdapter(getContext(), newList, this);
    }

    private void showDialog(final String index, final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.delete))
                .setMessage(getString(R.string.delete_text))
                .setCancelable(true)
                .setNegativeButton(R.string.no, (dialog, which) -> {
                })
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    favoritesOperations.removeSong(index);
                    createDataParsed.fullSongList(songsList, position);
                    setContent();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
