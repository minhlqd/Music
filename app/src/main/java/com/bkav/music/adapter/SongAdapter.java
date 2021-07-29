package com.bkav.music.adapter;

import android.content.Context;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bkav.music.database.AllSongOperations;
import com.bkav.music.database.FavoritesOperations;
import com.bkav.music.interfaces.ICallBack;
import com.bkav.music.R;
import com.bkav.music.model.SongsList;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<SongsList> mSongList;
    private ICallBack mOnClickItem;

    private boolean checkPlay;
    private AllSongOperations mAllSongOperations;
    private FavoritesOperations mFavoritesOperations;


    public SongAdapter(Context mContext, ArrayList<SongsList> mSongList, ICallBack mOnClickItem) {
        this.mContext = mContext;
        this.mSongList = mSongList;
        this.mOnClickItem = mOnClickItem;
        mFavoritesOperations = new FavoritesOperations(mContext);
        mAllSongOperations = new AllSongOperations(mContext);
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
        holder.tvSubTitle.setText(songsList.getDuration());
        holder.tvTitle.setText(songsList.getTitle());
        holder.queueMusic.setVisibility(View.GONE);
        if (songsList.getPlay() == 1) {
            holder.tvTitle.setTypeface(holder.tvTitle.getTypeface(), Typeface.BOLD);
            holder.tvPosition.setVisibility(View.GONE);
            holder.imageMusic.setVisibility(View.VISIBLE);
            holder.imageMusic.setImageResource(R.drawable.ic_play_all_song);
        } else {
            holder.tvTitle.setTypeface(holder.tvTitle.getTypeface(), Typeface.NORMAL);
            holder.tvPosition.setVisibility(View.VISIBLE);
            holder.imageMusic.setVisibility(View.GONE);
            holder.tvPosition.setText(String.valueOf(position + 1));
        }
        //holder.imageMusic.setImageResource(songsList.getImage());
    }



    @Override
    public int getItemCount() {
        return mSongList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener
            , View.OnLongClickListener
            , PopupMenu.OnMenuItemClickListener{
        TextView tvTitle;
        TextView tvSubTitle;
        TextView tvPosition;
        ICallBack mOnClick;
        ImageView queueMusic;
        ImageView imageMusic;
        ImageView menuPopup;

        public ViewHolder(View itemView, ICallBack callBack) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_music_name);
            tvSubTitle = itemView.findViewById(R.id.tv_music_subtitle);
            tvPosition = itemView.findViewById(R.id.position);
            queueMusic = itemView.findViewById(R.id.queue_music);
            imageMusic = itemView.findViewById(R.id.iv_music_list);

            menuPopup = itemView.findViewById(R.id.more_vert);
            this.mOnClick = callBack;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            menuPopup.setOnClickListener(v -> {
                showPopupMenu(v);
            });
        }

        private void showPopupMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.menu_popup);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
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

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.favorite:{
                    mSongList.get(getAdapterPosition()).setLike(1);
                    mAllSongOperations.updateSong(mSongList.get(getAdapterPosition()));
                    if (! mFavoritesOperations.checkFavorites( mSongList.get(getAdapterPosition()).getTitle() ) ) {
                        mFavoritesOperations.addSongFav(mSongList.get(getAdapterPosition()));
                    }
                    return true;
                }
                case R.id.delete:{
                    mAllSongOperations.removeAllSong(mSongList.get(getAdapterPosition()).getTitle());
                    mFavoritesOperations.removeSong(mSongList.get(getAdapterPosition()).getTitle());
                    mSongList.remove(getAdapterPosition());
                    notifyDataSetChanged();
                    return true;
                }
            }
            return false;
        }
    }
}