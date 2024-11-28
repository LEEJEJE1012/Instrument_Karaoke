package com.example.instrument_karaoke;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class AudioPlayerActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private Button playButton, stopButton;
    private TextView fileNameTextView;
    private String selectedFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audioplayer);

        Button playButton = (Button) findViewById(R.id.button_audioplayer_play);
        Button stopButton = (Button) findViewById(R.id.button_audioplay_stop);
        TextView fileNameTextView = findViewById(R.id.fileNameTextView);

        // 선택된 파일 경로 가져오기
        String fileName = getIntent().getStringExtra("fileName");
        File appSpecificDir = new File(getExternalFilesDir(null), "AudioFiles");
        selectedFilePath = new File(appSpecificDir, fileName).getAbsolutePath();

        fileNameTextView.setText(fileName);

        // MediaPlayer 설정
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(selectedFilePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 재생 버튼 클릭 리스너
        playButton.setOnClickListener(v -> {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        });

        // 정지 버튼 클릭 리스너
        stopButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}