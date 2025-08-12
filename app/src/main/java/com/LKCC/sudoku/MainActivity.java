package com.LKCC.sudoku;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SudokuBoardView.SudokuListener {
    private Handler timerHandler = new Handler();
    private int secondsElapsed = 0;
    private boolean isTimerRunning = false;
    private boolean isPaused = false; // Add pause state tracking
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
    private int hintsUsed = 0;
    private static final int MAX_HINTS = 3;
    private TextView tvMistakes;
    private TextView tvScore;
    private TextView tvDifficulty;
    private TextView tvPencilon;
    private TextView tvfastPencilon;
    private TextView tvHintCount;
    private SudokuBoardView sudokuBoard;
    private TextView[] numberCountViews = new TextView[9]; // Array to hold the small number count TextViews
    private TextView[] numberButtons = new TextView[9]; // Array to hold the number buttons
    private View btnPause; // Change to View since it's an ImageView in layout
    private View btnSave; // Add save button reference
    private View btnLoad; // Add load button reference
    private LinearLayout winningScreen; // Add winning screen reference
    private LinearLayout losingScreen; // Add losing screen reference
    private GameHistoryManager historyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize history manager
        historyManager = new GameHistoryManager(this);

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
        tvHintCount = findViewById(R.id.tvHintCount); // Initialize hint count TextView
        winningScreen = findViewById(R.id.winningScreen); // Initialize winning screen
        losingScreen = findViewById(R.id.losingScreen); // Initialize losing screen

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

        // Set default modes to OFF - ensure they start in the correct state
        sudokuBoard.setPencilMode(false);
        // Only toggle fast pencil mode if it's currently on (since we want it OFF)
        if (sudokuBoard.isfastPencilMode()) {
            sudokuBoard.togglefastPencilMode();
        }

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
                useHint();
            }
        });

        // Initialize hint count display
        updateHintCountDisplay();

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

        // Add pause button functionality
        btnPause = findViewById(R.id.btnPause);
        if (btnPause != null) {
            btnPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePause();
                }
            });
        }

        // Add save button functionality
        btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) {
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveGameState();
                }
            });
        }

        // Add load button functionality
        btnLoad = findViewById(R.id.btnLoad);
        if (btnLoad != null) {
            btnLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadGameState();
                }
            });
        }

        // Start timer when game begins
        isTimerRunning = true;
        timerHandler.postDelayed(timerRunnable, 1000);

        // Initialize winning screen buttons
        setupWinningScreenButtons();

        // Initialize losing screen buttons
        setupLosingScreenButtons();
    }

    // Method to toggle pause state
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            // Pause the game
            isTimerRunning = false;
            sudokuBoard.setPaused(true); // Notify board it's paused
            // Change icon to play button (resume) - using Android's built-in icon
            if (btnPause instanceof android.widget.ImageView) {
                ((android.widget.ImageView) btnPause).setImageResource(android.R.drawable.ic_media_play);
            }
        } else {
            // Resume the game
            isTimerRunning = true;
            sudokuBoard.setPaused(false); // Notify board it's resumed
            // Change icon back to pause button
            if (btnPause instanceof android.widget.ImageView) {
                ((android.widget.ImageView) btnPause).setImageResource(android.R.drawable.ic_media_pause);
            }
            timerHandler.postDelayed(timerRunnable, 1000);
        }
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

        // Save completed game to history
        saveGameToHistory(true);

        // Show the full-screen winning overlay
        winningScreen.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCorrectBoxClick() {
        score += Math.max(200,(int)((6000-1000*Math.log1p((double)secondsElapsed/10))/3)); // Add 10 points for correct placement
        tvScore.setText("Score: " + score);
        updateNumberCounts(); // Update number counts when board changes
    }

    @Override
    public void onMistake() {
        mistakes++;
        tvMistakes.setText("Mistakes: " + mistakes + "/3");
        if (mistakes >= 3) {
            // Game over logic
            isTimerRunning = false;
            Toast.makeText(this, "Game Over! Too many mistakes.", Toast.LENGTH_SHORT).show();

            // Save failed game to history
            saveGameToHistory(false);

            // Show the full-screen losing overlay
            losingScreen.setVisibility(View.VISIBLE);
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

    private void saveGameToHistory(boolean completed) {
        String difficultyString;
        switch (mode) {
            case EASY:
                difficultyString = "Easy";
                break;
            case MEDIUM:
                difficultyString = "Medium";
                break;
            case HARD:
                difficultyString = "Hard";
                break;
            default:
                difficultyString = "Easy";
        }

        GameHistory gameHistory = new GameHistory(
            difficultyString,
            secondsElapsed,
            score,
            mistakes,
            completed
        );

        historyManager.saveGameHistory(gameHistory);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Save game history if the game was in progress when destroyed
        if (isTimerRunning && secondsElapsed > 30) { // Only save if played for more than 30 seconds
            saveGameToHistory(false);
        }
    }

    // Method to use a hint
    private void useHint() {
        if (hintsUsed < MAX_HINTS) {
            sudokuBoard.showHint();
            hintsUsed++;
            updateHintCountDisplay(); // Update hint count display after using a hint
        } else {
            Toast.makeText(this, "No more hints available", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to update the hint count display
    private void updateHintCountDisplay() {
        tvHintCount.setText(String.valueOf(MAX_HINTS - hintsUsed));
    }

    // Method to reset hints (useful for new game functionality)
    private void resetHints() {
        hintsUsed = 0;
        updateHintCountDisplay();
    }

    // Method to setup winning screen buttons
    private void setupWinningScreenButtons() {
        Button btnOneMoreGame = findViewById(R.id.btnOneMoreGame);
        Button btnBackHome = findViewById(R.id.btnBackHome);

        if (btnOneMoreGame != null) {
            btnOneMoreGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Start a new game with same difficulty
                    startNewGame();
                }
            });
        }

        if (btnBackHome != null) {
            btnBackHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go back to home screen
                    finish();
                }
            });
        }
    }

    // Method to setup losing screen buttons
    private void setupLosingScreenButtons() {
        Button btnTryAgain = findViewById(R.id.btnTryAgain);
        Button btnBackHomeFromLose = findViewById(R.id.btnBackHomeFromLose);

        if (btnTryAgain != null) {
            btnTryAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Restart the same game
                    restartGame();
                }
            });
        }

        if (btnBackHomeFromLose != null) {
            btnBackHomeFromLose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Go back to home screen
                    finish();
                }
            });
        }
    }

    // Method to restart the game
    private void restartGame() {
        // Hide losing screen
        losingScreen.setVisibility(View.GONE);

        // Reset game state variables
        secondsElapsed = 0;
        score = 0;
        mistakes = 0;
        hintsUsed = 0;

        // Reset UI elements
        tvScore.setText("Score: " + score);
        tvMistakes.setText("Mistakes: " + mistakes + "/3");
        tvFinished.setVisibility(View.GONE);
        updateHintCountDisplay();

        // Generate new puzzle with same difficulty
        sudokuBoard.generateNewPuzzle();
        updateNumberCounts();

        // Restart timer
        isTimerRunning = true;
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    // Method to start a new game
    private void startNewGame() {
        // Hide winning screen
        winningScreen.setVisibility(View.GONE);

        // Reset game state variables
        secondsElapsed = 0;
        score = 0;
        mistakes = 0;
        hintsUsed = 0;

        // Reset UI elements
        tvScore.setText("Score: " + score);
        tvMistakes.setText("Mistakes: " + mistakes + "/3");
        tvFinished.setVisibility(View.GONE);
        updateHintCountDisplay();

        // Generate new puzzle with same difficulty
        sudokuBoard.generateNewPuzzle();
        updateNumberCounts();

        // Restart timer
        isTimerRunning = true;
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    // Method to save current game state
    private void saveGameState() {
        if (sudokuBoard == null) {
            Toast.makeText(this, "No game to save", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("SudokuGameSave", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Save game board state
        int[][] currentBoard = sudokuBoard.getCurrentBoard();
        int[][] initialBoard = sudokuBoard.getInitialBoard();
        int[][] solution = sudokuBoard.getSolution();
        boolean[][][] pencilMarks = sudokuBoard.getPencilMarks();

        // Convert 2D arrays to strings for storage
        StringBuilder currentBoardStr = new StringBuilder();
        StringBuilder initialBoardStr = new StringBuilder();
        StringBuilder solutionStr = new StringBuilder();
        StringBuilder pencilMarksStr = new StringBuilder();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                currentBoardStr.append(currentBoard[i][j]).append(",");
                initialBoardStr.append(initialBoard[i][j]).append(",");
                solutionStr.append(solution[i][j]).append(",");

                // Save pencil marks for each cell
                for (int k = 1; k <= 9; k++) {
                    pencilMarksStr.append(pencilMarks[i][j][k] ? "1" : "0");
                }
                pencilMarksStr.append(",");
            }
        }

        // Save all game state
        editor.putString("currentBoard", currentBoardStr.toString());
        editor.putString("initialBoard", initialBoardStr.toString());
        editor.putString("solution", solutionStr.toString());
        editor.putString("pencilMarks", pencilMarksStr.toString());
        editor.putInt("secondsElapsed", secondsElapsed);
        editor.putInt("score", score);
        editor.putInt("mistakes", mistakes);
        editor.putInt("hintsUsed", hintsUsed);
        editor.putInt("difficulty", difficulty);
        editor.putString("gameMode", mode.toString());
        editor.putBoolean("pencilMode", sudokuBoard.isPencilMode());
        editor.putBoolean("fastPencilMode", sudokuBoard.isfastPencilMode());
        editor.putBoolean("gameExists", true);

        editor.apply();
        Toast.makeText(this, "Game saved successfully!", Toast.LENGTH_SHORT).show();
    }

    // Method to load saved game state
    private void loadGameState() {
        SharedPreferences prefs = getSharedPreferences("SudokuGameSave", MODE_PRIVATE);

        if (!prefs.getBoolean("gameExists", false)) {
            Toast.makeText(this, "No saved game found", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Load game state
            String currentBoardStr = prefs.getString("currentBoard", "");
            String initialBoardStr = prefs.getString("initialBoard", "");
            String solutionStr = prefs.getString("solution", "");
            String pencilMarksStr = prefs.getString("pencilMarks", "");

            if (currentBoardStr.isEmpty() || initialBoardStr.isEmpty() || solutionStr.isEmpty()) {
                Toast.makeText(this, "Invalid saved game data", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse board data
            String[] currentBoardData = currentBoardStr.split(",");
            String[] initialBoardData = initialBoardStr.split(",");
            String[] solutionData = solutionStr.split(",");
            String[] pencilMarksData = pencilMarksStr.split(",");

            int[][] currentBoard = new int[9][9];
            int[][] initialBoard = new int[9][9];
            int[][] solution = new int[9][9];
            boolean[][][] pencilMarks = new boolean[9][9][10];

            // Restore board states
            int index = 0;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    currentBoard[i][j] = Integer.parseInt(currentBoardData[index]);
                    initialBoard[i][j] = Integer.parseInt(initialBoardData[index]);
                    solution[i][j] = Integer.parseInt(solutionData[index]);

                    // Restore pencil marks
                    if (index < pencilMarksData.length && !pencilMarksData[index].isEmpty()) {
                        String cellPencilMarks = pencilMarksData[index];
                        for (int k = 1; k <= 9 && k-1 < cellPencilMarks.length(); k++) {
                            pencilMarks[i][j][k] = cellPencilMarks.charAt(k-1) == '1';
                        }
                    }
                    index++;
                }
            }

            // Restore game variables
            secondsElapsed = prefs.getInt("secondsElapsed", 0);
            score = prefs.getInt("score", 0);
            mistakes = prefs.getInt("mistakes", 0);
            hintsUsed = prefs.getInt("hintsUsed", 0);
            difficulty = prefs.getInt("difficulty", 0);
            String gameModeStr = prefs.getString("gameMode", "EASY");

            try {
                mode = GameMode.valueOf(gameModeStr);
            } catch (IllegalArgumentException e) {
                mode = GameMode.EASY;
            }

            // Restore UI elements
            tvScore.setText("Score: " + score);
            tvMistakes.setText("Mistakes: " + mistakes + "/3");
            updateHintCountDisplay();

            // Set difficulty display
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

            // Restore sudoku board state
            sudokuBoard.loadGameState(currentBoard, initialBoard, solution, pencilMarks);

            // Restore game modes
            boolean pencilMode = prefs.getBoolean("pencilMode", false);
            boolean fastPencilMode = prefs.getBoolean("fastPencilMode", false);

            sudokuBoard.setPencilMode(pencilMode);
            tvPencilon.setText(pencilMode ? "ON" : "OFF");

            if (sudokuBoard.isfastPencilMode() != fastPencilMode) {
                sudokuBoard.togglefastPencilMode();
            }
            tvfastPencilon.setText(fastPencilMode ? "ON" : "OFF");

            // Update number counts
            updateNumberCounts();

            // Resume timer if game was running
            if (!isPaused) {
                isTimerRunning = true;
                timerHandler.postDelayed(timerRunnable, 1000);
            }

            Toast.makeText(this, "Game loaded successfully!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error loading saved game", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
