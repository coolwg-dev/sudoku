package com.LKCC.sudoku;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class GameHistoryManager {
    private static final String PREF_NAME = "sudoku_game_history";
    private static final String KEY_HISTORY_LIST = "history_list";
    private static final String KEY_TOTAL_GAMES = "total_games";
    private static final String KEY_COMPLETED_GAMES = "completed_games";
    private static final String KEY_BEST_TIME_EASY = "best_time_easy";
    private static final String KEY_BEST_TIME_MEDIUM = "best_time_medium";
    private static final String KEY_BEST_TIME_HARD = "best_time_hard";
    private static final String KEY_HIGHEST_SCORE = "highest_score";

    private SharedPreferences prefs;
    private Context context;

    public GameHistoryManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveGameHistory(GameHistory history) {
        // Get existing history
        Set<String> historySet = prefs.getStringSet(KEY_HISTORY_LIST, new HashSet<String>());
        Set<String> newHistorySet = new HashSet<>(historySet);

        // Add new game
        newHistorySet.add(history.toString());

        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_HISTORY_LIST, newHistorySet);

        // Update statistics
        int totalGames = prefs.getInt(KEY_TOTAL_GAMES, 0) + 1;
        editor.putInt(KEY_TOTAL_GAMES, totalGames);

        if (history.isCompleted()) {
            int completedGames = prefs.getInt(KEY_COMPLETED_GAMES, 0) + 1;
            editor.putInt(KEY_COMPLETED_GAMES, completedGames);

            // Update best times for each difficulty
            String difficulty = history.getDifficulty().toLowerCase();
            String bestTimeKey = "best_time_" + difficulty;
            int currentBestTime = prefs.getInt(bestTimeKey, Integer.MAX_VALUE);
            if (history.getTimeInSeconds() < currentBestTime) {
                editor.putInt(bestTimeKey, history.getTimeInSeconds());
            }
        }

        // Update highest score
        int currentHighestScore = prefs.getInt(KEY_HIGHEST_SCORE, 0);
        if (history.getScore() > currentHighestScore) {
            editor.putInt(KEY_HIGHEST_SCORE, history.getScore());
        }

        editor.apply();
    }

    public List<GameHistory> getAllHistory() {
        Set<String> historySet = prefs.getStringSet(KEY_HISTORY_LIST, new HashSet<String>());
        List<GameHistory> historyList = new ArrayList<>();

        for (String historyString : historySet) {
            GameHistory history = GameHistory.fromString(historyString);
            if (history != null) {
                historyList.add(history);
            }
        }

        // Sort by date (newest first) - simple string comparison works for our date format
        historyList.sort((h1, h2) -> h2.getDate().compareTo(h1.getDate()));

        return historyList;
    }

    public int getTotalGames() {
        return prefs.getInt(KEY_TOTAL_GAMES, 0);
    }

    public int getCompletedGames() {
        return prefs.getInt(KEY_COMPLETED_GAMES, 0);
    }

    public int getBestTime(String difficulty) {
        String key = "best_time_" + difficulty.toLowerCase();
        int bestTime = prefs.getInt(key, Integer.MAX_VALUE);
        return bestTime == Integer.MAX_VALUE ? -1 : bestTime;
    }

    public int getHighestScore() {
        return prefs.getInt(KEY_HIGHEST_SCORE, 0);
    }

    public double getCompletionRate() {
        int total = getTotalGames();
        if (total == 0) return 0.0;
        return (double) getCompletedGames() / total * 100;
    }

    public void clearHistory() {
        prefs.edit().clear().apply();
    }
}
