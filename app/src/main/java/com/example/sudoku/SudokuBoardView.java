package com.example.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class SudokuBoardView extends View {
    private int[][] board = new int[9][9];
    private int selectedRow = -1, selectedCol = -1;
    private Paint linePaint, textPaint, selectedPaint;

    public SudokuBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int cellSize = Math.min(width, height) / 9;
        // Draw selected cell
        if (selectedRow != -1 && selectedCol != -1) {
            canvas.drawRect(selectedCol * cellSize, selectedRow * cellSize,
                    (selectedCol + 1) * cellSize, (selectedRow + 1) * cellSize, selectedPaint);
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
            board[selectedRow][selectedCol] = number;
            invalidate();
        }
    }
}
