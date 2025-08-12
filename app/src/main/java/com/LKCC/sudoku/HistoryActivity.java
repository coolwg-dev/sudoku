package com.LKCC.sudoku;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.graphics.Color;
import androidx.appcompat.app.AlertDialog;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    private GameHistoryManager historyManager;
    private LinearLayout llHistoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyManager = new GameHistoryManager(this);
        llHistoryList = findViewById(R.id.llHistoryList);

        Button btnBackHome = findViewById(R.id.btnBackHome);
        btnBackHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HistoryActivity.this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        Button btnClearHistory = findViewById(R.id.btnClearHistory);
        btnClearHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearHistoryDialog();
            }
        });

        loadAndDisplayHistory();
    }

    private void loadAndDisplayHistory() {
        // Update statistics
        updateStatistics();

        // Load and display game history
        List<GameHistory> historyList = historyManager.getAllHistory();
        llHistoryList.removeAllViews();

        if (historyList.isEmpty()) {
            TextView emptyView = new TextView(this);
            emptyView.setText("No games played yet. Start playing to see your history!");
            emptyView.setTextSize(16);
            emptyView.setPadding(16, 32, 16, 32);
            emptyView.setTextColor(Color.GRAY);
            llHistoryList.addView(emptyView);
        } else {
            // Show only recent 20 games to avoid performance issues
            int maxGames = Math.min(historyList.size(), 20);
            for (int i = 0; i < maxGames; i++) {
                GameHistory game = historyList.get(i);
                View gameView = createGameHistoryView(game);
                llHistoryList.addView(gameView);
            }

            if (historyList.size() > 20) {
                TextView moreView = new TextView(this);
                moreView.setText("... and " + (historyList.size() - 20) + " more games");
                moreView.setTextSize(14);
                moreView.setPadding(16, 16, 16, 16);
                moreView.setTextColor(Color.GRAY);
                llHistoryList.addView(moreView);
            }
        }
    }

    private void updateStatistics() {
        TextView tvTotalGames = findViewById(R.id.tvTotalGames);
        TextView tvCompletedGames = findViewById(R.id.tvCompletedGames);
        TextView tvCompletionRate = findViewById(R.id.tvCompletionRate);
        TextView tvHighestScore = findViewById(R.id.tvHighestScore);
        TextView tvBestTimeEasy = findViewById(R.id.tvBestTimeEasy);
        TextView tvBestTimeMedium = findViewById(R.id.tvBestTimeMedium);
        TextView tvBestTimeHard = findViewById(R.id.tvBestTimeHard);

        int totalGames = historyManager.getTotalGames();
        int completedGames = historyManager.getCompletedGames();
        double completionRate = historyManager.getCompletionRate();
        int highestScore = historyManager.getHighestScore();

        tvTotalGames.setText("Total Games: " + totalGames);
        tvCompletedGames.setText("Completed Games: " + completedGames);
        tvCompletionRate.setText(String.format("Completion Rate: %.1f%%", completionRate));
        tvHighestScore.setText("Highest Score: " + highestScore);

        // Best times
        int bestTimeEasy = historyManager.getBestTime("easy");
        int bestTimeMedium = historyManager.getBestTime("medium");
        int bestTimeHard = historyManager.getBestTime("hard");

        tvBestTimeEasy.setText("Easy: " + (bestTimeEasy == -1 ? "--:--" : formatTime(bestTimeEasy)));
        tvBestTimeMedium.setText("Medium: " + (bestTimeMedium == -1 ? "--:--" : formatTime(bestTimeMedium)));
        tvBestTimeHard.setText("Hard: " + (bestTimeHard == -1 ? "--:--" : formatTime(bestTimeHard)));
    }

    private View createGameHistoryView(GameHistory game) {
        LinearLayout gameLayout = new LinearLayout(this);
        gameLayout.setOrientation(LinearLayout.VERTICAL);
        gameLayout.setPadding(16, 12, 16, 12);
        gameLayout.setBackground(getResources().getDrawable(android.R.drawable.dialog_holo_light_frame));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 8);
        gameLayout.setLayoutParams(params);

        // Date and completion status
        TextView headerView = new TextView(this);
        String status = game.isCompleted() ? "✓ COMPLETED" : "✗ " + (game.getMistakes() >= 3 ? "FAILED" : "INCOMPLETE");
        String statusColor = game.isCompleted() ? "#4CAF50" : "#f44336";
        headerView.setText(game.getDate() + " - " + status);
        headerView.setTextSize(14);
        headerView.setTextColor(Color.parseColor(statusColor));
        headerView.setTextStyle(android.graphics.Typeface.BOLD);
        gameLayout.addView(headerView);

        // Game details
        TextView detailsView = new TextView(this);
        String details = String.format("Difficulty: %s | Time: %s | Score: %d | Mistakes: %d/3",
            game.getDifficulty(),
            game.getFormattedTime(),
            game.getScore(),
            game.getMistakes()
        );
        detailsView.setText(details);
        detailsView.setTextSize(12);
        detailsView.setTextColor(Color.GRAY);
        detailsView.setPadding(0, 4, 0, 0);
        gameLayout.addView(detailsView);

        return gameLayout;
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private void showClearHistoryDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Clear History")
            .setMessage("Are you sure you want to clear all game history? This action cannot be undone.")
            .setPositiveButton("Clear", (dialog, which) -> {
                historyManager.clearHistory();
                loadAndDisplayHistory();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
