package com.example.gamehub_m.ui.games;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamehub_m.R;
import com.example.gamehub_m.data.ScoreManager;

import java.util.Random;

public class RpsActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView playerScoreText, cpuScoreText;
    private TextView playerMoveText, cpuMoveText, vsText, resultText, actionText;
    private Button btnRock, btnPaper, btnScissors, btnPlayAgain;
    private LinearLayout controlsArea;

    private int playerScore = 0;
    private int cpuScore = 0;
    private boolean isAnimating = false;

    private static final String MOVE_ROCK = "🪨";
    private static final String MOVE_PAPER = "📄";
    private static final String MOVE_SCISSORS = "✂️";
    private static final String MOVE_QUESTION = "❔";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rps);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        playerScoreText = findViewById(R.id.playerScoreText);
        cpuScoreText = findViewById(R.id.cpuScoreText);
        
        playerMoveText = findViewById(R.id.playerMoveText);
        cpuMoveText = findViewById(R.id.cpuMoveText);
        vsText = findViewById(R.id.vsText);
        resultText = findViewById(R.id.resultText);
        actionText = findViewById(R.id.actionText);
        
        btnRock = findViewById(R.id.btnRock);
        btnPaper = findViewById(R.id.btnPaper);
        btnScissors = findViewById(R.id.btnScissors);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);
        controlsArea = findViewById(R.id.controlsArea);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnRock.setOnClickListener(v -> playRound(MOVE_ROCK));
        btnPaper.setOnClickListener(v -> playRound(MOVE_PAPER));
        btnScissors.setOnClickListener(v -> playRound(MOVE_SCISSORS));

        btnPlayAgain.setOnClickListener(v -> resetRound());
    }

    private void playRound(String playerMove) {
        if (isAnimating) return;
        isAnimating = true;


        playerMoveText.setText(playerMove);
        cpuMoveText.setText(MOVE_QUESTION); // Hide CPU move initially
        

        setButtonsEnabled(false);
        resultText.setVisibility(View.INVISIBLE);
        actionText.setText("CPU is choosing...");


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String cpuMove = getCpuMove();
            cpuMoveText.setText(cpuMove);
            determineWinner(playerMove, cpuMove);
        }, 600);
    }

    private String getCpuMove() {
        String[] moves = {MOVE_ROCK, MOVE_PAPER, MOVE_SCISSORS};
        return moves[new Random().nextInt(moves.length)];
    }

    private void determineWinner(String pMove, String cMove) {
        // 0 = Draw, 1 = Player Win, -1 = CPU Win
        int result = 0;

        if (pMove.equals(cMove)) {
            result = 0;
        } else if (
            (pMove.equals(MOVE_ROCK) && cMove.equals(MOVE_SCISSORS)) ||
            (pMove.equals(MOVE_PAPER) && cMove.equals(MOVE_ROCK)) ||
            (pMove.equals(MOVE_SCISSORS) && cMove.equals(MOVE_PAPER))
        ) {
            result = 1;
        } else {
            result = -1;
        }

        showResult(result);
    }

    private void showResult(int result) {
        String message;
        int color;

        if (result == 1) {
            playerScore++;
            message = "YOU WIN!";
            color = getResources().getColor(R.color.success, null);
        } else if (result == -1) {
            cpuScore++;
            message = "CPU WINS!";
            color = 0xFFFF5252; // Red
        } else {
            message = "DRAW!";
            color = 0xFFFFFFFF; // White
        }

        updateScoreDisplay();

        resultText.setText(message);
        resultText.setTextColor(color);
        resultText.setVisibility(View.VISIBLE);
        

        btnPlayAgain.setVisibility(View.VISIBLE);
        actionText.setVisibility(View.INVISIBLE);
        

        btnRock.setVisibility(View.GONE);
        btnPaper.setVisibility(View.GONE);
        btnScissors.setVisibility(View.GONE);


        if (playerScore > 0) {
            ScoreManager.saveScore("RPS", playerScore, null); // Fire and forget
        }
    }

    private void resetRound() {
        isAnimating = false;
        
        playerMoveText.setText(MOVE_QUESTION);
        cpuMoveText.setText(MOVE_QUESTION);
        resultText.setVisibility(View.INVISIBLE);
        
        btnPlayAgain.setVisibility(View.GONE);
        actionText.setVisibility(View.VISIBLE);
        actionText.setText("Choose your move!");
        
        btnRock.setVisibility(View.VISIBLE);
        btnPaper.setVisibility(View.VISIBLE);
        btnScissors.setVisibility(View.VISIBLE);
        setButtonsEnabled(true);
    }

    private void updateScoreDisplay() {
        playerScoreText.setText(String.valueOf(playerScore));
        cpuScoreText.setText(String.valueOf(cpuScore));
    }

    private void setButtonsEnabled(boolean enabled) {
        btnRock.setEnabled(enabled);
        btnPaper.setEnabled(enabled);
        btnScissors.setEnabled(enabled);
    }
}
