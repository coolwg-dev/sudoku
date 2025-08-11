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
    private boolean[][][] pencilMarks = new boolean[9][9][10]; // [row][col][number] - index 0 unused, 1-9 for numbers
    private int selectedRow = -1, selectedCol = -1;
    private Paint linePaint, majorLinePaint, textPaint, selectedPaint, relatedPaint, wrongPaint, matchPaint, userInputPaint, pencilPaint;
    private Random random = new Random();
    private boolean highlightWrong = false;
    private boolean gameCompleted = false;
    private SudokuListener sudokuListener;
    private int clues = 30; // Default
    private boolean pencilMode = false; // Track if we're in pencil mode
    private boolean fastPencilMode = false; // Track if we're in fast pencil mode
    public interface SudokuListener {
        void onGameCompleted();
        void onCorrectBoxClick();
        void onMistake();
        void onNoCellSelected(); // Added for no cell selected feedback
        void onBoardChanged(); // Add this method for updating number counts
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

            if (pencilMode) {
                // In pencil mode, only add marks for numbers that are valid (allowed) in this position
                if (isNumberAllowed(selectedRow, selectedCol, number)) {
                    // Toggle pencil mark
                    pencilMarks[selectedRow][selectedCol][number] = !pencilMarks[selectedRow][selectedCol][number];
                    invalidate();
                }
                // No mistakes counted in pencil mode
                return;
            }

            // Clear pencil marks when placing a number
            for (int n = 1; n <= 9; n++) {
                pencilMarks[selectedRow][selectedCol][n] = false;
            }

            // Always place the number on the board
            board[selectedRow][selectedCol] = number;

            // If fast pencil mode is enabled, update all related pencil marks
            if (fastPencilMode) {
                updatePencilMarksAfterNumberPlacement(selectedRow, selectedCol, number);
            }

            // Check if it's correct for scoring/feedback
            if (solution[selectedRow][selectedCol] == number) {
                if (sudokuListener != null) {
                    sudokuListener.onCorrectBoxClick();
                }
            } else {
                if (sudokuListener != null) {
                    sudokuListener.onMistake();
                }
            }

            // Notify that board has changed
            if (sudokuListener != null) {
                sudokuListener.onBoardChanged();
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
        // Minor grid lines (within 3x3 sections)
        linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(2);

        // Major grid lines (3x3 section boundaries)
        majorLinePaint = new Paint();
        majorLinePaint.setColor(Color.BLACK);
        majorLinePaint.setStrokeWidth(6);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(64);
        textPaint.setTextAlign(Paint.Align.CENTER);
        selectedPaint = new Paint();
        selectedPaint.setColor(Color.YELLOW);
        relatedPaint = new Paint();
        relatedPaint.setColor(Color.LTGRAY);
        wrongPaint = new Paint();
        wrongPaint.setColor(Color.parseColor("#D32F2F")); // Darker, more visible red
        wrongPaint.setTextSize(64);
        wrongPaint.setTextAlign(Paint.Align.CENTER);
        wrongPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD); // Make wrong numbers bold
        matchPaint = new Paint();
        matchPaint.setColor(Color.RED);
        matchPaint.setTextSize(64);
        matchPaint.setTextAlign(Paint.Align.CENTER);
        userInputPaint = new Paint();
        userInputPaint.setColor(Color.BLUE); // Blue color for user input numbers
        userInputPaint.setTextSize(64);
        userInputPaint.setTextAlign(Paint.Align.CENTER);
        pencilPaint = new Paint();
        pencilPaint.setColor(Color.GRAY); // Color for pencil marks
        pencilPaint.setTextSize(32);
        pencilPaint.setTextAlign(Paint.Align.CENTER);
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
        // Draw grid lines
        // First draw all minor grid lines (thin lines for individual cells)
        for (int i = 0; i <= 9; i++) {
            // Skip lines that will be drawn as major lines
            if (i % 3 != 0) {
                canvas.drawLine(i * cellSize, 0, i * cellSize, cellSize * 9, linePaint);
                canvas.drawLine(0, i * cellSize, cellSize * 9, i * cellSize, linePaint);
            }
        }

        // Then draw major grid lines (thick lines for 3x3 section boundaries)
        for (int i = 0; i <= 9; i += 3) {
            canvas.drawLine(i * cellSize, 0, i * cellSize, cellSize * 9, majorLinePaint);
            canvas.drawLine(0, i * cellSize, cellSize * 9, i * cellSize, majorLinePaint);
        }
        // Draw numbers and pencil marks
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] != 0) {
                    float x = c * cellSize + cellSize / 2f;
                    float y = r * cellSize + cellSize / 1.5f;

                    // Check if this is a user-input number (not an initial clue)
                    boolean isUserInput = (initialBoard[r][c] == 0);

                    if (related[r][c] && board[r][c] == selectedNumber && !(r == selectedRow && c == selectedCol)) {
                        canvas.drawText(String.valueOf(board[r][c]), x, y, matchPaint);
                    } else if (board[r][c] != solution[r][c]) {
                        canvas.drawText(String.valueOf(board[r][c]), x, y, wrongPaint);
                    } else if (isUserInput) {
                        canvas.drawText(String.valueOf(board[r][c]), x, y, userInputPaint);
                    } else {
                        canvas.drawText(String.valueOf(board[r][c]), x, y, textPaint);
                    }
                }

                // Draw pencil marks only if cell is empty
                if (board[r][c] == 0) {
                    for (int n = 1; n <= 9; n++) {
                        if (pencilMarks[r][c][n]) {
                            float x, y;
                            // Position pencil marks according to specification:
                            // 1 = upper left, 2 = upper middle, 3 = upper right
                            // 4 = middle left, 5 = center, 6 = middle right
                            // 7 = lower left, 8 = lower middle, 9 = lower right
                            switch (n) {
                                case 1: // upper left
                                    x = c * cellSize + cellSize * 0.2f;
                                    y = r * cellSize + cellSize * 0.3f;
                                    break;
                                case 2: // upper middle
                                    x = c * cellSize + cellSize * 0.5f;
                                    y = r * cellSize + cellSize * 0.3f;
                                    break;
                                case 3: // upper right
                                    x = c * cellSize + cellSize * 0.8f;
                                    y = r * cellSize + cellSize * 0.3f;
                                    break;
                                case 4: // middle left
                                    x = c * cellSize + cellSize * 0.2f;
                                    y = r * cellSize + cellSize * 0.5f;
                                    break;
                                case 5: // center
                                    x = c * cellSize + cellSize * 0.5f;
                                    y = r * cellSize + cellSize * 0.5f;
                                    break;
                                case 6: // middle right
                                    x = c * cellSize + cellSize * 0.8f;
                                    y = r * cellSize + cellSize * 0.5f;
                                    break;
                                case 7: // lower left
                                    x = c * cellSize + cellSize * 0.2f;
                                    y = r * cellSize + cellSize * 0.8f;
                                    break;
                                case 8: // lower middle
                                    x = c * cellSize + cellSize * 0.5f;
                                    y = r * cellSize + cellSize * 0.8f;
                                    break;
                                case 9: // lower right
                                    x = c * cellSize + cellSize * 0.8f;
                                    y = r * cellSize + cellSize * 0.8f;
                                    break;
                                default:
                                    x = c * cellSize + cellSize / 2f;
                                    y = r * cellSize + cellSize / 2f;
                                    break;
                            }
                            canvas.drawText(String.valueOf(n), x, y, pencilPaint);
                        }
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

            // Notify that board has changed
            if (sudokuListener != null) {
                sudokuListener.onBoardChanged();
            }

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

            // Store the number that was erased for fast pencil mode updates
            int erasedNumber = board[selectedRow][selectedCol];

            // Clear the number
            board[selectedRow][selectedCol] = 0;

            // Clear any pencil marks for this cell
            for (int n = 1; n <= 9; n++) {
                pencilMarks[selectedRow][selectedCol][n] = false;
            }

            // If fast pencil mode is enabled, update pencil marks after erasing
            if (fastPencilMode && erasedNumber != 0) {
                updatePencilMarksAfterErase(selectedRow, selectedCol, erasedNumber);
            }

            // Notify that board has changed
            if (sudokuListener != null) {
                sudokuListener.onBoardChanged();
            }

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

    // Method to get the remaining count for a specific number
    public int getRemainingCount(int number) {
        int count = 0;
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (solution[row][col] == number && board[row][col] != number) {
                    count++;
                }
            }
        }
        return count;
    }

    // Method to get all remaining counts
    public int[] getAllRemainingCounts() {
        int[] counts = new int[9];
        for (int num = 1; num <= 9; num++) {
            counts[num - 1] = getRemainingCount(num);
        }
        return counts;
    }

    // Toggle pencil mode
    public void togglePencilMode() {
        pencilMode = !pencilMode;
        invalidate();
    }

    // Get pencil mode status
    public boolean isPencilMode() {
        return pencilMode;
    }

    // Set pencil mode
    public void setPencilMode(boolean enabled) {
        pencilMode = enabled;
        invalidate();
    }

    public void togglefastPencilMode() {
        fastPencilMode = !fastPencilMode;

        if (fastPencilMode) {
            // When turning on fast pencil mode, automatically fill all empty cells with valid pencil marks
            fillAllPencilMarks();
        } else {
            // When turning off fast pencil mode, clear all pencil marks
            clearAllPencilMarks();
        }

        invalidate();
    }
    public boolean isfastPencilMode() {
        return fastPencilMode;
    }

    // Clear pencil marks from the selected cell
    public void clearPencilMarks(int row, int col) {
        if (row != -1 && col != -1) {
            for (int n = 1; n <= 9; n++) {
                pencilMarks[row][col][n] = false;
            }

            // Notify that board has changed
            if (sudokuListener != null) {
                sudokuListener.onBoardChanged();
            }

            invalidate();
        }
    }

    // Method to fill all empty cells with valid pencil marks
    private void fillAllPencilMarks() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                // Only add pencil marks to empty cells
                if (board[row][col] == 0) {
                    // Clear existing pencil marks first
                    for (int n = 1; n <= 9; n++) {
                        pencilMarks[row][col][n] = false;
                    }

                    // Add all valid numbers as pencil marks
                    for (int num = 1; num <= 9; num++) {
                        if (isNumberAllowed(row, col, num)) {
                            pencilMarks[row][col][num] = true;
                        }
                    }
                }
            }
        }

        // Notify that board has changed
        if (sudokuListener != null) {
            sudokuListener.onBoardChanged();
        }
    }

    // Method to clear all pencil marks from the grid
    private void clearAllPencilMarks() {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                for (int n = 1; n <= 9; n++) {
                    pencilMarks[row][col][n] = false;
                }
            }
        }

        // Notify that board has changed
        if (sudokuListener != null) {
            sudokuListener.onBoardChanged();
        }
    }

    // Method to update pencil marks after placing a number (for fast pencil mode)
    private void updatePencilMarksAfterNumberPlacement(int row, int col, int number) {
        // Remove the placed number from pencil marks in the same row
        for (int c = 0; c < 9; c++) {
            if (board[row][c] == 0) { // Only update empty cells
                pencilMarks[row][c][number] = false;
            }
        }

        // Remove the placed number from pencil marks in the same column
        for (int r = 0; r < 9; r++) {
            if (board[r][col] == 0) { // Only update empty cells
                pencilMarks[r][col][number] = false;
            }
        }

        // Remove the placed number from pencil marks in the same 3x3 box
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if (board[r][c] == 0) { // Only update empty cells
                    pencilMarks[r][c][number] = false;
                }
            }
        }
    }

    // Method to update pencil marks after erasing a number (for fast pencil mode)
    private void updatePencilMarksAfterErase(int row, int col, int erasedNumber) {
        // First, add valid pencil marks back to the erased cell
        if (board[row][col] == 0) { // Should be empty now
            for (int num = 1; num <= 9; num++) {
                if (isNumberAllowed(row, col, num)) {
                    pencilMarks[row][col][num] = true;
                }
            }
        }

        // Then, check if the erased number can now be added back to related cells
        // Check cells in the same row
        for (int c = 0; c < 9; c++) {
            if (board[row][c] == 0 && isNumberAllowed(row, c, erasedNumber)) {
                pencilMarks[row][c][erasedNumber] = true;
            }
        }

        // Check cells in the same column
        for (int r = 0; r < 9; r++) {
            if (board[r][col] == 0 && isNumberAllowed(r, col, erasedNumber)) {
                pencilMarks[r][col][erasedNumber] = true;
            }
        }

        // Check cells in the same 3x3 box
        int startRow = (row / 3) * 3;
        int startCol = (col / 3) * 3;
        for (int r = startRow; r < startRow + 3; r++) {
            for (int c = startCol; c < startCol + 3; c++) {
                if (board[r][c] == 0 && isNumberAllowed(r, c, erasedNumber)) {
                    pencilMarks[r][c][erasedNumber] = true;
                }
            }
        }
    }

    // Check if a number is allowed in the given cell (row, col)
    private boolean isNumberAllowed(int row, int col, int number) {
        // A number is allowed if it's not already in the same row, column, or 3x3 box
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == number || board[i][col] == number) return false;
        }
        int startRow = row - row % 3, startCol = col - col % 3;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (board[startRow + i][startCol + j] == number) return false;
        return true;
    }
}
