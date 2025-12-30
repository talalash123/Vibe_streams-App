package com.example.vibe_streams;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vibe_streams.models.Song;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AdminPanelActivity extends AppCompatActivity implements AdminSongAdapter.OnSongActionClickListener {

    private RecyclerView rvSongs;
    private FloatingActionButton fabAddSong;
    private AdminSongAdapter adapter;
    private ArrayList<Song> songList = new ArrayList<>();

    private FirebaseFirestore db;
    private ListenerRegistration firestoreListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupFirestoreListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firestoreListener != null) {
            firestoreListener.remove();
        }
    }

    private void initializeViews() {
        rvSongs = findViewById(R.id.rvSongs);
        fabAddSong = findViewById(R.id.fabAddSong);
    }

    private void setupRecyclerView() {
        rvSongs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AdminSongAdapter(this, songList, this);
        rvSongs.setAdapter(adapter);
    }

    private void setupFirestoreListener() {
        firestoreListener = db.collection("songs")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Error listening for updates", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        songList.clear();
                        for (QueryDocumentSnapshot doc : snapshots) {
                            Song song = doc.toObject(Song.class);
                            // FIX: Only add valid songs to the list to prevent empty items
                            if (song.getId() != null && !song.getId().isEmpty() &&
                                song.getTitle() != null && !song.getTitle().isEmpty()) {
                                songList.add(song);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void setupClickListeners() {
        fabAddSong.setOnClickListener(v -> {
            startActivity(new Intent(AdminPanelActivity.this, AddEditSongActivity.class));
        });
    }

    @Override
    public void onEditClick(Song song) {
        Intent intent = new Intent(this, AddEditSongActivity.class);
        intent.putExtra("editing_song", song);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Song song) {
        // Add a safety check for a valid ID before attempting to delete
        if (song.getId() == null || song.getId().isEmpty()) {
            Toast.makeText(this, "Cannot delete song with invalid ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Song")
                .setMessage("Are you sure you want to delete '" + song.getTitle() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("songs").document(song.getId()).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(AdminPanelActivity.this, "Song deleted", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(ex -> Toast.makeText(AdminPanelActivity.this, "Failed to delete song", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
