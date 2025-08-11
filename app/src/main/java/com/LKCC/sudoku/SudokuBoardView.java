package com.LKCC.sudoku;

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
    private Paint linePaint, textPaint, selectedPaint, relatedPaint, wrongPaint, matchPaint;
    private Random random = new Random();
    private boolean highlightWrong = false;
    private boolean gameCompleted = false;
    private SudokuListener sudokuListener;
    private int clues = 30; // Default

    public interface SudokuListener {
        void onGameCompleted();
        void onCorrectBoxClick();
        void onMistake();
        void onNoCellSelected(); // Added for no cell selected feedback
    }

    public void setSudokuListener(SudokuListener listener) {
        this.sudokuListener = listener;
    }

    public void setNumber(int number) {
        if (selectedRow != -1 && selectedCol != -1 && !gameCompleted) {
            // Check if the cell is not a clue (initial cell)
            if (initialBoard[selectedRow][selectedCol] != 0) {
                // This is a clue cell, can't be modified
                return;
            }

            if (solution[selectedRow][selectedCol] == number) {
                board[selectedRow][selectedCol] = number;
                if (sudokuListener != null) {
                    sudokuListener.onCorrectBoxClick();
                }
            } else {
                if (sudokuListener != null) {
                    sudokuListener.onMistake();
                }
            }
            invalidate();
            if (isBoardCompleteAndCorrect()) {
                gameCompleted = true;
                if (sudokuListener != null) {
                    sudokuListener.onGameCompleted();
                }
            }
        } else if (selectedRow == -1 || selectedCol == -1) {
            // Provide feedback that no cell is selected
            if (sudokuListener != null) {
                sudokuListener.onNoCellSelected();
            }
        }
    }

    public void setClues(int clues) {
        this.clues = clues;
    }

    public SudokuBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        // Do not generate puzzle here
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
        relatedPaint = new Paint();
        relatedPaint.setColor(Color.LTGRAY);
        wrongPaint = new Paint();
        wrongPaint.setColor(Color.RED);
        wrongPaint.setTextSize(64);
        wrongPaint.setTextAlign(Paint.Align.CENTER);
        matchPaint = new Paint();
        matchPaint.setColor(Color.parseColor("#90CAF9")); // Light blue for matching numbers
        matchPaint.setTextSize(64);
        matchPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int cellSize = Math.min(width, height) / 9;
        // Highlight related cells in grey
        boolean[][] related = new boolean[9][9];
        int selectedNumber = -1;
        if (selectedRow != -1 && selectedCol != -1 && board[selectedRow][selectedCol] != 0) {
            selectedNumber = board[selectedRow][selectedCol];
            // Highlight row
            for (int c = 0; c < 9; c++) related[selectedRow][c] = true;
            // Highlight column
            for (int r = 0; r < 9; r++) related[r][selectedCol] = true;
            // Highlight 3x3 box
            int boxRow = (selectedRow / 3) * 3;
            int boxCol = (selectedCol / 3) * 3;
            for (int r = boxRow; r < boxRow + 3; r++)
                for (int c = boxCol; c < boxCol + 3; c++) related[r][c] = true;
            // Draw grey highlight
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (related[r][c]) {
                        canvas.drawRect(c * cellSize, r * cellSize,
                            (c + 1) * cellSize, (r + 1) * cellSize, relatedPaint);
                    }
                }
            }
        }
        // Highlight selected cell FIRST
        if (selectedRow != -1 && selectedCol != -1) {
            canvas.drawRect(selectedCol * cellSize, selectedRow * cellSize,
                    (selectedCol + 1) * cellSize, (selectedRow + 1) * cellSize, selectedPaint);
        }
        // Draw grid
        for (int i = 0; i <= 9; i++) {
            canvas.drawLine(i * cellSize, 0, i * cellSize, cellSize * 9, linePaint);
            canvas.drawLine(0, i * cellSize, cellSize * 9, i * cellSize, linePaint);
        }
        // Draw numbers
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] != 0) {
                    float x = c * cellSize + cellSize / 2f;
                    float y = r * cellSize + cellSize / 1.5f;
                    if (related[r][c] && board[r][c] == selectedNumber && !(r == selectedRow && c == selectedCol)) {
                        canvas.drawText(String.valueOf(board[r][c]), x, y, matchPaint);
                    } else if (board[r][c] != solution[r][c]) {
                        canvas.drawText(String.valueOf(board[r][c]), x, y, wrongPaint);
                    } else {
                        canvas.drawText(String.valueOf(board[r][c]), x, y, textPaint);
                    }
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int cellSize = Math.min(getWidth(), getHeight()) / 9;
            int col = (int) (event.getX() / cellSize);
            int row = (int) (event.getY() / cellSize);
            if (row >= 0 && row < 9 && col >= 0 && col < 9) {
                selectedRow = row;
                selectedCol = col;
                invalidate();
            }
        }
        return true;
    }

    // Remove numbers from the solution to create a playable puzzle
    private void removeNumbers(int[][] board, int clues) {
        // Create a list of all cell positions
        ArrayList<int[]> cells = new ArrayList<>();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                cells.add(new int[]{r, c});
            }
        }
        Collections.shuffle(cells, random);
        int cellsToRemove = 81 - clues;
        for (int i = 0; i < cellsToRemove; i++) {
            int[] pos = cells.get(i);
            board[pos[0]][pos[1]] = 0;
        }
    }

    // Backtracking Sudoku solver to fill the board with a valid solution
    private boolean fillBoard(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    ArrayList<Integer> numbers = new ArrayList<>();
                    for (int n = 1; n <= 9; n++) numbers.add(n);
                    Collections.shuffle(numbers, random);
                    for (int number : numbers) {
                        if (isSafe(board, row, col, number)) {
                            board[row][col] = number;
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
        int startRow = row - row % 3, startCol = col - col % 3;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[startRow + i][startCol + j] == num) return false;
        return true;
    }

    // Update generateRandomSudoku to use removeNumbers
    private void generateRandomSudoku() {
        // Clear all arrays first
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                solution[r][c] = 0;
                board[r][c] = 0;
                initialBoard[r][c] = 0;
            }
        }

        // Fill the solution board with a valid Sudoku
        fillBoard(solution);

        // Copy solution to board
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                board[r][c] = solution[r][c];
            }
        }

        // Remove numbers to create the puzzle
        removeNumbers(board, clues);

        // Save the puzzle state as initial board (clues)
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                initialBoard[r][c] = board[r][c];
            }
        }
    }

    // Add hint logic
    public void showHint() {
        if (selectedRow != -1 && selectedCol != -1 && board[selectedRow][selectedCol] == 0) {
            board[selectedRow][selectedCol] = solution[selectedRow][selectedCol];
            invalidate();
        }
    }

    // Add erase function
    public void eraseCell() {
        if (selectedRow != -1 && selectedCol != -1 && !gameCompleted) {
            // Check if the cell is not a clue (initial cell)
            if (initialBoard[selectedRow][selectedCol] != 0) {
                // This is a clue cell, can't be modified
                return;
            }
            board[selectedRow][selectedCol] = 0;
            invalidate();
        } else if (selectedRow == -1 || selectedCol == -1) {
            // Provide feedback that no cell is selected
            if (sudokuListener != null) {
                sudokuListener.onNoCellSelected();
            }
        }
    }

    public void generateNewPuzzle() {
        // Reset game state
        gameCompleted = false;
        selectedRow = -1;
        selectedCol = -1;
        generateRandomSudoku();
        // Copy the puzzle state to initialBoard after removing numbers
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                initialBoard[r][c] = board[r][c];
            }
        }
        invalidate();
    }

    private boolean isBoardCompleteAndCorrect() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] == 0 || board[r][c] != solution[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }
}
