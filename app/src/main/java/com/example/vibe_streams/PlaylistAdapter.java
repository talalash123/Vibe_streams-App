package com.example.vibe_streams;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vibe_streams.models.Playlist;

import java.util.ArrayList;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    public interface OnPlaylistClickListener {
        void onPlaylistClick(Playlist playlist);
    }

    private final ArrayList<Playlist> playlists;
    private final OnPlaylistClickListener listener;

    public PlaylistAdapter(ArrayList<Playlist> playlists, OnPlaylistClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist currentPlaylist = playlists.get(position);
        holder.bind(currentPlaylist, listener);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPlaylistCover;
        TextView tvPlaylistName;
        TextView tvSongCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlaylistCover = itemView.findViewById(R.id.ivPlaylistCover);
            tvPlaylistName = itemView.findViewById(R.id.tvPlaylistName);
            tvSongCount = itemView.findViewById(R.id.tvSongCount);
        }

        public void bind(final Playlist playlist, final OnPlaylistClickListener listener) {
            tvPlaylistName.setText(playlist.getName());
            String songCountText = playlist.getSongCount() + " songs";
            tvSongCount.setText(songCountText);

            // FIX: Use the getCoverUrl() method to load the image from Firebase
            String coverUrl = playlist.getCoverUrl();

            Glide.with(itemView.getContext())
                .load(coverUrl)
                .placeholder(R.drawable.music_placeholder)
                .error(R.drawable.music_placeholder)
                .into(ivPlaylistCover);

            itemView.setOnClickListener(v -> listener.onPlaylistClick(playlist));
        }
    }
}
