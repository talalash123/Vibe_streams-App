package com.example.vibe_streams;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vibe_streams.models.Song;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AddSongActivity extends AppCompatActivity {

    private RecyclerView rvSongsToAdd;
    private EditText etSearchSongs;
    private SongAdapter songAdapter;
    private ArrayList<Song> allSongs = new ArrayList<>();
    private ArrayList<Song> filteredSongs = new ArrayList<>();
    private FirebaseFirestore db;

    public interface OnSongClickListener {
        void onSongClick(Song song);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_song);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeViews();
        setupRecyclerView();
        setupSearch();
        fetchSongsFromFirebase();
    }

    private void initializeViews() {
        rvSongsToAdd = findViewById(R.id.rvSongsToAdd);
        etSearchSongs = findViewById(R.id.etSearchSongs);
    }

    private void setupRecyclerView() {
        // Use a GridLayoutManager for a two-column grid
        rvSongsToAdd.setLayoutManager(new GridLayoutManager(this, 2));
        songAdapter = new SongAdapter(this, filteredSongs, song -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selected_song", song);
            setResult(RESULT_OK, resultIntent);
            finish();
        });
        rvSongsToAdd.setAdapter(songAdapter);
    }

    private void fetchSongsFromFirebase() {
        db.collection("songs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allSongs.clear();
                allSongs.addAll(task.getResult().toObjects(Song.class));
                filterSongs("");
            } else {
                Toast.makeText(this, "Error fetching songs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        etSearchSongs.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSongs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void filterSongs(String query) {
        filteredSongs.clear();
        if (query.isEmpty()) {
            filteredSongs.addAll(allSongs);
        } else {
            for (Song song : allSongs) {
                if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    song.getArtist().toLowerCase().contains(query.toLowerCase())) {
                    filteredSongs.add(song);
                }
            }
        }
        songAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

        private final Context context;
        private final ArrayList<Song> songs;
        private final OnSongClickListener listener;

        public SongAdapter(Context context, ArrayList<Song> songs, OnSongClickListener listener) {
            this.context = context;
            this.songs = songs;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_song_horizontal, parent, false);
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
            ImageView ivSongCover;
            TextView tvSongTitle, tvSongArtist;

            ViewHolder(View itemView) {
                super(itemView);
                ivSongCover = itemView.findViewById(R.id.ivSongCover);
                tvSongTitle = itemView.findViewById(R.id.tvSongTitle);
                tvSongArtist = itemView.findViewById(R.id.tvSongArtist);
            }

            void bind(final Song song, final OnSongClickListener listener) {
                tvSongTitle.setText(song.getTitle());
                tvSongArtist.setText(song.getArtist());

                Glide.with(context)
                        .load(song.getCoverUrl())
                        .placeholder(R.drawable.music_placeholder)
                        .into(ivSongCover);

                itemView.setOnClickListener(v -> listener.onSongClick(song));
            }
        }
    }
}
