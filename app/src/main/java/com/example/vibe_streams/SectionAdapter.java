package com.example.vibe_streams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibe_streams.models.Song;

import java.util.ArrayList;

public class SectionAdapter extends RecyclerView.Adapter<SectionAdapter.ViewHolder> {

    public interface OnSongClickedListener {
        void onSongClicked();
    }

    private final Context context;
    private ArrayList<String> sectionTitles;
    private ArrayList<ArrayList<Song>> allSectionsRows;
    private final OnSongClickedListener songClickedListener;

    public SectionAdapter(Context context, ArrayList<String> sectionTitles, ArrayList<ArrayList<Song>> allSectionsRows, OnSongClickedListener listener) {
        this.context = context;
        this.sectionTitles = sectionTitles;
        this.allSectionsRows = allSectionsRows;
        this.songClickedListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_section, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvSectionTitle.setText(sectionTitles.get(position));

        ArrayList<Song> songsInSection = allSectionsRows.get(position);

        // FIX: Create a compatible listener to bridge the two adapters.
        UserSongAdapterHorizontal.OnSongInteractionListener interactionListener = (song, songPosition) -> {
            // First, set the global player manager with the correct playlist and starting song.
            MusicPlayerManager.getInstance().setPlaylist(songsInSection, songPosition, context);
            // Then, notify the activity that a song was clicked so it can show the mini-player.
            if (songClickedListener != null) {
                songClickedListener.onSongClicked();
            }
        };

        UserSongAdapterHorizontal horizontalAdapter = new UserSongAdapterHorizontal(context, songsInSection, interactionListener);

        holder.rvSongs.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        holder.rvSongs.setAdapter(horizontalAdapter);
        holder.rvSongs.setHasFixedSize(true);
    }

    @Override
    public int getItemCount() {
        return sectionTitles.size();
    }

    // Method to update the adapter's data
    public void updateData(ArrayList<String> newTitles, ArrayList<ArrayList<Song>> newRows) {
        this.sectionTitles = newTitles;
        this.allSectionsRows = newRows;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSectionTitle;
        RecyclerView rvSongs;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSectionTitle = itemView.findViewById(R.id.tvSectionTitle);
            rvSongs = itemView.findViewById(R.id.rvSongs);
        }
    }
}
