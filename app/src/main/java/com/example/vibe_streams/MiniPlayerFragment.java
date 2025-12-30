package com.example.vibe_streams;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.vibe_streams.models.Song;

public class MiniPlayerFragment extends Fragment {

    private TextView tvSongTitle, tvArtist;
    private ImageView ivCoverArt, btnPlayPause, btnNext, btnPrev;

    private MusicPlayerManager player;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateUiRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mini_player, container, false);

        player = MusicPlayerManager.getInstance();
        initializeViews(view);
        setupClickListeners(view);
        initializeUiUpdater();

        return view;
    }

    private void initializeViews(View view) {
        tvSongTitle = view.findViewById(R.id.tvMiniSongTitle);
        tvArtist = view.findViewById(R.id.tvMiniArtist);
        ivCoverArt = view.findViewById(R.id.ivMiniCoverArt);
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        btnNext = view.findViewById(R.id.btnNext);
        btnPrev = view.findViewById(R.id.btnPrev);
    }

    private void setupClickListeners(View view) {
        btnPlayPause.setOnClickListener(v -> {
            if (player.getCurrentSong() == null) return;
            player.playPause();
            updateMiniPlayerUI();
        });

        btnNext.setOnClickListener(v -> {
            if (player.getCurrentSong() == null || getActivity() == null) return;
            player.next(getActivity());
            updateMiniPlayerUI();
        });

        btnPrev.setOnClickListener(v -> {
            if (player.getCurrentSong() == null || getActivity() == null) return;
            player.previous(getActivity());
            updateMiniPlayerUI();
        });

        view.setOnClickListener(v -> {
            if (player.getCurrentSong() == null) return;
            Intent intent = new Intent(getActivity(), activity_song_player.class);
            startActivity(intent);
        });
    }

    private void initializeUiUpdater() {
        updateUiRunnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded()) { // Check if fragment is attached
                    updateMiniPlayerUI();
                    handler.postDelayed(this, 1000);
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(updateUiRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updateUiRunnable);
    }

    public void updateMiniPlayerUI() {
        if (!isAdded() || player == null) {
            return;
        }

        Song currentSong = player.getCurrentSong();

        if (currentSong == null) {
            if (getView() != null) getView().setVisibility(View.GONE);
            return;
        }

        if (getView() != null) getView().setVisibility(View.VISIBLE);

        tvSongTitle.setText(currentSong.getTitle());
        tvArtist.setText(currentSong.getArtist());

        if (getContext() != null) {
            Glide.with(getContext())
                .load(currentSong.getCoverUrl())
                .placeholder(R.drawable.music_placeholder)
                .into(ivCoverArt);
        }

        // FIX: Use the correct drawable resource 'pause'
        if (player.isPlaying()) {
            btnPlayPause.setImageResource(R.drawable.resume);
        } else {
            btnPlayPause.setImageResource(R.drawable.play);
        }
    }
}
