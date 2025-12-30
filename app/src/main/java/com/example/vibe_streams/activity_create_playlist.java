package com.example.vibe_streams;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.vibe_streams.models.Playlist;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class activity_create_playlist extends AppCompatActivity {

    private TextInputEditText etPlaylistName;
    private MaterialButton btnCreatePlaylist;
    private ImageView btnBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_playlist);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();
        setupBottomNav();
    }

    private void initializeViews() {
        etPlaylistName = findViewById(R.id.etPlaylistName);
        btnCreatePlaylist = findViewById(R.id.btnCreatePlaylist);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCreatePlaylist.setOnClickListener(v -> createPlaylist());
    }

    private void createPlaylist() {
        String playlistName = etPlaylistName.getText().toString().trim();

        if (playlistName.isEmpty()) {
            etPlaylistName.setError("Please give your playlist a name");
            return;
        }

        Playlist newPlaylist = new Playlist(playlistName);

        db.collection("playlists").add(newPlaylist)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, "Playlist '" + playlistName + "' created!", Toast.LENGTH_LONG).show();
                // Go to library, which will now show the new playlist
                Intent intent = new Intent(this, activity_library.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error creating playlist", Toast.LENGTH_SHORT).show();
            });
    }

    private void setupBottomNav() {
        View bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.findViewById(R.id.btnHome).setOnClickListener(v -> startActivity(new Intent(this, activity_user_home.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
            bottomNav.findViewById(R.id.btnSearch).setOnClickListener(v -> startActivity(new Intent(this, activity_search.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)));
            bottomNav.findViewById(R.id.btnLibrary).setOnClickListener(v -> {
                if (!isTaskRoot()) {
                    finish();
                } else {
                    startActivity(new Intent(this, activity_library.class).addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                }
            });
            bottomNav.findViewById(R.id.btnCreate).setOnClickListener(v -> { /* Already here */ });
        }
    }
}
