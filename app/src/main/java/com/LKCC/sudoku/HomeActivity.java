package com.LKCC.sudoku;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnPlay = findViewById(R.id.btnPlayGame);
        Button btnHistory = findViewById(R.id.btnHistory);
        Spinner spinnerDifficulty = findViewById(R.id.spinnerDifficulty);

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selected = spinnerDifficulty.getSelectedItemPosition();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.putExtra("difficulty", selected); // 0:Easy, 1:Medium, 2:Hard
                startActivity(intent);
            }
        });

        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
    }
}

