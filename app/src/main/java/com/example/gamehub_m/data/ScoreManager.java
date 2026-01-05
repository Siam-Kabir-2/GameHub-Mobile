package com.example.gamehub_m.data;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Firebase Score Manager - Saves and retrieves game scores from Firebase Realtime Database
 */
public class ScoreManager {

    private static final String SCORES_REF = "scores";
    private static final String LEADERBOARD_REF = "leaderboard";

    private static DatabaseReference getDatabaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    /**
     * Save a game score for the current user
     * Structure: scores/{userId}/{gameName}/{scoreId}
     */
    public static void saveScore(String gameName, int score, OnScoreSavedListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            if (listener != null) {
                listener.onError("User not logged in");
            }
            return;
        }

        String userId = user.getUid();
        String displayName = user.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getEmail() != null ? user.getEmail().split("@")[0] : "Anonymous";
        }

        DatabaseReference scoresRef = getDatabaseRef().child(SCORES_REF).child(userId).child(gameName);
        
        // Create score entry
        Map<String, Object> scoreData = new HashMap<>();
        scoreData.put("score", score);
        scoreData.put("timestamp", System.currentTimeMillis());
        scoreData.put("displayName", displayName);

        // Push new score
        final String finalDisplayName = displayName;
        scoresRef.push().setValue(scoreData)
                .addOnSuccessListener(aVoid -> {

                    updateLeaderboard(gameName, userId, finalDisplayName, score, listener);
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onError(e.getMessage());
                    }
                });
    }

    /**
     * Update the leaderboard with the user's high score for a game
     * Structure: leaderboard/{gameName}/{userId}
     */
    private static void updateLeaderboard(String gameName, String userId, String displayName, int score, OnScoreSavedListener listener) {
        DatabaseReference leaderboardRef = getDatabaseRef()
                .child(LEADERBOARD_REF)
                .child(gameName)
                .child(userId);


        leaderboardRef.get().addOnCompleteListener(task -> {
            boolean shouldUpdate = true;
            
            if (task.isSuccessful() && task.getResult().exists()) {
                Long currentHighScore = task.getResult().child("highScore").getValue(Long.class);
                if (currentHighScore != null && currentHighScore >= score) {
                    shouldUpdate = false;
                }
            }

            if (shouldUpdate) {
                Map<String, Object> leaderboardData = new HashMap<>();
                leaderboardData.put("displayName", displayName);
                leaderboardData.put("highScore", score);
                leaderboardData.put("userId", userId);
                leaderboardData.put("updatedAt", System.currentTimeMillis());

                leaderboardRef.setValue(leaderboardData)
                        .addOnSuccessListener(aVoid -> {
                            if (listener != null) {
                                listener.onSuccess();
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (listener != null) {
                                listener.onError(e.getMessage());
                            }
                        });
            } else {
                if (listener != null) {
                    listener.onSuccess();
                }
            }
        });
    }

    /**
     * Fetch leaderboard for a specific game
     * @param gameName The name of the game (e.g., "Guess", "Memory")
     * @param limit Number of entries to fetch
     * @param listener Callback
     */
    public static void getLeaderboard(String gameName, int limit, OnLeaderboardFetchListener listener) {
        DatabaseReference gameLeaderboardRef = getDatabaseRef().child(LEADERBOARD_REF).child(gameName);
        
        // Order by highScore (ascending by default in Firebase)
        Query topScoresQuery = gameLeaderboardRef.orderByChild("highScore").limitToLast(limit);

        topScoresQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<LeaderboardEntry> entries = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    try {
                        String displayName = snapshot.child("displayName").getValue(String.class);
                        Long highScore = snapshot.child("highScore").getValue(Long.class);
                        String userId = snapshot.child("userId").getValue(String.class);
                        
                        if (displayName != null && highScore != null) {
                            entries.add(new LeaderboardEntry(userId, displayName, highScore));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                // Firebase returns ascending order, so reverse for descending (highest first)
                Collections.reverse(entries);
                
                if (listener != null) {
                    listener.onSuccess(entries);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if (listener != null) {
                    listener.onError(databaseError.getMessage());
                }
            }
        });
    }

    /**
     * Listener interface for score save operations
     */
    public interface OnScoreSavedListener {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * Listener interface for leaderboard fetch operations
     */
    public interface OnLeaderboardFetchListener {
        void onSuccess(List<LeaderboardEntry> entries);
        void onError(String errorMessage);
    }

    /**
     * Model for Leaderboard Entry
     */
    public static class LeaderboardEntry {
        public String userId;
        public String displayName;
        public long highScore;

        public LeaderboardEntry(String userId, String displayName, long highScore) {
            this.userId = userId;
            this.displayName = displayName;
            this.highScore = highScore;
        }
    }
}
