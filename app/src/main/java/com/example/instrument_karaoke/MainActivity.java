package com.example.instrument_karaoke;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.*;
import android.content.Intent;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ImageButton uploadscore_button = (ImageButton) findViewById(R.id.button_main_uploadscore);
        uploadscore_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SheetUploadActivity.class);
                startActivity(intent);
            }
        });

        ImageButton myscore_button = (ImageButton) findViewById(R.id.button_main_myscore);
        myscore_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SheetManageActivity.class);
                startActivity(intent);
            }
        });

        ImageButton play_button = (ImageButton) findViewById(R.id.button_main_play);
        play_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainPlayerActivity.class);
                startActivity(intent);
            }
        });

        ImageButton recorded_button = (ImageButton) findViewById(R.id.button_main_recorded);
        recorded_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RecordingManageActivity.class);
                startActivity(intent);
            }
        });

    }
}

