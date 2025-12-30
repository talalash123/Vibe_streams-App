package com.example.vibe_streams;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vibe_streams.models.Song;

import java.util.ArrayList;

public class UserSongAdapterHorizontal extends RecyclerView.Adapter<UserSongAdapterHorizontal.ViewHolder> {

    public interface OnSongInteractionListener {
        void onSongClicked(Song song, int position);
    }

    private final Context context;
    private final ArrayList<Song> songs;
    private final OnSongInteractionListener interactionListener;

    // Main constructor for activities that need complex interaction (like playlist detail)
    public UserSongAdapterHorizontal(Context context, ArrayList<Song> songs, @Nullable OnSongInteractionListener listener) {
        this.context = context;
        this.songs = songs;
        this.interactionListener = listener;
    }

    // Convenience constructor for simple playback
    public UserSongAdapterHorizontal(Context context, ArrayList<Song> songs) {
        this(context, songs, null);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Song currentSong = songs.get(position);

        holder.tvTitle.setText(currentSong.getTitle());
        holder.tvArtist.setText(currentSong.getArtist());

        Glide.with(context)
                .load(currentSong.getCoverUrl())
                .placeholder(R.drawable.music_placeholder)
                .into(holder.ivCover);

        holder.itemView.setOnClickListener(v -> {
            if (interactionListener != null) {
                // If a listener is provided, let the Activity decide what to do.
                interactionListener.onSongClicked(currentSong, holder.getAdapterPosition());
            } else {
                // Default behavior: If no listener, just play the song.
                MusicPlayerManager.getInstance().setPlaylist(songs, holder.getAdapterPosition(), context);
                
                // Optionally, you can also launch the full player screen.
                Intent intent = new Intent(context, activity_song_player.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return (songs != null) ? songs.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivSongCover);
            tvTitle = itemView.findViewById(R.id.tvSongTitle);
            tvArtist = itemView.findViewById(R.id.tvSongArtist);
        }
    }
}
