package com.example.music.fragment;


import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
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
import com.example.music.interfaces.ICreateDataParseMedia;
import com.example.music.model.Song;
import com.example.music.service.MusicService;

import java.util.ArrayList;

;

@SuppressWarnings("ALL")
public class MediaPlaybackFragment extends ListFragment
        implements PopupMenu.OnMenuItemClickListener {

//    public static final String KEY_POSITION = "position";

    public ArrayList<Song> songsList = new ArrayList<>();

    private RecyclerView mRecyclerView;

    private RelativeLayout mMusicDetail;

    private ImageView mImageMusic;
    private ImageView mSmallImageMusic;
    private ImageView mLibaryMusic;
    private ImageView mMenuPopup;
    private TextView mTitle;
    private TextView mSubTitle;
    private TextView mTvPosition;

    private MainActivity mainActivity;

    private boolean checkSurface = false;
    private ICreateDataParseMedia createDataParsed;

    private int mPosition;
    private MusicService musicService;

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
            // mMusicDetail.setVisibility(View.GONE);
        } else {
            mMusicDetail.setVisibility(View.VISIBLE);
            ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        createDataParsed = (ICreateDataParseMedia) context;
        mainActivity = (MainActivity) context;
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

        mSmallImageMusic.setVisibility(View.VISIBLE);
        mTvPosition.setVisibility(View.GONE);

        mMenuPopup.setOnClickListener(v -> {
            showMenuPopup(v);
        });
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mTitle.setText(bundle.getString(Key.CONST_TITLE));
            mSubTitle.setText(bundle.getString(Key.CONST_SUBTITLE));
            getImageAlbum(getContext(), mImageMusic, bundle.getLong(Key.CONST_IMAGE));
            getImageAlbum(getContext(), mSmallImageMusic, bundle.getLong(Key.CONST_IMAGE));

            int like = bundle.getInt(Key.CONST_LIKE);
            mLibaryMusic.setOnClickListener(v -> {
                mainActivity.playerLayout.setVisibility(View.GONE);
                mainActivity.playerSheetAll.setVisibility(View.VISIBLE);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new AllSongFragment())
                        .commit();
            });

            createDataParsed.isLike(like);
        }
    }

    // lay duong dan anh tu album
    private Uri queryAlbumUri(long id) {
        final Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        return ContentUris.withAppendedId(artworkUri,id);
    }

    // hien thi anh bai hat bang thu vien glide
    private void getImageAlbum(Context context, ImageView view, Long image){
        Glide.with(context)
                .load(queryAlbumUri(image))
                .placeholder(R.drawable.ic_music_player)
                .into(view);
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
}
