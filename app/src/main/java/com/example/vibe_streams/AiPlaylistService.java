package com.example.vibe_streams;

import android.util.Log;

import com.example.vibe_streams.models.Song;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class AiPlaylistService {

    private static final String TAG = "AiPlaylistService";

    public interface AiPlaylistCallback {
        void onPlaylistReady(ArrayList<Song> playlist);
        void onError(String message);
    }

    private final GenerativeModelFutures generativeModel;
    private final Executor mainExecutor;

    public AiPlaylistService(Executor mainExecutor) {
        this.mainExecutor = mainExecutor;
        GenerativeModel gm = new GenerativeModel("gemini-pro", BuildConfig.GEMINI_API_KEY);
        generativeModel = GenerativeModelFutures.from(gm);
    }

    public void generatePlaylist(String prompt, List<Song> allSongs, AiPlaylistCallback callback) {
        Content content = new Content.Builder().addText(prompt).build();

        ListenableFuture<GenerateContentResponse> future = generativeModel.generateContent(content);
        Futures.addCallback(future, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    String textResponse = result.getText();
                    if (textResponse == null) {
                        throw new Exception("Received empty response from AI.");
                    }
                    Log.d(TAG, "Full AI Response: " + textResponse);
                    ArrayList<Song> playlist = parseResponseAndFindSongs(textResponse, allSongs);
                    mainExecutor.execute(() -> callback.onPlaylistReady(playlist));
                } catch (Exception e) {
                    mainExecutor.execute(() -> callback.onError(e.getMessage()));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "AI content generation failed", t);
                mainExecutor.execute(() -> callback.onError(t.getMessage()));
            }
        }, mainExecutor);
    }

    private ArrayList<Song> parseResponseAndFindSongs(String responseText, List<Song> allSongs) {
        ArrayList<Song> foundSongs = new ArrayList<>();
        String lowerCaseResponse = responseText.toLowerCase();

        // First, try a precise search by splitting the response into lines or commas
        String[] potentialTitles = responseText.split("[\n,]+");
        for (String titlePart : potentialTitles) {
            String cleanedTitle = titlePart.replaceAll("^\\d+\\.\\s*", "").replaceAll("\"", "").trim();
            if (cleanedTitle.isEmpty()) continue;

            for (Song song : allSongs) {
                if (song.getTitle().equalsIgnoreCase(cleanedTitle)) {
                    if (!foundSongs.stream().anyMatch(s -> s.getId().equals(song.getId()))) {
                        foundSongs.add(song);
                        break;
                    }
                }
            }
        }

        // If the precise search yields few results, fall back to a broader search
        if (foundSongs.size() < 5) {
            for (Song song : allSongs) {
                if (lowerCaseResponse.contains(song.getTitle().toLowerCase())) {
                    if (!foundSongs.stream().anyMatch(s -> s.getId().equals(song.getId()))) {
                        foundSongs.add(song);
                    }
                }
            }
        }
        
        Log.d(TAG, "Found " + foundSongs.size() + " songs from AI response.");
        return foundSongs;
    }
}
