package com.example.gamehub_m.ui.games;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.gamehub_m.R;
import com.example.gamehub_m.data.ScoreManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToeActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView playerScoreText, cpuScoreText, statusText;
    private Button btnReset;
    private AppCompatButton[][] buttons = new AppCompatButton[3][3];

    private boolean playerTurn = true;
    private int roundCount = 0;
    private int playerScore = 0;
    private int cpuScore = 0;
    private boolean gameActive = true;

    private static final String PLAYER_SYMBOL = "X";
    private static final String CPU_SYMBOL = "O";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tictactoe);

        initViews();
        setupGame();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        playerScoreText = findViewById(R.id.playerScoreText);
        cpuScoreText = findViewById(R.id.cpuScoreText);
        statusText = findViewById(R.id.statusText);
        btnReset = findViewById(R.id.btnReset);

        btnBack.setOnClickListener(v -> finish());
        btnReset.setOnClickListener(v -> resetGame());


        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                String buttonID = "cell_" + i + j;
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                buttons[i][j] = findViewById(resID);
                
                final int r = i;
                final int c = j;
                buttons[i][j].setOnClickListener(v -> onCellClicked(r, c));
            }
        }
    }

    private void setupGame() {
        resetBoard();
    }

    private void onCellClicked(int r, int c) {
        if (!gameActive || !buttons[r][c].getText().toString().equals("") || !playerTurn) {
            return;
        }

        makeMove(r, c, PLAYER_SYMBOL);
        
        if (checkWin(PLAYER_SYMBOL)) {
            playerWins();
        } else if (roundCount == 9) {
            draw();
        } else {
            playerTurn = false;
            statusText.setText("CPU's Turn...");
            new Handler(Looper.getMainLooper()).postDelayed(this::cpuMove, 700);
        }
    }

    private void cpuMove() {
        if (!gameActive) return;


        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().toString().equals("")) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            int[] move = emptyCells.get(new Random().nextInt(emptyCells.size()));
            makeMove(move[0], move[1], CPU_SYMBOL);

            if (checkWin(CPU_SYMBOL)) {
                cpuWins();
            } else if (roundCount == 9) {
                draw();
            } else {
                playerTurn = true;
                statusText.setText("Your Turn");
            }
        }
    }

    private void makeMove(int r, int c, String symbol) {
        buttons[r][c].setText(symbol);
        roundCount++;

        if (symbol.equals(PLAYER_SYMBOL)) {
            buttons[r][c].setTextColor(ContextCompat.getColor(this, R.color.secondary));
        } else {
            buttons[r][c].setTextColor(0xFFFF5252); // Red
        }
    }

    private boolean checkWin(String symbol) {

        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().toString().equals(symbol) &&
                buttons[i][1].getText().toString().equals(symbol) &&
                buttons[i][2].getText().toString().equals(symbol)) {
                return true;
            }
            if (buttons[0][i].getText().toString().equals(symbol) &&
                buttons[1][i].getText().toString().equals(symbol) &&
                buttons[2][i].getText().toString().equals(symbol)) {
                return true;
            }
        }


        if (buttons[0][0].getText().toString().equals(symbol) &&
            buttons[1][1].getText().toString().equals(symbol) &&
            buttons[2][2].getText().toString().equals(symbol)) {
            return true;
        }
        if (buttons[0][2].getText().toString().equals(symbol) &&
            buttons[1][1].getText().toString().equals(symbol) &&
            buttons[2][0].getText().toString().equals(symbol)) {
            return true;
        }

        return false;
    }

    private void playerWins() {
        gameActive = false;
        playerScore++;
        updateScore();
        statusText.setText("YOU WIN!");
        statusText.setTextColor(ContextCompat.getColor(this, R.color.success));
        btnReset.setVisibility(View.VISIBLE);
        

        ScoreManager.saveScore("TicTacToe", playerScore, null);
    }

    private void cpuWins() {
        gameActive = false;
        cpuScore++;
        updateScore();
        statusText.setText("CPU WINS!");
        statusText.setTextColor(0xFFFF5252);
        btnReset.setVisibility(View.VISIBLE);
    }

    private void draw() {
        gameActive = false;
        statusText.setText("DRAW!");
        statusText.setTextColor(ContextCompat.getColor(this, R.color.white));
        btnReset.setVisibility(View.VISIBLE);
    }

    private void updateScore() {
        playerScoreText.setText(String.valueOf(playerScore));
        cpuScoreText.setText(String.valueOf(cpuScore));
    }

    private void resetGame() {
        resetBoard();
        gameActive = true;
        playerTurn = true;
        roundCount = 0;
        statusText.setText("Your Turn");
        statusText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        btnReset.setVisibility(View.GONE);
    }

    private void resetBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
            }
        }
    }
}
