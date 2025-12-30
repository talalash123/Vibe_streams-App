package com.example.vibe_streams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Correctly import the official Song model
import com.example.vibe_streams.models.Song;

import java.util.ArrayList;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.RowViewHolder> {

    // This now uses the correct Song model
    private final ArrayList<ArrayList<Song>> rows;
    private final Context context;

    // The constructor now correctly accepts the official Song model.
    public RowAdapter(Context context, ArrayList<ArrayList<Song>> rows) {
        this.context = context;
        this.rows = rows;
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_row, parent, false);
        return new RowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        // This is now an ArrayList of Song objects.
        ArrayList<Song> songsInSection = rows.get(position);

        // The adapter call is now correct because the types match.
        UserSongAdapterHorizontal adapter = new UserSongAdapterHorizontal(context, songsInSection);

        holder.recyclerView.setLayoutManager(new LinearLayoutManager(
                holder.itemView.getContext(),
                LinearLayoutManager.HORIZONTAL,
                false
        ));
        holder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return rows != null ? rows.size() : 0;
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;

        RowViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.rvRow);
        }
    }
}
