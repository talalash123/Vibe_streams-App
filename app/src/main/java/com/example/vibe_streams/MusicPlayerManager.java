package com.example.vibe_streams;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.util.Log;

import com.example.vibe_streams.models.Song;

import java.io.IOException;
import java.util.ArrayList;

public class MusicPlayerManager {

    private static MusicPlayerManager instance;
    private MediaPlayer mediaPlayer;

    private ArrayList<Song> playlist = new ArrayList<>();
    private int currentIndex = -1;

    private MusicPlayerManager() {}

    public static synchronized MusicPlayerManager getInstance() {
        if (instance == null) {
            instance = new MusicPlayerManager();
        }
        return instance;
    }

    public void playSong(Song song, Context context) {
        if (song == null) return;

        this.playlist = new ArrayList<>();
        this.playlist.add(song);
        this.currentIndex = 0;

        playCurrentSong(context);
    }

    public void setPlaylist(ArrayList<Song> playlist, int index, Context context) {
        this.playlist = playlist;
        this.currentIndex = index;
        playCurrentSong(context);
    }

    private void playCurrentSong(Context context) {
        if (currentIndex < 0 || currentIndex >= playlist.size()) return;

        Song currentSong = playlist.get(currentIndex);
        String audioUrl = currentSong.getAudioUrl();

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        // Standard MediaPlayer cannot play YouTube URLs. We'll log a warning for now.
        if (audioUrl != null && (audioUrl.contains("youtube.com") || audioUrl.contains("youtu.be"))) {
            Log.w("MusicPlayerManager", "YouTube URLs are not supported by the standard MediaPlayer.");
            return; // Do not proceed
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            );
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync(); // Use async preparation for network streams
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        } catch (IOException e) {
            Log.e("MusicPlayerManager", "Error setting data source: " + audioUrl, e);
            mediaPlayer = null;
        }
    }

    public Song getCurrentSong() {
        if (playlist.isEmpty() || currentIndex < 0 || currentIndex >= playlist.size()) {
            return null;
        }
        return playlist.get(currentIndex);
    }

    public void playPause() {
        if (mediaPlayer == null) return;
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void next(Context context) {
        if (!playlist.isEmpty()) {
            currentIndex = (currentIndex + 1) % playlist.size();
            playCurrentSong(context);
        }
    }

    public void previous(Context context) {
        if (!playlist.isEmpty()) {
            currentIndex = (currentIndex > 0) ? currentIndex - 1 : playlist.size() - 1;
            playCurrentSong(context);
        }
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void seekTo(int position) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(position);
        }
    }
}
