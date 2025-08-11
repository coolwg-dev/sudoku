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
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            int minutes = secondsElapsed / 60;
            int seconds = secondsElapsed % 60;
            tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTimer = findViewById(R.id.tvTimer);
        tvFinished = findViewById(R.id.tvFinished);
        tvFinished.setVisibility(View.GONE);
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
        int[] buttonIds = {R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int i = 0; i < buttonIds.length; i++) {
            final int number = i + 1;
            Button btn = findViewById(buttonIds[i]);
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
        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
}
