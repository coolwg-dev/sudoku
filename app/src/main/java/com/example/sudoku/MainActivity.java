package com.example.sudoku;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTimer = findViewById(R.id.tvTimer);
        SudokuBoardView sudokuBoard = findViewById(R.id.sudokuBoard);
        sudokuBoard.setSudokuListener(this);
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
        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to HomeActivity
            }
        });
        // Get difficulty from intent
        difficulty = getIntent().getIntExtra("difficulty", 0);
        sudokuBoard.setDifficulty(difficulty); // You must implement this in SudokuBoardView
        // Start timer
        isTimerRunning = true;
        secondsElapsed = 0;
        timerHandler.post(timerRunnable);
    }

    @Override
    public void onNumberWrong() {
        Toast.makeText(this, "Wrong number!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSudokuCompleted() {
        isTimerRunning = false;
        Toast.makeText(this, "Congratulations! Sudoku completed!", Toast.LENGTH_LONG).show();
        saveHistory();
    }

    private void saveHistory() {
        SharedPreferences prefs = getSharedPreferences("sudoku_history", MODE_PRIVATE);
        String history = prefs.getString("history", "");
        String[] modes = {"Easy", "Medium", "Hard"};
        int minutes = secondsElapsed / 60;
        int seconds = secondsElapsed % 60;
        String timeStr = String.format("%02d:%02d", minutes, seconds);
        String record = String.format("%s | %s", modes[difficulty], timeStr);
        history = record + "\n" + history;
        prefs.edit().putString("history", history).apply();
    }
}
