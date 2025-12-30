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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibe_streams.models.Playlist;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class activity_library extends AppCompatActivity {

    private RecyclerView rvPlaylists;
    private PlaylistAdapter playlistAdapter;
    private ArrayList<Playlist> displayedPlaylistList = new ArrayList<>(); // The list shown in the UI
    private ArrayList<Playlist> allPlaylistsMaster = new ArrayList<>(); // The full, unfiltered list
    private FrameLayout miniPlayerContainer;
    private EditText etSearchPlaylists;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        setupSearch();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlaylistsFromFirestore();
        if (MusicPlayerManager.getInstance().getCurrentSong() != null) {
            showOrUpdateMiniPlayer();
        } else {
            if (miniPlayerContainer != null) {
                miniPlayerContainer.setVisibility(View.GONE);
            }
        }
    }

    private void initializeViews() {
        rvPlaylists = findViewById(R.id.rvPlaylists);
        miniPlayerContainer = findViewById(R.id.miniPlayerContainer);
        etSearchPlaylists = findViewById(R.id.etSearchPlaylists);
    }

    private void setupRecyclerView() {
        rvPlaylists.setLayoutManager(new LinearLayoutManager(this));
        playlistAdapter = new PlaylistAdapter(displayedPlaylistList, this::onPlaylistClicked);
        rvPlaylists.setAdapter(playlistAdapter);
    }

    private void loadPlaylistsFromFirestore() {
        db.collection("playlists").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                allPlaylistsMaster.clear();
                allPlaylistsMaster.addAll(task.getResult().toObjects(Playlist.class));
                // After loading, apply the current filter to show the initial list
                filterPlaylists(etSearchPlaylists.getText().toString());
            } else {
                Toast.makeText(this, "Error loading playlists", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onPlaylistClicked(Playlist playlist) {
        Intent intent = new Intent(activity_library.this, activity_playlist_detail.class);
        intent.putExtra("playlist_id", playlist.getId());
        startActivity(intent);
    }

    private void setupSearch() {
        etSearchPlaylists.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPlaylists(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void filterPlaylists(String query) {
        displayedPlaylistList.clear();
        if (query.isEmpty()) {
            displayedPlaylistList.addAll(allPlaylistsMaster);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Playlist playlist : allPlaylistsMaster) {
                if (playlist.getName().toLowerCase().contains(lowerCaseQuery)) {
                    displayedPlaylistList.add(playlist);
                }
            }
        }
        if (playlistAdapter != null) {
            playlistAdapter.notifyDataSetChanged();
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
        if (bottomNav != null) {
            bottomNav.findViewById(R.id.btnHome).setOnClickListener(v -> startActivity(new Intent(this, activity_user_home.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
            bottomNav.findViewById(R.id.btnSearch).setOnClickListener(v -> startActivity(new Intent(this, activity_search.class).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
            bottomNav.findViewById(R.id.btnLibrary).setOnClickListener(v -> { /* Already here */ });
            bottomNav.findViewById(R.id.btnCreate).setOnClickListener(v -> startActivity(new Intent(this, activity_create_playlist.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
        }
    }
}
