package com.example.music.adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music.interfaces.ICallBack;
import com.example.music.R;
import com.example.music.model.SongsList;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder>{

    private Context mContext;
    private ArrayList<SongsList> mSongList;
    private ICallBack mOnClickItem;

    private boolean checkPlay;


    public SongAdapter(Context mContext, ArrayList<SongsList> mSongList, ICallBack mOnClickItem) {
        this.mContext = mContext;
        this.mSongList = mSongList;
        this.mOnClickItem = mOnClickItem;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.playlist_items, parent, false);
        return new ViewHolder(view, mOnClickItem);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        SongsList songsList = mSongList.get(position);
        holder.tvSubTitle.setText(songsList.getSubTitle());
        holder.tvTitle.setText(songsList.getTitle());
        holder.queueMusic.setVisibility(View.GONE);
        holder.imageMusic.setImageResource(songsList.getImage());
//        if (checkPlay) {
//            holder.imageMusic.setImageResource(R.drawable.ic_play_all_song);
//        }
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

//    @Override
//    public void isPlay(boolean is_play) {
//        checkPlay = is_play;
//    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView tvTitle;
        TextView tvSubTitle;
        ICallBack mOnClick;
        ImageView queueMusic;
        ImageView imageMusic;
        public ViewHolder(View itemView, ICallBack callBack) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_music_name);
            tvSubTitle = itemView.findViewById(R.id.tv_music_subtitle);
            queueMusic = itemView.findViewById(R.id.queue_music);
            imageMusic = itemView.findViewById(R.id.iv_music_list);

            this.mOnClick = callBack;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mOnClick.onClickItem(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            mOnClick.onLongClickItem(getAdapterPosition());
            return true;
        }

    }
}
