package com.bkav.music.fragment;


import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
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
import androidx.fragment.app.ListFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bkav.music.interfaces.ICallBack;
import com.bkav.music.activity.MainActivity;
import com.bkav.music.Key;
import com.bkav.music.interfaces.ICreateDataParseMedia;
import com.bkav.music.model.SongsList;
import com.bkav.music.R;

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
    private TextView mPosition;

    private boolean checkSurface = false;
    private ICreateDataParseMedia createDataParsed;

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
        createDataParsed = (ICreateDataParseMedia) context;
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
        mPosition = view.findViewById(R.id.position);

        mSmallImageMusic.setVisibility(View.VISIBLE);
        mPosition.setVisibility(View.GONE);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            //mImageMusic.setImageResource(bundle.getInt(Key.CONST_IMAGE));
            //mSmallImageMusic.setImageResource(bundle.getInt(Key.CONST_IMAGE));

            mTitle.setText(bundle.getString(Key.CONST_TITLE));
            mSubTitle.setText(bundle.getString(Key.CONST_SUBTITLE));
            displayInto(getContext(), mImageMusic, bundle.getLong(Key.CONST_IMAGE));
            displayInto(getContext(), mSmallImageMusic, bundle.getLong(Key.CONST_IMAGE));

            int like = bundle.getInt(Key.CONST_LIKE);
            mLibaryMusic.setOnClickListener(v -> {
                MainActivity.mPlayerLayout.setVisibility(View.GONE);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new AllSongFragment())
                        .commit();
            });

            createDataParsed.isLike(like);
        }
    }

    private Uri queryAlbumUri(long id) {
        final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri,id);
    }

    private void displayInto(Context context, ImageView view, Long image){
        Glide.with(context)
                .load(queryAlbumUri(image))
                .placeholder(R.drawable.ic_music)
                .into(view);
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

}
