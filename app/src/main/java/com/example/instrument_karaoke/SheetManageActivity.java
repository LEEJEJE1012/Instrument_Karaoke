package com.example.instrument_karaoke;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class SheetManageActivity extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> audioFiles;
    private File appSpecificDir;
    private ArrayAdapter<String> adapter;
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Handler handler = new Handler(); // SeekBar 업데이트를 위한 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet_manage);
        listView = findViewById(R.id.listview_sheetupload_sheetlist);

        // 애플리케이션 전용 외부 저장소 경로
        appSpecificDir = new File(getExternalFilesDir(null), "AudioFiles");

        // 파일 목록 가져오기
        audioFiles = new ArrayList<>();
        loadAudioFiles();

        // 어댑터 설정 및 항목 텍스트 색상 변경
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1, // 기본 레이아웃 사용
                audioFiles
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

        // 항목 클릭 시 파일 재생 액티비티로 이동
//        listView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
//            String selectedFileName = audioFiles.get(position);
//            Intent intent = new Intent(SheetManageActivity.this, AudioPlayerActivity.class);
//            intent.putExtra("fileName", selectedFileName);
//            startActivity(intent);
//        });
        listView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String selectedFileName = audioFiles.get(position);
            showAudioPlayerDialog(selectedFileName);
        });



        // 항목 길게 누르기 시 파일 삭제
        listView.setOnItemLongClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String fileNameToDelete = audioFiles.get(position);
            showDeleteConfirmationDialog(fileNameToDelete, position);
            return true;
        });
    }

    // 팝업으로 재생/정지 기능 제공
    private void showAudioPlayerDialog(String fileName) {
        // 팝업 레이아웃 설정
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_audio_player, null);

        TextView fileNameTextView = dialogView.findViewById(R.id.dialog_file_name);
        Button playButton = dialogView.findViewById(R.id.dialog_button_play);
        Button stopButton = dialogView.findViewById(R.id.dialog_button_stop);
        seekBar = dialogView.findViewById(R.id.dialog_audioplayer_seekbar);

        fileNameTextView.setText(fileName);

        // MediaPlayer 설정
        File appSpecificDir = new File(getExternalFilesDir(null), "AudioFiles");
        String filePath = new File(appSpecificDir, fileName).getAbsolutePath();
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // SeekBar 설정
        mediaPlayer.setOnPreparedListener(mp -> {
            seekBar.setMax(mp.getDuration()); // MediaPlayer 준비 완료 후 SeekBar 최대값 설정
        });
        // SeekBar 업데이트 쓰레드 시작
        Runnable updateSeekBar = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    seekBar.setProgress(mediaPlayer.getCurrentPosition());
                }
                handler.postDelayed(this, 100); // 100ms 간격으로 실행
            }
        };

        // 재생 버튼 리스너
        playButton.setOnClickListener(v -> {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                handler.post(updateSeekBar);
            }
        });

        // 정지 버튼 리스너
        stopButton.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        });

        // SeekBar 변경 이벤트 리스너
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress); // 사용자가 SeekBar를 움직이면 해당 위치로 이동
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // 사용자가 SeekBar를 터치할 때
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // 사용자가 SeekBar 터치를 끝냈을 때
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
                    handler.removeCallbacks(updateSeekBar);
                })
                .show();
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