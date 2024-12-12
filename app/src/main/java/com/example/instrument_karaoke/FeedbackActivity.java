package com.example.instrument_karaoke;

import android.os.Bundle;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FeedbackActivity extends AppCompatActivity {

    private static final float X_SCALE = 200; // X축 스케일 조정
    private static final float Y_SCALE = 15; // Y축 스케일 조정
    private static final float OFFSET_Y = 700; // Y축 시작점

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_feedback);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Intent에서 값 받기
        double beatScore = getIntent().getDoubleExtra("beatScore", 0.0);
        double pitchScore = getIntent().getDoubleExtra("pitchScore", 0.0);

        TextView beatScoreTextView = findViewById(R.id.textview_feedback_beatscore);
        TextView pitchScoreTextView = findViewById(R.id.textview_feedback_pitchscore);
        HorizontalScrollView horizontalScrollView = findViewById(R.id.Scrollview);


        beatScoreTextView.setText("비트 점수: " + beatScore);
        pitchScoreTextView.setText("음정 점수: " + pitchScore);

        CustomView customView = findViewById(R.id.customView_feedback_customview);

        List<DataPoint> ansPoints = new ArrayList<>();
        List<DataPoint> recPoints = new ArrayList<>();

        File feedbackDir = new File(getExternalFilesDir(null), "Feedback");
        File resultFile = new File(feedbackDir, "result.csv");

        // CSV 파일 읽기
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(resultFile)))) {
            String line;
            reader.readLine(); // 헤더 건너뛰기
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                // 데이터 추출
                float ansOnset = Float.parseFloat(parts[1]);
                String ansPitch = parts[4];
                float recOnset = Float.parseFloat(parts[2]);
                String recPitch = parts[5];

                // x 좌표 (onset)
                float xAns = ansOnset * X_SCALE;
                float xRec = recOnset * X_SCALE;

                // ansPitch를 분리해 개별 점 추가
                for (String pitch : ansPitch.split("/")) {
                    float yAns = OFFSET_Y - pitchToY(pitch.trim())*Y_SCALE;
                    ansPoints.add(new DataPoint(xAns, yAns));
                }

                // recPitch를 분리해 개별 점 추가
                if (!recPitch.equals("NaN")) {
                    for (String pitch : recPitch.split("/")) {
                        float yRec = OFFSET_Y - pitchToY(pitch.trim())*Y_SCALE;
                        recPoints.add(new DataPoint(xRec, yRec));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 커스텀 뷰에 데이터 전달
        customView.setPoints(ansPoints, recPoints);
    }

    // pitch 값을 y 좌표로 변환
    private int pitchToY(String pitch) {
        switch (pitch) {
            case "C4": return 0;
            case "C♯4": return 1;
            case "D4": return 2;
            case "D♯4": return 3;
            case "E4": return 4;
            case "F4": return 5;
            case "F♯4": return 6;
            case "G4": return 7;
            case "G♯4": return 8;
            case "A4": return 9;
            case "A♯4": return 10;
            case "B4": return 11;
            case "C5": return 12;
            default: return 0;
        }
    }
}