package com.example.music.fragment;


import android.content.Context;
import android.os.Bundle;
;

import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.interfaces.ICallBack;
import com.example.music.activity.MainActivity;
import com.example.music.Key;
import com.example.music.model.SongsList;
import com.example.music.R;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class MediaPlaybackFragment extends ListFragment implements ICallBack {

//    public static final String KEY_POSITION = "position";

    public ArrayList<SongsList> songsList = new ArrayList<>();

    private RecyclerView mRecyclerView;

    private RelativeLayout mMusicDetail;

    private ImageView mImageMusic;
    private ImageView mSmallImageMusic;
    private ImageView mLibaryMusic;
    private TextView mTitle;
    private TextView mSubTitle;


    private boolean checkSurface = false;

//    private boolean likeFag = false;

    private createDataParsed createDataParsed;

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(Key.KEY_POSITION, position);
        MediaPlaybackFragment tabFragment = new MediaPlaybackFragment();
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Display display = ((WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        if (display.getRotation() == Surface.ROTATION_90 || display.getRotation() == Surface.ROTATION_270) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().show();
            mMusicDetail.setVisibility(View.GONE);
        } else {
            mMusicDetail.setVisibility(View.VISIBLE);
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        createDataParsed = (createDataParsed) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_media_play_back, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mImageMusic = view.findViewById(R.id.image_music);
        mSmallImageMusic = view.findViewById(R.id.iv_music_list);
        mLibaryMusic = view.findViewById(R.id.queue_music);
        mTitle = view.findViewById(R.id.tv_music_name);
        mSubTitle = view.findViewById(R.id.tv_music_subtitle);
        mMusicDetail = view.findViewById(R.id.music_detail);

        Bundle bundle = this.getArguments();
        assert bundle != null;
        mImageMusic.setImageResource(bundle.getInt(Key.CONST_IMAGE));;
        mSmallImageMusic.setImageResource(bundle.getInt(Key.CONST_IMAGE));

        mTitle.setText(bundle.getString(Key.CONST_TITLE));
        mSubTitle.setText(bundle.getString(Key.CONST_SUBTITLE));

        int like = bundle.getInt(Key.CONST_LIKE);

        mLibaryMusic.setOnClickListener(v -> {
            MainActivity.mLinearLayout.setVisibility(View.GONE);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, new AllSongFragment())
                    .commit();
        });

        createDataParsed.isLike(like);
    }

    @Override
    public void onStop() {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onClickItem(int position) {

    }

    @Override
    public void onLongClickItem(int position) {

    }

    public interface createDataParsed {
        void isLike(int like);
    }


}
