package com.example.vibe_streams.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;

public class Playlist implements Serializable {
    @DocumentId
    private String id;
    private String name;
    private ArrayList<Song> songList;

    // Firestore requires a public, no-argument constructor
    public Playlist() {
        this.songList = new ArrayList<>();
    }

    // FIX: Add the constructor that takes a name, which is used in activity_create_playlist
    public Playlist(String name) {
        this.name = name;
        this.songList = new ArrayList<>();
    }

    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public ArrayList<Song> getSongList() { return songList; }

    // --- Setters (Required for Firestore to build the object) ---
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSongList(ArrayList<Song> songList) { this.songList = songList; }

    // --- Convenience Methods ---
    public String getCoverUrl() {
        if (songList != null && !songList.isEmpty() && songList.get(0) != null) {
            return songList.get(0).getCoverUrl();
        }
        return null;
    }

    public int getSongCount() {
        return (songList != null) ? songList.size() : 0;
    }
}
