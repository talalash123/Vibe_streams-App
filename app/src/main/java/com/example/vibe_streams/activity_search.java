package com.example.vibe_streams;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibe_streams.models.Song;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class activity_search extends AppCompatActivity {

    private RecyclerView rvSongs;
    private EditText etSearch;
    private UserSongAdapterHorizontal adapter;
    private ArrayList<Song> allSongs = new ArrayList<>();
    private ArrayList<Song> filteredList = new ArrayList<>();
    private FrameLayout miniPlayerContainer;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        setupSearch();
        setupBottomNav();
        fetchSongsFromFirebase();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MusicPlayerManager.getInstance().getCurrentSong() != null) {
            showOrUpdateMiniPlayer();
        } else {
            if (miniPlayerContainer != null) {
                miniPlayerContainer.setVisibility(View.GONE);
            }
        }
    }

    private void initializeViews() {
        rvSongs = findViewById(R.id.rvSongs);
        etSearch = findViewById(R.id.Search);
        miniPlayerContainer = findViewById(R.id.miniPlayerContainer);
    }

    private void setupRecyclerView() {
        rvSongs.setLayoutManager(new GridLayoutManager(this, 2));
        // FIX: Use the simple constructor for default playback behavior
        adapter = new UserSongAdapterHorizontal(this, filteredList);
        rvSongs.setAdapter(adapter);
    }

    private void fetchSongsFromFirebase() {
        db.collection("songs").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allSongs.clear();
                allSongs.addAll(task.getResult().toObjects(Song.class));
                filterSongs(""); // Initially display all songs
            } else {
                Toast.makeText(this, "Error fetching songs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
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
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(allSongs);
        } else {
            for (Song song : allSongs) {
                if (song.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        song.getArtist().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(song);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showOrUpdateMiniPlayer() {
        if (MusicPlayerManager.getInstance().getCurrentSong() == null) {
            if (miniPlayerContainer != null) miniPlayerContainer.setVisibility(View.GONE);
            return;
        }

        if (miniPlayerContainer != null) miniPlayerContainer.setVisibility(View.VISIBLE);

        FragmentManager fragmentManager = getSupportFragmentManager();
        MiniPlayerFragment miniPlayerFragment = (MiniPlayerFragment) fragmentManager.findFragmentByTag("MINI_PLAYER_FRAGMENT");

        if (miniPlayerFragment == null) {
            miniPlayerFragment = new MiniPlayerFragment();
            fragmentManager.beginTransaction()
                    .replace(R.id.miniPlayerContainer, miniPlayerFragment, "MINI_PLAYER_FRAGMENT")
                    .commit();
        } else {
            miniPlayerFragment.updateMiniPlayerUI();
        }
    }

    private void setupBottomNav() {
        View bottomNav = findViewById(R.id.bottomNav);
        bottomNav.findViewById(R.id.btnHome).setOnClickListener(v -> startActivity(new Intent(this, activity_user_home.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
        bottomNav.findViewById(R.id.btnSearch).setOnClickListener(v -> { /* Already here */ });
        bottomNav.findViewById(R.id.btnLibrary).setOnClickListener(v -> startActivity(new Intent(this, activity_library.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
        bottomNav.findViewById(R.id.btnCreate).setOnClickListener(v -> startActivity(new Intent(this, activity_create_playlist.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
    }
}
