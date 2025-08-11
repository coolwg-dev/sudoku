package com.LKCC.sudoku;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SudokuBoardView.SudokuListener {
    private Handler timerHandler = new Handler();
    private int secondsElapsed = 0;
    private boolean isTimerRunning = false;
    private TextView tvTimer;
    private TextView tvFinished;
    private int difficulty = 0;
    private int score = 0;
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            int minutes = secondsElapsed / 60;
            int seconds = secondsElapsed % 60;
            tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
            // Only update timer, do not change score here
            if (isTimerRunning) {
                secondsElapsed++;
                timerHandler.postDelayed(this, 1000);
            }
        }
    };
    public enum GameMode {
        EASY, MEDIUM, HARD
    }
    private GameMode mode = GameMode.EASY;
    private int mistakes = 0;
    private TextView tvMistakes;
    private TextView tvScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get difficulty from intent
        difficulty = getIntent().getIntExtra("difficulty", 0);
        switch (difficulty) {
            case 0:
                mode = GameMode.EASY;
                break;
            case 1:
                mode = GameMode.MEDIUM;
                break;
            case 2:
                mode = GameMode.HARD;
                break;
            default:
                mode = GameMode.EASY;
                break;
        }

        tvTimer = findViewById(R.id.tvTimer);
        tvFinished = findViewById(R.id.tvFinished);
        tvFinished.setVisibility(View.GONE);
        tvMistakes = findViewById(R.id.tvMistakes);
        tvScore = findViewById(R.id.tvScore);
        SudokuBoardView sudokuBoard = findViewById(R.id.sudokuBoard);
        sudokuBoard.setSudokuListener(this);
        // Set clues based on mode
        int clues = 50;
        switch (mode) {
            case EASY:
                clues = 50;
                break;
            case MEDIUM:
                clues = 40;
                break;
            case HARD:
                clues = 30;
                break;
        }
        sudokuBoard.setClues(clues);
        sudokuBoard.generateNewPuzzle();
        int[] buttonIds = {R.id.btnNum1, R.id.btnNum2, R.id.btnNum3, R.id.btnNum4, R.id.btnNum5, R.id.btnNum6, R.id.btnNum7, R.id.btnNum8, R.id.btnNum9};
        for (int i = 0; i < buttonIds.length; i++) {
            final int number = i + 1;
            TextView btn = findViewById(buttonIds[i]);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sudokuBoard.setNumber(number);
                }
            });
        }
        Button btnHint = findViewById(R.id.btnHint);
        btnHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sudokuBoard.showHint();
            }
        });

        // Add erase button functionality
        View btnErase = findViewById(R.id.btnErase);
        if (btnErase != null) {
            btnErase.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sudokuBoard.eraseCell();
                }
            });
        }

        // Add back button functionality
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        Button btnBackHome = findViewById(R.id.btnBackHome);
        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        // Start timer when game begins
        isTimerRunning = true;
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    @Override
    public void onGameCompleted() {
        isTimerRunning = false;
        if (tvFinished != null) {
            tvFinished.setVisibility(View.VISIBLE);
        }
    }

    // Call this method whenever a mistake is made
    public void onMistake() {
        mistakes++;
        if (tvMistakes != null) {
            tvMistakes.setText("Mistakes: " + mistakes + "/3");
        }
    }

    // Call this method whenever a box is clicked correctly
    public void onCorrectBoxClick() {
        // Logarithmic score calculation based on elapsed time
        int addScore = Math.max(200, (int)(5000 - 1000 * Math.log1p(secondsElapsed)));
        score += addScore;
        if (tvScore != null) {
            tvScore.setText("Score: " + score);
        }
    }

    // Add this new method to handle when no cell is selected
    @Override
    public void onNoCellSelected() {
        Toast.makeText(this, "Please select a cell first", Toast.LENGTH_SHORT).show();
    }
}
