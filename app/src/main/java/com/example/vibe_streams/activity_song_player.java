package com.example.vibe_streams;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.vibe_streams.models.Song;

import java.util.Locale;

public class activity_song_player extends AppCompatActivity {

    private TextView tvSongTitle, tvArtist, tvCurrentTime, tvTotalTime;
    private ImageView ivSongCover, btnPlayPause, btnNext, btnPrev, btnBack;
    private SeekBar seekBar;

    private MusicPlayerManager player;
    private final Handler handler = new Handler();
    private Runnable updateSeekBarRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_player);

        player = MusicPlayerManager.getInstance();

        initializeViews();
        setupClickListeners();
        setupSeekBarListener();

        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                updatePlayerState();
                handler.postDelayed(this, 1000);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
        handler.post(updateSeekBarRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateSeekBarRunnable);
    }

    private void initializeViews() {
        tvSongTitle = findViewById(R.id.tvSongTitle);
        tvArtist = findViewById(R.id.tvArtist);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvTotalTime = findViewById(R.id.tvTotalTime);
        ivSongCover = findViewById(R.id.ivSongCover);
        btnPlayPause = findViewById(R.id.btnPlayPause);
        btnNext = findViewById(R.id.btnNext);
        btnPrev = findViewById(R.id.btnPrev);
        btnBack = findViewById(R.id.btnBack);
        seekBar = findViewById(R.id.seekBar);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnPlayPause.setOnClickListener(v -> {
            player.playPause();
            updateUI();
        });

        btnNext.setOnClickListener(v -> {
            player.next(this);
            updateUI();
        });

        btnPrev.setOnClickListener(v -> {
            player.previous(this);
            updateUI();
        });
    }

    private void setupSeekBarListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void updateUI() {
        Song currentSong = player.getCurrentSong();
        if (currentSong == null) return;

        tvSongTitle.setText(currentSong.getTitle());
        tvArtist.setText(currentSong.getArtist());
        
        // FIX: Load cover art from the String URL provided by Firebase
        Glide.with(this)
                .load(currentSong.getCoverUrl())
                .placeholder(R.drawable.music_placeholder)
                .into(ivSongCover);

        // FIX: Use the correct drawable resource 'pause'
        if (player.isPlaying()) {
            btnPlayPause.setImageResource(R.drawable.resume);
        } else {
            btnPlayPause.setImageResource(R.drawable.play);
        }
    }

    private void updatePlayerState() {
        MediaPlayer mp = player.getMediaPlayer();
        if (mp == null || !mp.isPlaying()) return;

        try {
            int currentPosition = mp.getCurrentPosition();
            seekBar.setProgress(currentPosition);
            tvCurrentTime.setText(formatTime(currentPosition));

            int duration = mp.getDuration();
            if (duration > 0 && seekBar.getMax() != duration) {
                seekBar.setMax(duration);
                tvTotalTime.setText(formatTime(duration));
            }
        } catch (IllegalStateException e) {
        }
    }

    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }
}
