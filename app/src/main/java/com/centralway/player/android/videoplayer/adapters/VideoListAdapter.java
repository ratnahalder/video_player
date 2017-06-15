package com.centralway.player.android.videoplayer.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.centralway.player.android.videoplayer.R;
import com.centralway.player.android.videoplayer.utilities.VideoAlbum;

import java.io.File;
import java.util.List;

/**
 * VideoListAdapter is used in VideoListActivity to display list of video(name, image and no. of songs)
 * located in sd-card
 *
 * @author  Ratna Halder(ratnacse06@gmail.com).
 */
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.MyViewHolder> {

    private Context mContext;
    private List<VideoAlbum> albumList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title, count;
        public ImageView thumbnail, overflow;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            count = (TextView) view.findViewById(R.id.count);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            overflow = (ImageView) view.findViewById(R.id.overflow);
        }
    }


    public VideoListAdapter(Context mContext, List<VideoAlbum> albumList) {
        this.mContext = mContext;
        this.albumList = albumList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_card, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        VideoAlbum album = albumList.get(position);
        holder.title.setText(album.getName());
        //holder.count.setText(album.getNumberOfSongs() + " songs");

        // loading video cover using Glide library
        Glide.with(mContext).load( Uri.fromFile( new File( album.getImagePath() ) ) ).into(holder.thumbnail);

    }


    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public VideoAlbum getItem(int position){
        return albumList.get(position);
    }
}
