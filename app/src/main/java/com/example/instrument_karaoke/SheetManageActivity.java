package com.example.instrument_karaoke;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.ArrayList;

public class SheetManageActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> audioFiles;
    private File appSpecificDir;
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_manage);
        listView = findViewById(R.id.listview_sheetmanage_sheetlist);

        // 애플리케이션 전용 외부 저장소 경로
        appSpecificDir = new File(getExternalFilesDir(null), "AudioFiles");

        // 파일 목록 가져오기
        audioFiles = new ArrayList<>();
        loadAudioFiles();

        // ListView에 파일 목록 표시
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, audioFiles);
        listView.setAdapter(adapter);

        // 항목 클릭 시 파일 재생 액티비티로 이동
        listView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String selectedFileName = audioFiles.get(position);
            Intent intent = new Intent(SheetManageActivity.this, AudioPlayerActivity.class);
            intent.putExtra("fileName", selectedFileName);
            startActivity(intent);
        });

        // 항목 길게 누르기 시 파일 삭제
        listView.setOnItemLongClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String fileNameToDelete = audioFiles.get(position);
            showDeleteConfirmationDialog(fileNameToDelete, position);
            return true;
        });
    }

    // 파일 목록 불러오기
    private void loadAudioFiles() {
        audioFiles.clear();
        if (appSpecificDir.exists()) {
            File[] files = appSpecificDir.listFiles((dir, name) -> name.endsWith(".wav"));
            if (files != null) {
                for (File file : files) {
                    audioFiles.add(file.getName());
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
            audioFiles.remove(position); // 리스트에서 제거
            adapter.notifyDataSetChanged(); // ListView 갱신
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("삭제 실패")
                    .setMessage("파일을 삭제할 수 없습니다.")
                    .setPositiveButton("확인", null)
                    .show();
        }
    }
}