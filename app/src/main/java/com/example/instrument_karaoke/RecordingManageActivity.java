package com.example.instrument_karaoke;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RecordingManageActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> recordFiles;
    private File appSpecificDir;
    private ArrayAdapter<String> adapter;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recording_manage);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listview_recordingmanage_recordlist);
        appSpecificDir = new File(getExternalFilesDir(null), "Recorded");

        recordFiles = new ArrayList<>();
        loadAudioFiles();

        // 어댑터 설정 및 항목 텍스트 색상 변경
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1, // 기본 레이아웃 사용
                recordFiles
        ){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // 항목의 텍스트뷰 색상 변경
                TextView textView = (TextView) view.findViewById(android.R.id.text1);
                textView.setTextColor(getResources().getColor(android.R.color.black, null));

                return view;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String selectedFileName = recordFiles.get(position);
            showAudioPlayerDialog(selectedFileName);
        });

        listView.setOnItemLongClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String fileNameToDelete = recordFiles.get(position);
            showDeleteConfirmationDialog(fileNameToDelete, position);
            return true;
        });


    }
    private void showAudioPlayerDialog(String fileName) {
        // 팝업 레이아웃 설정
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_record_player, null);

        TextView fileNameTextView = dialogView.findViewById(R.id.dialog_file_name);
        Button playButton = dialogView.findViewById(R.id.dialog_button_play);
        Button stopButton = dialogView.findViewById(R.id.dialog_button_stop);
        Button selectmxl = dialogView.findViewById(R.id.dialog_button_selectmxl);
        Button feedbackButton = dialogView.findViewById(R.id.dialog_button_feedback);

        fileNameTextView.setText(fileName);

        // MediaPlayer 설정
        File appSpecificDir = new File(getExternalFilesDir(null), "Recorded");
        String filePath = new File(appSpecificDir, fileName).getAbsolutePath();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // WAV 파일 저장 (선택한 파일 이름)
        String selectedWavFile = fileName;

        // MXL 파일 선택 버튼
        final String[] selectedMxlFile = {null}; // 선택한 MXL 파일 이름 저장
        selectmxl.setOnClickListener(v -> showMxlFilePicker(selectedMxlFile));

        // 피드백 받기 버튼
        feedbackButton.setOnClickListener(v -> {
            if (selectedMxlFile[0] == null) {
                new AlertDialog.Builder(this)
                        .setTitle("MXL 파일 선택")
                        .setMessage("MXL 파일을 먼저 선택해 주세요.")
                        .setPositiveButton("확인", null)
                        .show();
            } else {
                //sendFilesToServer(selectedWavFile, selectedMxlFile[0]);
            }
        });

        // 재생 버튼 리스너
        playButton.setOnClickListener(v -> {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        });

        // 정지 버튼 리스너
        stopButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        });

        // 팝업 생성
        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .setPositiveButton("닫기", (dialog, which) -> {
                    if (mediaPlayer != null) {
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                })
                .show();
    }
    // 파일 목록 불러오기
    private void loadAudioFiles() {
        recordFiles.clear();
        if (appSpecificDir.exists()) {
            File[] files = appSpecificDir.listFiles((dir, name) -> name.endsWith(".wav"));
            if (files != null) {
                for (File file : files) {
                    recordFiles.add(file.getName());
                }
            }
        }
    }

    // 삭제 확인 다이얼로그 표시
    private void showDeleteConfirmationDialog(String fileName, int position) {
        new AlertDialog.Builder(this)
                .setTitle("파일 삭제")
                .setMessage("정말로 '" + fileName + "'을(를) 삭제하시겠습니까?")
                .setPositiveButton("삭제", (dialog, which) -> deleteFile(fileName, position))
                .setNegativeButton("취소", null)
                .show();
    }

    // 파일 삭제 및 리스트 갱신
    private void deleteFile(String fileName, int position) {
        File fileToDelete = new File(appSpecificDir, fileName);
        if (fileToDelete.exists() && fileToDelete.delete()) {
            recordFiles.remove(position); // 리스트에서 제거
            adapter.notifyDataSetChanged(); // ListView 갱신
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("삭제 실패")
                    .setMessage("파일을 삭제할 수 없습니다.")
                    .setPositiveButton("확인", null)
                    .show();
        }
    }

    private void showMxlFilePicker(String[] selectedMxlFile) {
        // MXLFiles 디렉터리 확인
        File mxlDir = new File(getExternalFilesDir(null), "MXLFiles");
        if (!mxlDir.exists() || !mxlDir.isDirectory()) {
            new AlertDialog.Builder(this)
                    .setTitle("MXL 파일 없음")
                    .setMessage("MXLFiles 디렉터리에 파일이 없습니다.")
                    .setPositiveButton("확인", null)
                    .show();
            return;
        }

        // MXL 파일 목록 가져오기
        File[] mxlFiles = mxlDir.listFiles((dir, name) -> name.endsWith(".mxl"));
        if (mxlFiles == null || mxlFiles.length == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("MXL 파일 없음")
                    .setMessage("MXLFiles 디렉터리에 파일이 없습니다.")
                    .setPositiveButton("확인", null)
                    .show();
            return;
        }

        // 파일 이름 리스트 생성
        String[] fileNames = Arrays.stream(mxlFiles)
                .map(File::getName)
                .toArray(String[]::new);

        // 다이얼로그로 파일 선택
        new AlertDialog.Builder(this)
                .setTitle("MXL 파일 선택")
                .setItems(fileNames, (dialog, which) -> {
                    selectedMxlFile[0] = fileNames[which];
                    new AlertDialog.Builder(this)
                            .setTitle("파일 선택됨")
                            .setMessage("선택한 MXL 파일: " + selectedMxlFile[0])
                            .setPositiveButton("확인", null)
                            .show();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}