package com.example.gamehub_m.ui.games;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.gamehub_m.R;
import com.example.gamehub_m.data.ScoreManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Memory Game Activity (Simon Says style)
 * Watch the pattern and repeat the entire sequence from the beginning
 */
public class MemoryActivity extends AppCompatActivity {

    private View btnGreen, btnRed, btnYellow, btnBlue;
    private Button startButton;
    private TextView statusText, scoreText, roundText;
    private ImageButton btnBack;

    private List<Integer> sequence = new ArrayList<>();
    private List<Integer> userInput = new ArrayList<>();
    private int score = 0;
    private boolean canClick = false;
    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());

    private static final int GREEN = 0;
    private static final int RED = 1;
    private static final int YELLOW = 2;
    private static final int BLUE = 3;


    private static final int[] NORMAL_COLORS = {
            0xFF2E7D32, // Green
            0xFFC62828, // Red
            0xFFF9A825, // Yellow
            0xFF1565C0  // Blue
    };

    // Flash (active) colors
    private static final int[] FLASH_COLORS = {
            0xFF69F0AE, // Bright Green
            0xFFFF5252, // Bright Red
            0xFFFFFF00, // Bright Yellow
            0xFF448AFF  // Bright Blue
    };

    // Border colors
    private static final int[] BORDER_COLORS = {
            0xFF1B5E20, // Dark Green
            0xFF8E0000, // Dark Red
            0xFFF57F17, // Dark Yellow
            0xFF0D47A1  // Dark Blue
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        initViews();
        setupListeners();
        setColorButtonsEnabled(false);
    }

    private void initViews() {
        btnGreen = findViewById(R.id.btnGreen);
        btnRed = findViewById(R.id.btnRed);
        btnYellow = findViewById(R.id.btnYellow);
        btnBlue = findViewById(R.id.btnBlue);
        startButton = findViewById(R.id.startButton);
        statusText = findViewById(R.id.statusText);
        scoreText = findViewById(R.id.scoreText);
        roundText = findViewById(R.id.roundText);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        startButton.setOnClickListener(v -> startGame());

        btnGreen.setOnClickListener(v -> handleColorClick(GREEN));
        btnRed.setOnClickListener(v -> handleColorClick(RED));
        btnYellow.setOnClickListener(v -> handleColorClick(YELLOW));
        btnBlue.setOnClickListener(v -> handleColorClick(BLUE));
    }

    private void startGame() {

        if (score > 0) {
            saveScore();
        }


        sequence.clear();
        userInput.clear();
        score = 0;
        updateScore();
        startButton.setEnabled(false);
        startButton.setAlpha(0.5f);
        nextRound();
    }

    private void nextRound() {
        canClick = false;
        userInput.clear();


        int nextColor = random.nextInt(4);
        sequence.add(nextColor);

        roundText.setText(String.valueOf(sequence.size()));
        statusText.setText("Watch the pattern!");
        setColorButtonsEnabled(false);


        handler.postDelayed(this::playSequence, 500);
    }

    private void playSequence() {

        for (int i = 0; i < sequence.size(); i++) {
            final int color = sequence.get(i);
            final int delay = i * 800; // 800ms between each flash


            handler.postDelayed(() -> flashButton(color, true), delay);

            handler.postDelayed(() -> flashButton(color, false), delay + 500);
        }


        int totalDelay = sequence.size() * 800;
        handler.postDelayed(() -> {
            statusText.setText("Your turn! Repeat the pattern");
            setColorButtonsEnabled(true);
            canClick = true;
        }, totalDelay);
    }

    private void flashButton(int color, boolean flash) {
        View btn = getButton(color);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(48f);

        if (flash) {
            drawable.setColor(FLASH_COLORS[color]);
            drawable.setStroke(9, 0xFFFFFFFF);
            btn.setElevation(24f);
        } else {
            drawable.setColor(NORMAL_COLORS[color]);
            drawable.setStroke(9, BORDER_COLORS[color]);
            btn.setElevation(0f);
        }

        btn.setBackground(drawable);
    }

    private View getButton(int color) {
        switch (color) {
            case GREEN: return btnGreen;
            case RED: return btnRed;
            case YELLOW: return btnYellow;
            case BLUE: return btnBlue;
            default: return btnGreen;
        }
    }

    private void handleColorClick(int color) {
        if (!canClick) return;

        // Visual feedback
        flashButton(color, true);
        handler.postDelayed(() -> flashButton(color, false), 300);

        userInput.add(color);
        int index = userInput.size() - 1;

        // Check if correct
        if (!userInput.get(index).equals(sequence.get(index))) {
            gameOver();
            return;
        }


        if (userInput.size() == sequence.size()) {
            score++;
            updateScore();
            canClick = false;
            setColorButtonsEnabled(false);

            statusText.setText("✓ Correct! Next round...");

            // Start next round after delay
            handler.postDelayed(this::nextRound, 1000);
        }
    }

    private void gameOver() {
        canClick = false;
        setColorButtonsEnabled(false);

        statusText.setText("💀 Game Over! Score: " + score);
        startButton.setEnabled(true);
        startButton.setAlpha(1f);
        startButton.setText("PLAY AGAIN");

        Toast.makeText(this, "Game Over! Final Score: " + score, Toast.LENGTH_LONG).show();

        saveScore();
    }

    private void updateScore() {
        scoreText.setText(String.valueOf(score));
    }

    private void saveScore() {
        if (score > 0) {
            ScoreManager.saveScore("Memory", score, new ScoreManager.OnScoreSavedListener() {
                @Override
                public void onSuccess() {
                    android.util.Log.d("MemoryGame", "Score saved: " + score);
                }

                @Override
                public void onError(String errorMessage) {
                    android.util.Log.e("MemoryGame", "Failed to save: " + errorMessage);
                }
            });
        }
    }

    private void setColorButtonsEnabled(boolean enabled) {
        float alpha = enabled ? 1.0f : 0.6f;
        btnGreen.setAlpha(alpha);
        btnRed.setAlpha(alpha);
        btnYellow.setAlpha(alpha);
        btnBlue.setAlpha(alpha);
    }

    @Override
    public void onBackPressed() {
        if (score > 0) {
            saveScore();
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
