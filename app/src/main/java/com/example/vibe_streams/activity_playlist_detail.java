package com.example.vibe_streams;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vibe_streams.models.Playlist;
import com.example.vibe_streams.models.Song;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class activity_playlist_detail extends AppCompatActivity implements UserSongAdapterHorizontal.OnSongInteractionListener {

    private static final int ADD_SONG_REQUEST = 1;

    private TextView tvPlaylistName, tvPlaylistCreator;
    private RecyclerView rvPlaylistSongs;
    private ImageView btnBack, btnMore, ivPlaylistCover;
    private MaterialButton btnAddSongs, btnToggleDeleteMode;
    private FrameLayout miniPlayerContainer;

    private UserSongAdapterHorizontal songsAdapter;
    private ArrayList<Song> songList = new ArrayList<>();

    private FirebaseFirestore db;
    private String playlistId;
    private Playlist currentPlaylist;
    private boolean isDeleteMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_detail);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
        getIntentDataAndLoadPlaylist();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MusicPlayerManager.getInstance().getCurrentSong() != null) {
            showOrUpdateMiniPlayer();
        }
    }

    private void initializeViews() {
        tvPlaylistName = findViewById(R.id.tvPlaylistName);
        tvPlaylistCreator = findViewById(R.id.tvPlaylistCreator);
        rvPlaylistSongs = findViewById(R.id.rvPlaylistSongs);
        btnBack = findViewById(R.id.btnBack);
        btnMore = findViewById(R.id.btnMore);
        btnAddSongs = findViewById(R.id.btnAddSongs);
        btnToggleDeleteMode = findViewById(R.id.btnToggleDeleteMode);
        miniPlayerContainer = findViewById(R.id.miniPlayerContainer);
        ivPlaylistCover = findViewById(R.id.ivPlaylistCover);
    }

    private void getIntentDataAndLoadPlaylist() {
        playlistId = getIntent().getStringExtra("playlist_id");
        if (playlistId != null) {
            loadPlaylistFromFirestore();
        }
    }

    private void loadPlaylistFromFirestore() {
        db.collection("playlists").document(playlistId)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading playlist", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        currentPlaylist = snapshot.toObject(Playlist.class);
                        if (currentPlaylist != null) {
                            updateUI();
                        }
                    } else {
                        Toast.makeText(this, "Playlist deleted.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void setupRecyclerView() {
        rvPlaylistSongs.setLayoutManager(new GridLayoutManager(this, 2));
        songsAdapter = new UserSongAdapterHorizontal(this, songList, this);
        rvPlaylistSongs.setAdapter(songsAdapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnMore.setOnClickListener(this::showPopupMenu);
        btnAddSongs.setOnClickListener(v -> addSong());
        btnToggleDeleteMode.setOnClickListener(v -> toggleDeleteMode());
    }

    private void toggleDeleteMode() {
        isDeleteMode = !isDeleteMode;
        if (isDeleteMode) {
            btnToggleDeleteMode.setText("Done");
            // FIX: Use a built-in Android icon that is guaranteed to exist.
            btnToggleDeleteMode.setIconResource(android.R.drawable.ic_menu_save);
            Toast.makeText(this, "Delete mode: Tap a song to remove", Toast.LENGTH_SHORT).show();
        } else {
            btnToggleDeleteMode.setText("Delete Song");
            btnToggleDeleteMode.setIconResource(android.R.drawable.ic_menu_delete);
        }
    }

    private void updateUI() {
        if (currentPlaylist == null) return;
        tvPlaylistName.setText(currentPlaylist.getName());
        tvPlaylistCreator.setText("By Vibe Streams • " + currentPlaylist.getSongCount() + " songs");

        songList.clear();
        if (currentPlaylist.getSongList() != null) {
            songList.addAll(currentPlaylist.getSongList());
        }
        songsAdapter.notifyDataSetChanged();

        Glide.with(this).load(currentPlaylist.getCoverUrl()).placeholder(R.drawable.music_placeholder).into(ivPlaylistCover);
    }

    @Override
    public void onSongClicked(Song song, int position) {
        if (isDeleteMode) {
            confirmRemoveSong(song);
        } else {
            MusicPlayerManager.getInstance().setPlaylist(songList, position, this);
            showOrUpdateMiniPlayer();
        }
    }

    private void confirmRemoveSong(Song song) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Song")
                .setMessage("Are you sure you want to remove '" + song.getTitle() + "' from this playlist?")
                .setPositiveButton("Remove", (dialog, which) -> removeSongFromPlaylist(song))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeSongFromPlaylist(Song song) {
        if (playlistId == null) return;
        db.collection("playlists").document(playlistId)
                .update("songList", FieldValue.arrayRemove(song))
                .addOnSuccessListener(aVoid -> Toast.makeText(activity_playlist_detail.this, "Song removed", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(activity_playlist_detail.this, "Failed to remove song", Toast.LENGTH_SHORT).show());
    }

    private void showOrUpdateMiniPlayer() {
        if (MusicPlayerManager.getInstance().getCurrentSong() == null) {
            if (miniPlayerContainer != null) miniPlayerContainer.setVisibility(View.GONE);
            return;
        }
        if (miniPlayerContainer != null) miniPlayerContainer.setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.miniPlayerContainer, new MiniPlayerFragment(), "MINI_PLAYER_FRAGMENT")
                .commit();
    }

    private void addSong() {
        Intent intent = new Intent(this, AddSongActivity.class);
        startActivityForResult(intent, ADD_SONG_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_SONG_REQUEST && resultCode == RESULT_OK && data != null) {
            Song selectedSong = (Song) data.getSerializableExtra("selected_song");
            if (selectedSong != null) {
                db.collection("playlists").document(playlistId)
                        .update("songList", FieldValue.arrayUnion(selectedSong))
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Added to playlist", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to add song", Toast.LENGTH_SHORT).show());
            }
        }
    }

    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.playlist_detail_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_share) {
                sharePlaylist();
                return true;
            } else if (itemId == R.id.action_rename) {
                showRenameDialog();
                return true;
            } else if (itemId == R.id.action_delete) {
                showDeleteConfirmationDialog();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void sharePlaylist() {
        if (currentPlaylist == null || currentPlaylist.getSongList().isEmpty()) {
            Toast.makeText(this, "Cannot share an empty playlist.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder shareText = new StringBuilder("Check out my playlist: " + currentPlaylist.getName() + "\n");
        for (Song song : currentPlaylist.getSongList()) {
            shareText.append("\n- ").append(song.getTitle()).append(" by ").append(song.getArtist());
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
        startActivity(Intent.createChooser(shareIntent, "Share Playlist"));
    }

    private void showRenameDialog() {
        if (currentPlaylist == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Playlist");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentPlaylist.getName());
        builder.setView(input);

        builder.setPositiveButton("Rename", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(currentPlaylist.getName())) {
                db.collection("playlists").document(playlistId).update("name", newName)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Playlist renamed", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show());
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Playlist")
                .setMessage("Are you sure you want to permanently delete this playlist?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("playlists").document(playlistId).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Playlist deleted", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to delete playlist", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
