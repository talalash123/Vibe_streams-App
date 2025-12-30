package com.example.vibe_streams.models;

import java.io.Serializable;

public class Song implements Serializable {
    private String id;
    private String title;
    private String artist;
    private String coverUrl;
    private String audioUrl;
    private String category;

    // Firestore requires a public, no-argument constructor
    public Song() {}

    // --- Getters ---
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getCoverUrl() { return coverUrl; }
    public String getAudioUrl() { return audioUrl; }
    public String getCategory() { return category; }

    // --- Setters (Required for Firestore to build the object) ---
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setArtist(String artist) { this.artist = artist; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public void setAudioUrl(String audioUrl) { this.audioUrl = audioUrl; }
    public void setCategory(String category) { this.category = category; }
}
