package com.LKCC.sudoku;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameHistory {
    private String date;
    private String difficulty;
    private int timeInSeconds;
    private int score;
    private int mistakes;
    private boolean completed;

    public GameHistory(String difficulty, int timeInSeconds, int score, int mistakes, boolean completed) {
        this.date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        this.difficulty = difficulty;
        this.timeInSeconds = timeInSeconds;
        this.score = score;
        this.mistakes = mistakes;
        this.completed = completed;
    }

    // Constructor for loading from storage
    public GameHistory(String date, String difficulty, int timeInSeconds, int score, int mistakes, boolean completed) {
        this.date = date;
        this.difficulty = difficulty;
        this.timeInSeconds = timeInSeconds;
        this.score = score;
        this.mistakes = mistakes;
        this.completed = completed;
    }

    // Getters
    public String getDate() { return date; }
    public String getDifficulty() { return difficulty; }
    public int getTimeInSeconds() { return timeInSeconds; }
    public int getScore() { return score; }
    public int getMistakes() { return mistakes; }
    public boolean isCompleted() { return completed; }

    public String getFormattedTime() {
        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String toString() {
        return date + "|" + difficulty + "|" + timeInSeconds + "|" + score + "|" + mistakes + "|" + completed;
    }

    public static GameHistory fromString(String str) {
        try {
            String[] parts = str.split("\\|");
            if (parts.length >= 6) {
                return new GameHistory(
                    parts[0], // date
                    parts[1], // difficulty
                    Integer.parseInt(parts[2]), // time
                    Integer.parseInt(parts[3]), // score
                    Integer.parseInt(parts[4]), // mistakes
                    Boolean.parseBoolean(parts[5]) // completed
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
