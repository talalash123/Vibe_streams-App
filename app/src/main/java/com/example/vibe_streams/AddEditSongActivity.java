package com.example.vibe_streams;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.vibe_streams.models.Song;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class AddEditSongActivity extends AppCompatActivity {

    private EditText etSongTitle, etArtistName, etAudioUrl, etCoverUrl;
    private Button btnSaveSong;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private boolean isEditing = false;
    private Song existingSong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_song);

        db = FirebaseFirestore.getInstance();

        initializeViews();
        setupClickListeners();

        if (getIntent().hasExtra("editing_song")) {
            isEditing = true;
            existingSong = (Song) getIntent().getSerializableExtra("editing_song");
            prepareForEditing();
        }
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etSongTitle = findViewById(R.id.etSongTitle);
        etArtistName = findViewById(R.id.etArtistName);
        etAudioUrl = findViewById(R.id.etAudioUrl);
        etCoverUrl = findViewById(R.id.etCoverUrl);
        btnSaveSong = findViewById(R.id.btnSaveSong);
        progressBar = findViewById(R.id.progressBar);
    }

    private void prepareForEditing() {
        getSupportActionBar().setTitle("Edit Song");
        etSongTitle.setText(existingSong.getTitle());
        etArtistName.setText(existingSong.getArtist());
        etAudioUrl.setText(existingSong.getAudioUrl());
        etCoverUrl.setText(existingSong.getCoverUrl());
        btnSaveSong.setText("Update Song");
    }

    private void setupClickListeners() {
        btnSaveSong.setOnClickListener(v -> saveSong());
    }

    private void saveSong() {
        String title = etSongTitle.getText().toString().trim();
        String artist = etArtistName.getText().toString().trim();
        String audioUrl = etAudioUrl.getText().toString().trim();
        String coverUrl = etCoverUrl.getText().toString().trim();

        if (title.isEmpty() || artist.isEmpty() || audioUrl.isEmpty() || coverUrl.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSaveSong.setEnabled(false);

        Song song = isEditing ? existingSong : new Song();
        if (!isEditing) {
            song.setId(UUID.randomUUID().toString());
        }
        song.setTitle(title);
        song.setArtist(artist);
        song.setAudioUrl(audioUrl);
        song.setCoverUrl(coverUrl);
        song.setCategory("Curated"); // You can change this if you want categories

        db.collection("songs").document(song.getId()).set(song)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, isEditing ? "Song Updated" : "Song Added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Operation Failed", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnSaveSong.setEnabled(true);
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
