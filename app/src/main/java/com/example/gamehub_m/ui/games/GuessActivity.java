package com.example.gamehub_m.ui.games;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.gamehub_m.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Random;

/**
 * Guess Number Game Activity
 * Player guesses a number between 1-100 within 10 attempts
 */
public class GuessActivity extends AppCompatActivity {

    private EditText guessInput;
    private Button guessButton;
    private Button restartButton;
    private TextView hintText;
    private TextView attemptsText;
    private TextView scoreText;
    private ImageButton btnBack;
    private CardView hintCard;

    private Random random = new Random();
    private int targetNumber;
    private int attemptsRemaining;
    private int totalScore = 0;
    private boolean gameOver = false;

    private static final int MAX_ATTEMPTS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guess);

        initViews();
        setupListeners();
        startNewRound();
    }

    private void initViews() {
        guessInput = findViewById(R.id.guessInput);
        guessButton = findViewById(R.id.guessButton);
        restartButton = findViewById(R.id.restartButton);
        hintText = findViewById(R.id.hintText);
        attemptsText = findViewById(R.id.attemptsText);
        scoreText = findViewById(R.id.scoreText);
        btnBack = findViewById(R.id.btnBack);
        hintCard = findViewById(R.id.hintCard);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        guessButton.setOnClickListener(v -> onGuess());

        restartButton.setOnClickListener(v -> restartGame());
    }

    private void startNewRound() {

        targetNumber = random.nextInt(100) + 1;
        attemptsRemaining = MAX_ATTEMPTS;
        gameOver = false;
        

        guessInput.setText("");
        guessInput.setEnabled(true);
        guessButton.setEnabled(true);
        attemptsText.setText(String.valueOf(attemptsRemaining));
        hintText.setText("Make your first guess!");
        hintText.setTextColor(getResources().getColor(R.color.text_secondary, null));
    }

    private void onGuess() {
        String inputText = guessInput.getText().toString().trim();
        
        if (inputText.isEmpty()) {
            Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show();
            return;
        }

        int guessedNumber;
        try {
            guessedNumber = Integer.parseInt(inputText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (guessedNumber < 1 || guessedNumber > 100) {
            Toast.makeText(this, "Number must be between 1 and 100", Toast.LENGTH_SHORT).show();
            return;
        }

        if (gameOver) {
            return;
        }


        if (guessedNumber == targetNumber) {

            int pointsEarned = attemptsRemaining;
            totalScore += pointsEarned;
            scoreText.setText(String.valueOf(totalScore));
            
            hintText.setText("🎉 Correct! You guessed " + guessedNumber + "!\n+" + pointsEarned + " points!");
            hintText.setTextColor(getResources().getColor(R.color.success, null));
            
            Toast.makeText(this, "+" + pointsEarned + " points!", Toast.LENGTH_SHORT).show();
            

            guessInput.setText("");
            startNewRound();
            
        } else {

            attemptsRemaining--;
            attemptsText.setText(String.valueOf(attemptsRemaining));
            guessInput.setText("");

            if (attemptsRemaining == 0) {

                gameOver = true;
                guessInput.setEnabled(false);
                guessButton.setEnabled(false);
                
                hintText.setText("💀 Game Over!\nThe number was " + targetNumber + "\nFinal Score: " + totalScore);
                hintText.setTextColor(getResources().getColor(R.color.error, null));
                

                saveScoreToFirebase();
                
            } else {

                if (guessedNumber < targetNumber) {
                    hintText.setText("📈 Too LOW!\nTry a higher number.");
                    hintText.setTextColor(getResources().getColor(R.color.secondary, null));
                } else {
                    hintText.setText("📉 Too HIGH!\nTry a lower number.");
                    hintText.setTextColor(getResources().getColor(R.color.warning, null));
                }
            }
        }
    }

    private void restartGame() {
        if (totalScore > 0) {
            Toast.makeText(this, "Saving score...", Toast.LENGTH_SHORT).show();
            saveScoreToFirebase(() -> {
                resetGame();
                Toast.makeText(this, "Score saved & Game restarted!", Toast.LENGTH_SHORT).show();
            });
        } else {
            resetGame();
            Toast.makeText(this, "Game restarted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void resetGame() {
        totalScore = 0;
        scoreText.setText("0");
        startNewRound();
    }

    private void saveScoreToFirebase(Runnable onComplete) {
        if (totalScore > 0) {
            com.example.gamehub_m.data.ScoreManager.saveScore("Guess", totalScore, 
                new com.example.gamehub_m.data.ScoreManager.OnScoreSavedListener() {
                    @Override
                    public void onSuccess() {
                        android.util.Log.d("GuessGame", "Score saved successfully: " + totalScore);
                        if (onComplete != null) onComplete.run();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        android.util.Log.e("GuessGame", "Failed to save score: " + errorMessage);
                        Toast.makeText(GuessActivity.this, "Save failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                        if (onComplete != null) onComplete.run(); // Proceed anyway
                    }
                });
        } else {
            if (onComplete != null) onComplete.run();
        }
    }

    // Overload for simple call
    private void saveScoreToFirebase() {
        saveScoreToFirebase(null);
    }

    @Override
    public void onBackPressed() {
        if (totalScore > 0) {
            Toast.makeText(this, "Saving score...", Toast.LENGTH_SHORT).show();
            saveScoreToFirebase(() -> {
                finish();
            });
        } else {
            super.onBackPressed();
        }
    }
}
