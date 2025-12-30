package com.example.vibe_streams;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibe_streams.models.Song;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class activity_user_home extends AppCompatActivity implements SectionAdapter.OnSongClickedListener {

    private RecyclerView rvSections;
    private DrawerLayout drawerLayout;
    private FrameLayout miniPlayerContainer;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private AiPlaylistService aiPlaylistService;

    private final ArrayList<String> sectionTitles = new ArrayList<>();
    private final ArrayList<ArrayList<Song>> allSectionsRows = new ArrayList<>();
    private SectionAdapter sectionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        aiPlaylistService = new AiPlaylistService(ContextCompat.getMainExecutor(this));

        initializeViews();
        setupRecyclerView();
        setupBottomNav();
        setupUserDrawer();
        fetchSongsAndSetupSections();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showOrUpdateMiniPlayer();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        rvSections = findViewById(R.id.rvSections);
        miniPlayerContainer = findViewById(R.id.miniPlayerContainer);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        sectionAdapter = new SectionAdapter(this, sectionTitles, allSectionsRows, this);
        rvSections.setLayoutManager(new LinearLayoutManager(this));
        rvSections.setHasFixedSize(true);
        rvSections.setAdapter(sectionAdapter);
    }

    private void fetchSongsAndSetupSections() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        db.collection("songs").get().addOnCompleteListener(task -> {
            if (progressBar != null) progressBar.setVisibility(View.GONE);

            if (task.isSuccessful() && task.getResult() != null) {
                ArrayList<Song> allSongs = new ArrayList<>(task.getResult().toObjects(Song.class));
                if (!allSongs.isEmpty()) {
                    setupLocalSections(allSongs);
                    generateAiRecommendations(allSongs);
                } else {
                    Toast.makeText(this, "No songs found.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error fetching songs.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupLocalSections(ArrayList<Song> allSongs) {
        sectionTitles.clear();
        allSectionsRows.clear();

        ArrayList<Song> availableSongs = new ArrayList<>(allSongs);

        if (availableSongs.size() >= 5) {
            ArrayList<Song> newReleaseSongs = new ArrayList<>(availableSongs.subList(availableSongs.size() - 5, availableSongs.size()));
            sectionTitles.add("New Release");
            allSectionsRows.add(newReleaseSongs);
            availableSongs.removeAll(newReleaseSongs);
        }

        Collections.shuffle(availableSongs);
        ArrayList<Song> trendingSongs = new ArrayList<>(availableSongs.subList(0, Math.min(5, availableSongs.size())));
        if (!trendingSongs.isEmpty()) {
            sectionTitles.add("Trending");
            allSectionsRows.add(trendingSongs);
        }

        sectionAdapter.notifyDataSetChanged();
    }

    private void generateAiRecommendations(ArrayList<Song> allSongs) {
        if (aiPlaylistService == null || allSongs.isEmpty()) {
            return; // Safety check
        }

        // New, simpler prompt with no artist restrictions
        String songListForPrompt = allSongs.stream().map(Song::getTitle).collect(Collectors.joining(", "));
        String prompt = "You are a music recommender. From the following list of songs, please select 8 that would make a great 'Recommended for You' playlist. " +
                      "Just return the song titles you picked, separated by commas. Do not add any other conversational text. " +
                      "Here is the list of available songs: " + songListForPrompt;

        aiPlaylistService.generatePlaylist(prompt, allSongs, new AiPlaylistService.AiPlaylistCallback() {
            @Override
            public void onPlaylistReady(ArrayList<Song> playlist) {
                if (!playlist.isEmpty()) {
                    sectionTitles.add("Recommended for You (AI)");
                    allSectionsRows.add(playlist);
                    sectionAdapter.notifyItemInserted(sectionTitles.size() - 1);
                } else {
                    Toast.makeText(activity_user_home.this, "AI could not generate recommendations at this time.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(activity_user_home.this, "AI Recommendation Failed: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onSongClicked() {
        showOrUpdateMiniPlayer();
    }

    private void showOrUpdateMiniPlayer() {
        if (MusicPlayerManager.getInstance().getCurrentSong() == null) {
            if (miniPlayerContainer != null) miniPlayerContainer.setVisibility(View.GONE);
            return;
        }
        if (miniPlayerContainer != null) miniPlayerContainer.setVisibility(View.VISIBLE);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag("MINI_PLAYER_FRAGMENT") == null) {
            fragmentManager.beginTransaction()
                    .replace(R.id.miniPlayerContainer, new MiniPlayerFragment(), "MINI_PLAYER_FRAGMENT")
                    .commit();
        }
    }

    private void setupBottomNav() {
        View bottomNav = findViewById(R.id.bottomNav);
        if(bottomNav != null) {
            bottomNav.findViewById(R.id.btnHome).setOnClickListener(v -> rvSections.smoothScrollToPosition(0));
            bottomNav.findViewById(R.id.btnSearch).setOnClickListener(v -> startActivity(new Intent(this, activity_search.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
            bottomNav.findViewById(R.id.btnLibrary).setOnClickListener(v -> startActivity(new Intent(this, activity_library.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
            bottomNav.findViewById(R.id.btnCreate).setOnClickListener(v -> startActivity(new Intent(this, activity_create_playlist.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
        }
    }

    private void setupUserDrawer() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userName = "User";
        String userInitial = "U";

        if (currentUser != null) {
            if (currentUser.getDisplayName() != null && !currentUser.getDisplayName().isEmpty()) {
                userName = currentUser.getDisplayName();
            } else if (currentUser.getEmail() != null && !currentUser.getEmail().isEmpty()) {
                userName = currentUser.getEmail();
            }
            if (!userName.isEmpty()) {
                userInitial = userName.substring(0, 1).toUpperCase();
            }
        }

        TextView tvUserCircle = findViewById(R.id.tvUserCircle);
        if(tvUserCircle != null) {
            tvUserCircle.setText(userInitial);
            tvUserCircle.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        }

        View navigationView = findViewById(R.id.navigation_view);
        if(navigationView != null) {
            TextView tvUserNameDrawer = navigationView.findViewById(R.id.tvUserName);
            TextView tvUserInitialDrawer = navigationView.findViewById(R.id.tvUserInitial);
            if(tvUserNameDrawer!=null) tvUserNameDrawer.setText(userName);
            if(tvUserInitialDrawer!=null) tvUserInitialDrawer.setText(userInitial);

            View rowLogout = navigationView.findViewById(R.id.rowLogout);
            if(rowLogout!=null) {
                rowLogout.setOnClickListener(v -> {
                    mAuth.signOut();
                    finishAffinity();
                    startActivity(new Intent(this, activity_login.class));
                });
            }
        }
    }
}
