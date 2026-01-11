package com.example.gamehub_m.ui.leaderboard;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamehub_m.R;
import com.example.gamehub_m.data.ScoreManager;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView leaderboardRecycler;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    
    private MaterialButton btnFilterGuess;
    private MaterialButton btnFilterMemory;
    private MaterialButton btnFilterRps;
    private MaterialButton btnFilterTicTacToe;
    private MaterialButton btnFilterSnake;

    private LeaderboardAdapter adapter;
    private String currentGame = "Guess"; // Default game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        initViews();
        setupRecyclerView();
        setupListeners();
        

        selectGame("Guess");
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        leaderboardRecycler = findViewById(R.id.leaderboardRecycler);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);
        
        btnFilterGuess = findViewById(R.id.btnFilterGuess);
        btnFilterMemory = findViewById(R.id.btnFilterMemory);
        btnFilterRps = findViewById(R.id.btnFilterRps);
        btnFilterTicTacToe = findViewById(R.id.btnFilterTicTacToe);
        btnFilterSnake = findViewById(R.id.btnFilterSnake);
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter();
        leaderboardRecycler.setLayoutManager(new LinearLayoutManager(this));
        leaderboardRecycler.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnFilterGuess.setOnClickListener(v -> selectGame("Guess"));
        btnFilterMemory.setOnClickListener(v -> selectGame("Memory"));
        btnFilterRps.setOnClickListener(v -> selectGame("RPS"));
        btnFilterTicTacToe.setOnClickListener(v -> selectGame("TicTacToe"));
        btnFilterSnake.setOnClickListener(v -> Toast.makeText(this, "Coming Soon!", Toast.LENGTH_SHORT).show());
    }

    private void selectGame(String gameName) {
        currentGame = gameName;
        updateFilterButtons();
        loadLeaderboard(gameName);
    }

    private void updateFilterButtons() {

        resetButtonStyle(btnFilterGuess);
        resetButtonStyle(btnFilterMemory);
        resetButtonStyle(btnFilterRps);
        resetButtonStyle(btnFilterTicTacToe);
        

        if (currentGame.equals("Guess")) {
            highlightButtonStyle(btnFilterGuess);
        } else if (currentGame.equals("Memory")) {
            highlightButtonStyle(btnFilterMemory);
        } else if (currentGame.equals("RPS")) {
            highlightButtonStyle(btnFilterRps);
        } else if (currentGame.equals("TicTacToe")) {
            highlightButtonStyle(btnFilterTicTacToe);
        }
    }

    private void resetButtonStyle(MaterialButton button) {
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setTextColor(Color.WHITE);
        button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_secondary)));
        button.setStrokeWidth(2);
    }

    private void highlightButtonStyle(MaterialButton button) {
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
        button.setTextColor(Color.WHITE);
        button.setStrokeWidth(0);
    }

    private void loadLeaderboard(String gameName) {
        showLoading(true);
        emptyStateText.setVisibility(View.GONE);
        android.util.Log.d("Leaderboard", "Loading scores for: " + gameName);

        // Safety timeout
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (progressBar.getVisibility() == View.VISIBLE) {
                showLoading(false);
                if (adapter.getItemCount() == 0) {
                    emptyStateText.setText("Request timed out.\nCheck internet or database rules.");
                    emptyStateText.setVisibility(View.VISIBLE);
                }
                Toast.makeText(this, "Loading timed out", Toast.LENGTH_SHORT).show();
            }
        }, 10000); // 10 seconds timeout

        ScoreManager.getLeaderboard(gameName, 50, new ScoreManager.OnLeaderboardFetchListener() {
            @Override
            public void onSuccess(List<ScoreManager.LeaderboardEntry> entries) {
                android.util.Log.d("Leaderboard", "Loaded " + entries.size() + " entries");
                showLoading(false);
                if (entries.isEmpty()) {
                    emptyStateText.setText("No scores yet!\nBe the first to play.");
                    emptyStateText.setVisibility(View.VISIBLE);
                    adapter.setEntries(entries); 
                } else {
                    emptyStateText.setVisibility(View.GONE);
                    adapter.setEntries(entries);
                    

                    com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String currentUserId = user.getUid();
                        boolean found = false;
                        for (int i = 0; i < entries.size(); i++) {
                            if (entries.get(i).userId != null && entries.get(i).userId.equals(currentUserId)) {
                                Toast.makeText(LeaderboardActivity.this, "Your Rank: " + (i + 1), Toast.LENGTH_SHORT).show();
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            Toast.makeText(LeaderboardActivity.this, "You are not in the top 50 yet!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }

            @Override
            public void onError(String errorMessage) {
                android.util.Log.e("Leaderboard", "Error: " + errorMessage);
                showLoading(false);
                Toast.makeText(LeaderboardActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        leaderboardRecycler.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}
