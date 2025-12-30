package com.example.vibe_streams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vibe_streams.models.Song;

import java.util.ArrayList;

public class AdminSongAdapter extends RecyclerView.Adapter<AdminSongAdapter.ViewHolder> {

    public interface OnSongActionClickListener {
        void onEditClick(Song song);
        void onDeleteClick(Song song);
    }

    private final Context context;
    private final ArrayList<Song> songs;
    private final OnSongActionClickListener listener;

    public AdminSongAdapter(Context context, ArrayList<Song> songs, OnSongActionClickListener listener) {
        this.context = context;
        this.songs = songs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_song, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(songs.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCover;
        TextView tvTitle, tvArtist;
        Button btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            ivCover = itemView.findViewById(R.id.ivCover);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(final Song song, final OnSongActionClickListener listener) {
            tvTitle.setText(song.getTitle());
            tvArtist.setText(song.getArtist());
            Glide.with(context).load(song.getCoverUrl()).into(ivCover);
            btnEdit.setOnClickListener(v -> listener.onEditClick(song));
            btnDelete.setOnClickListener(v -> listener.onDeleteClick(song));
        }
    }
}
