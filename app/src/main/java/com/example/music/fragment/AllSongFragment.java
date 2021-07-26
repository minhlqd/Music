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

    private static ContentResolver contentResolver1;

    public ArrayList<SongsList> songsList;
    public ArrayList<SongsList> newList;
    public ArrayList<SongsList> createSongsList = new ArrayList<>();

    private RecyclerView mRecyclerView;

    private ICreateDataParseSong createDataParse;
    private ContentResolver contentResolver;
    MainActivity mainActivity;

    boolean finalSearchedList;

    private LinearLayout mLinearPlaySong;

    private ImageView mPlayAllSong;
    private ImageView mMusicImage;
    private ImageView mPlayPause;
    private TextView mTitle;
    private TextView mSubTitle;

    private IPlayImage mPlayImage;

    private AllSongOperations mAllSongOperations;

    private boolean backFlag;
    private boolean checkPlayPause;

    public static Fragment getInstance(int position, ContentResolver mcontentResolver) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        AllSongFragment tabFragment = new AllSongFragment();
        tabFragment.setArguments(bundle);
        contentResolver1 = mcontentResolver;
        return tabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backFlag = true;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        createDataParse = (ICreateDataParseSong) context;
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

        contentResolver = contentResolver1;
        setContent();
    }

    private void setContent() {
        boolean searchedList;
        newList = new ArrayList<>();
        songsList = new ArrayList<>();

        songsList = mAllSongOperations.getAllSong();

        SongAdapter adapter = new SongAdapter(getContext(), songsList, this);
        if (!createDataParse.queryText().equals("")) {
            adapter = onQueryTextChange();
            adapter.notifyDataSetChanged();
            searchedList = true;
        } else {
            searchedList = false;
        }
        createDataParse.getLength(songsList.size());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(adapter);

        int positionSong = createDataParse.getPositionSong();

        getPlaySong(positionSong);

        if (createDataParse.isSong()) {
            mPlayPause.setImageResource(R.drawable.ic_pause_black);
//            mPlayImage.isPlay(true);
        } else {
            mPlayPause.setImageResource(R.drawable.ic_play_black);
//            mPlayImage.isPlay(false);
        }

        checkPlayPause = createDataParse.isSong();

        mPlayPause.setOnClickListener(v -> {
            if (checkPlayPause) {
                mPlayPause.setImageResource(R.drawable.ic_play_black);
                MainActivity.mediaPlayer.pause();
//                mPlayImage.isPlay(false);
                checkPlayPause = false;
            } else {
                mPlayPause.setImageResource(R.drawable.ic_pause_black);
                MainActivity.mediaPlayer.start();
//                mPlayImage.isPlay(true);
                MainActivity.playCycle();
                checkPlayPause = true;
            }
            getIntentService(songsList.get(positionSong));
        });

        mLinearPlaySong.setOnClickListener(v -> {
            createDataParse.onDataPassSong(songsList.get(positionSong).getTitle(), songsList.get(positionSong).getPath(), false);
            MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
            mediaPlayFragment.setArguments(getBundle(songsList.get(positionSong)));
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mediaPlayFragment).addToBackStack("fragment").commit();

            createDataParse.playCheckSong(checkPlayPause);
            createDataParse.fullSongList(songsList, positionSong);

        });

        finalSearchedList = searchedList;
    }

    private void getPlaySong(int positionSong) {
        mMusicImage.setImageResource(songsList.get(positionSong).getImage());
        mTitle.setText(songsList.get(positionSong).getTitle());
        mSubTitle.setText(songsList.get(positionSong).getSubTitle());
    }


    private void getMusic() {
    }

    public SongAdapter onQueryTextChange() {
        String text = createDataParse.queryText();
        for (SongsList songs : songsList) {
            String title = songs.getTitle().toLowerCase();
            String subTitle = songs.getSubTitle().toLowerCase();
            if (title.contains(text) || subTitle.contains(text)) {
                newList.add(songs);
            }
        }
        return new SongAdapter(getContext(), newList, this);

    }

    private void showDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getString(R.string.play_next))
                .setCancelable(true)
                .setNegativeButton(R.string.no, (dialog, which) -> {

                })
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    createDataParse.currentSong(songsList.get(position));
                    setContent();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onClickItem(int position) {
        if (!finalSearchedList) {
            createDataParse.onDataPassSong(songsList.get(position).getTitle(), songsList.get(position).getPath(),true);

        } else {
            createDataParse.onDataPassSong(newList.get(position).getTitle(), newList.get(position).getPath(),true);
        }
        MediaPlaybackFragment mediaPlayFragment = new MediaPlaybackFragment();
        mediaPlayFragment.setArguments(getBundle(songsList.get(position)));
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.fragment, mediaPlayFragment).addToBackStack("fragment").commit();

        getIntentService(songsList.get(position));

        createDataParse.fullSongList(songsList, position);
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

}
