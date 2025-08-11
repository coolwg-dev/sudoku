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
    private TextView tvDifficulty;
    private TextView tvPencilon;
    private TextView tvfastPencilon;
    private SudokuBoardView sudokuBoard;
    private TextView[] numberCountViews = new TextView[9]; // Array to hold the small number count TextViews
    private TextView[] numberButtons = new TextView[9]; // Array to hold the number buttons

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get difficulty from intent
        difficulty = getIntent().getIntExtra("difficulty", 0);
        switch (difficulty) {
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
        tvDifficulty = findViewById(R.id.tvDifficulty);
        tvPencilon = findViewById(R.id.tvPencilOn);
        tvfastPencilon = findViewById(R.id.tvFastPencilOn);

        // Initialize score to 0
        score = 0;
        tvScore.setText("Score: " + score);

        // Initialize mistakes to 0
        mistakes = 0;
        tvMistakes.setText("Mistakes: " + mistakes + "/3");

        // Set pencil mode indicators to OFF (default state)
        tvPencilon.setText("OFF");
        tvfastPencilon.setText("OFF");

        // Set difficulty display to match the game difficulty
        switch (mode) {
            case EASY:
                tvDifficulty.setText("Easy");
                break;
            case MEDIUM:
                tvDifficulty.setText("Medium");
                break;
            case HARD:
                tvDifficulty.setText("Hard");
                break;
        }

        sudokuBoard = findViewById(R.id.sudokuBoard);
        sudokuBoard.setSudokuListener(this);

        // Initialize number count views
        int[] smallButtonIds = {R.id.btnNum1Small, R.id.btnNum2Small, R.id.btnNum3Small,
                               R.id.btnNum4Small, R.id.btnNum5Small, R.id.btnNum6Small,
                               R.id.btnNum7Small, R.id.btnNum8Small, R.id.btnNum9Small};
        for (int i = 0; i < smallButtonIds.length; i++) {
            numberCountViews[i] = findViewById(smallButtonIds[i]);
        }

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

        // Update number counts after puzzle generation
        updateNumberCounts();

        int[] buttonIds = {R.id.btnNum1, R.id.btnNum2, R.id.btnNum3, R.id.btnNum4, R.id.btnNum5, R.id.btnNum6, R.id.btnNum7, R.id.btnNum8, R.id.btnNum9};
        for (int i = 0; i < buttonIds.length; i++) {
            final int number = i + 1;
            TextView btn = findViewById(buttonIds[i]);
            numberButtons[i] = btn; // Initialize the number button in the array
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

        View pencilmode = findViewById(R.id.btnPencil);
        if(pencilmode!=null){
            pencilmode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sudokuBoard.togglePencilMode();
                    tvPencilon.setText(sudokuBoard.isPencilMode() ? "ON" : "OFF");
                }
            });
        }

        View fastpencilmode = findViewById(R.id.btnFastPencil);
        if(fastpencilmode!=null){
            fastpencilmode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sudokuBoard.togglefastPencilMode();
                    tvfastPencilon.setText(sudokuBoard.isfastPencilMode() ? "ON" : "OFF");
                }
            });
        }

        // Add undo button functionality
        View btnUndo = findViewById(R.id.btnUndo);
        if (btnUndo != null) {
            btnUndo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sudokuBoard.undoLastMove();
                }
            });
        }

        // Start timer when game begins
        isTimerRunning = true;
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    // Add method to update number counts
    private void updateNumberCounts() {
        if (sudokuBoard != null) {
            int[] remainingCounts = sudokuBoard.getAllRemainingCounts();
            for (int i = 0; i < 9; i++) {
                if (numberCountViews[i] != null) {
                    if (remainingCounts[i] > 0) {
                        numberCountViews[i].setText(String.valueOf(remainingCounts[i]));
                    } else {
                        numberCountViews[i].setText("");
                    }
                }

                // Update number button appearance and clickability
                if (numberButtons[i] != null) {
                    if (remainingCounts[i] <= 0) {
                        // Make button transparent and unclickable when no numbers left
                        numberButtons[i].setAlpha(0f);
                        numberButtons[i].setClickable(false);
                    } else {
                        // Restore normal appearance and clickability
                        numberButtons[i].setAlpha(1f);
                        numberButtons[i].setClickable(true);
                    }
                }
            }
        }
    }

    @Override
    public void onGameCompleted() {
        isTimerRunning = false;
        if (tvFinished != null) {
            tvFinished.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCorrectBoxClick() {
        score += Math.max(200,(int)(2000-1000*Math.log1p((double)secondsElapsed/10))); // Add 10 points for correct placement
        tvScore.setText("Score: " + score);
        updateNumberCounts(); // Update number counts when board changes
    }

    @Override
    public void onMistake() {
        mistakes++;
        tvMistakes.setText("Mistakes: " + mistakes + "/3");
        if (mistakes >= 3) {
            // Game over logic can be added here
            Toast.makeText(this, "Game Over! Too many mistakes.", Toast.LENGTH_SHORT).show();
        }
        updateNumberCounts(); // Update number counts when board changes
    }

    // Add this new method to handle when no cell is selected
    @Override
    public void onNoCellSelected() {
        Toast.makeText(this, "Please select a cell first", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBoardChanged() {
        updateNumberCounts(); // Update number counts whenever board changes
    }
}
