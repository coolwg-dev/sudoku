package com.example.sudoku;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SudokuBoardView.SudokuListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
    }

    @Override
    public void onNumberWrong() {
        Toast.makeText(this, "Wrong number!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSudokuCompleted() {
        Toast.makeText(this, "Congratulations! Sudoku completed!", Toast.LENGTH_LONG).show();
    }
}
