package com.example.gamehub_m;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamehub_m.ui.login.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Main Activity - Home screen with game selection grid
 */
public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ImageView btnUser;
    private ImageView btnLeaderboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();


        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            navigateToLogin();
            return;
        }


        initViews();
        setupListeners();


        String displayName = currentUser.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = currentUser.getEmail() != null 
                    ? currentUser.getEmail().split("@")[0] 
                    : "Gamer";
        }
        Toast.makeText(this, String.format(getString(R.string.hello_user), displayName), Toast.LENGTH_SHORT).show();
    }

    private void initViews() {
        btnUser = findViewById(R.id.btnUser);
        btnLeaderboard = findViewById(R.id.btnLeaderboard);
    }

    private void setupListeners() {

        if (btnUser != null) {
            btnUser.setOnClickListener(v -> showProfileDialog());
        }


        if (btnLeaderboard != null) {
            btnLeaderboard.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(MainActivity.this, 
                        com.example.gamehub_m.ui.leaderboard.LeaderboardActivity.class);
                startActivity(intent);
            });
        }


        setupGameButtons();
    }

    private void setupGameButtons() {

        View btnGuess = findViewById(R.id.btnGuess);
        if (btnGuess != null) {
            btnGuess.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, 
                        com.example.gamehub_m.ui.games.GuessActivity.class);
                startActivity(intent);
            });
        }


        View btnMemory = findViewById(R.id.btnMemory);
        if (btnMemory != null) {
            btnMemory.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, 
                        com.example.gamehub_m.ui.games.MemoryActivity.class);
                startActivity(intent);
            });
        }
        

        View btnRps = findViewById(R.id.btnRps);
        if (btnRps != null) {
            btnRps.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, 
                        com.example.gamehub_m.ui.games.RpsActivity.class);
                startActivity(intent);
            });
        }
        

        View btnTicTacToe = findViewById(R.id.btnTicTacToe);
        if (btnTicTacToe != null) {
            btnTicTacToe.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, 
                        com.example.gamehub_m.ui.games.TicTacToeActivity.class);
                startActivity(intent);
            });
        }


        int[] comingSoonButtons = {
            R.id.btnSnake, R.id.btn2048
        };

        String[] gameNames = {
            "Snake", "2048"
        };

        for (int i = 0; i < comingSoonButtons.length; i++) {
            final String gameName = gameNames[i];
            View gameBtn = findViewById(comingSoonButtons[i]);
            if (gameBtn != null) {
                gameBtn.setOnClickListener(v -> {
                    Toast.makeText(this, gameName + " - Coming soon!", Toast.LENGTH_SHORT).show();
                });
            }
        }
    }

    private void showProfileDialog() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            navigateToLogin();
            return;
        }

        String displayName = user.getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            displayName = user.getEmail() != null 
                    ? user.getEmail().split("@")[0] 
                    : "Unknown";
        }
        String email = user.getEmail() != null ? user.getEmail() : "N/A";

        String message = "Username: " + displayName + "\nEmail: " + email;

        new AlertDialog.Builder(this)
                .setTitle(R.string.profile)
                .setMessage(message)
                .setPositiveButton(R.string.logout, (dialog, which) -> showLogoutConfirmation())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.logout)
                .setMessage(R.string.logout_confirm)
                .setPositiveButton(R.string.yes, (dialog, which) -> logout())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void logout() {
        mAuth.signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mAuth.getCurrentUser() == null) {
            navigateToLogin();
        }
    }
}