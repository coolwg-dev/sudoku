package com.example.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;
import java.util.Collections;
import java.util.ArrayList;

public class SudokuBoardView extends View {
    private int[][] board = new int[9][9];
    private int[][] initialBoard = new int[9][9];
    private int[][] solution = new int[9][9];
    private int selectedRow = -1, selectedCol = -1;
    private Paint linePaint, textPaint, selectedPaint;
    private Random random = new Random();
    private boolean highlightWrong = false;
    private boolean gameCompleted = false;

    public SudokuBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        generateRandomSudoku();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(4);
        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(64);
        textPaint.setTextAlign(Paint.Align.CENTER);
        selectedPaint = new Paint();
        selectedPaint.setColor(Color.YELLOW);
    }

    private void generateRandomSudoku() {
        // Generate a full valid solution
        fillBoard(solution);
        // Copy solution to initialBoard
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                initialBoard[r][c] = solution[r][c];
        // Remove numbers to create a puzzle
        removeNumbers(initialBoard, 40); // Remove 40 cells for medium difficulty
        // Copy initialBoard to board
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                board[r][c] = initialBoard[r][c];
    }

    // Backtracking Sudoku generator
    private boolean fillBoard(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    ArrayList<Integer> nums = new ArrayList<>();
                    for (int n = 1; n <= 9; n++) nums.add(n);
                    Collections.shuffle(nums, random);
                    for (int num : nums) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num;
                            if (fillBoard(board)) return true;
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isSafe(int[][] board, int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == num || board[i][col] == num) return false;
        }
        int boxRow = row - row % 3, boxCol = col - col % 3;
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (board[boxRow + r][boxCol + c] == num) return false;
        return true;
    }

    // Remove numbers to create a puzzle
    private void removeNumbers(int[][] board, int count) {
        int removed = 0;
        while (removed < count) {
            int r = random.nextInt(9);
            int c = random.nextInt(9);
            if (board[r][c] != 0) {
                int backup = board[r][c];
                board[r][c] = 0;
                // Optionally: check for unique solution here
                removed++;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int cellSize = Math.min(width, height) / 9;
        // If game completed, draw blue overlay
        if (gameCompleted) {
            Paint bluePaint = new Paint();
            bluePaint.setColor(Color.parseColor("#4488FF"));
            bluePaint.setAlpha(120);
            canvas.drawRect(0, 0, cellSize * 9, cellSize * 9, bluePaint);
        }
        // Draw selected cell (yellow or red if wrong)
        if (selectedRow != -1 && selectedCol != -1) {
            if (highlightWrong) {
                Paint wrongPaint = new Paint();
                wrongPaint.setColor(Color.RED);
                canvas.drawRect(selectedCol * cellSize, selectedRow * cellSize,
                        (selectedCol + 1) * cellSize, (selectedRow + 1) * cellSize, wrongPaint);
            } else {
                canvas.drawRect(selectedCol * cellSize, selectedRow * cellSize,
                        (selectedCol + 1) * cellSize, (selectedRow + 1) * cellSize, selectedPaint);
            }
        }
        // Draw numbers
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] != 0) {
                    float x = c * cellSize + cellSize / 2f;
                    float y = r * cellSize + cellSize / 1.5f;
                    canvas.drawText(String.valueOf(board[r][c]), x, y, textPaint);
                }
            }
        }
        // Draw grid
        for (int i = 0; i <= 9; i++) {
            float stroke = (i % 3 == 0) ? 8 : 2;
            linePaint.setStrokeWidth(stroke);
            // Vertical
            canvas.drawLine(i * cellSize, 0, i * cellSize, cellSize * 9, linePaint);
            // Horizontal
            canvas.drawLine(0, i * cellSize, cellSize * 9, i * cellSize, linePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int cellSize = getWidth() / 9;
            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);
            if (row >= 0 && row < 9 && col >= 0 && col < 9) {
                selectedRow = row;
                selectedCol = col;
                invalidate();
            }
            return true;
        }
        return false;
    }

    public void setNumber(int number) {
        if (selectedRow != -1 && selectedCol != -1) {
            if (initialBoard[selectedRow][selectedCol] == 0) {
                board[selectedRow][selectedCol] = number;
                if (number != solution[selectedRow][selectedCol]) {
                    highlightWrong = true;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            highlightWrong = false;
                            invalidate();
                        }
                    }, 500);
                    if (listener != null) listener.onNumberWrong();
                } else {
                    highlightWrong = false;
                    if (isBoardFull() && checkSolution()) {
                        gameCompleted = true;
                        if (listener != null) listener.onSudokuCompleted();
                    }
                }
                invalidate();
            }
        }
    }
    private boolean isBoardFull() {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (board[r][c] == 0)
                    return false;
        return true;
    }

    public boolean checkSolution() {
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                if (board[r][c] != solution[r][c])
                    return false;
        return true;
    }

    public void setDifficulty(int difficulty) {
        // 0: Easy, 1: Medium, 2: Hard
        int removeCount = 40;
        if (difficulty == 0) removeCount = 30;
        else if (difficulty == 1) removeCount = 40;
        else if (difficulty == 2) removeCount = 55;
        // Generate new puzzle
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++) {
                board[r][c] = 0;
                initialBoard[r][c] = 0;
                solution[r][c] = 0;
            }
        fillBoard(solution);
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                initialBoard[r][c] = solution[r][c];
        removeNumbers(initialBoard, removeCount);
        for (int r = 0; r < 9; r++)
            for (int c = 0; c < 9; c++)
                board[r][c] = initialBoard[r][c];
        gameCompleted = false;
        selectedRow = -1;
        selectedCol = -1;
        invalidate();
    }

    public interface SudokuListener {
        void onNumberWrong();
        void onSudokuCompleted();
    }
    private SudokuListener listener;
    public void setSudokuListener(SudokuListener listener) {
        this.listener = listener;
    }
}
