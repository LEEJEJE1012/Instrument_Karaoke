package com.example.instrument_karaoke;

import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.app.ProgressDialog;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1, // 기본 레이아웃 사용
                recordFiles
        ) {
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

        Uri selectedWavFileUri = Uri.fromFile(new File(filePath));
        Toast.makeText(this, "WAV 파일 선택 완료!", Toast.LENGTH_SHORT).show();
        Log.d("WavFileUri", "Selected WAV file URI: " + selectedWavFileUri.toString());

        // MXL 파일 선택 버튼
        final String[] selectedMxlFile = {null}; // 선택한 MXL 파일 이름 저장
        Uri[] selectedMxlFileUri = {null};

        selectmxl.setOnClickListener(v -> {
            File mxlDir = new File(getExternalFilesDir(null), "MXLFiles");

            if (!mxlDir.exists() || !mxlDir.isDirectory()) {
                Toast.makeText(this, "MXLFiles 디렉토리가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 디렉토리 내 MXL 파일 목록 가져오기
            File[] mxlFiles = mxlDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".mxl"));

            if (mxlFiles == null || mxlFiles.length == 0) {
                Toast.makeText(this, "MXLFiles 디렉토리에 MXL 파일이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 파일 이름 목록 생성
            String[] fileNames = new String[mxlFiles.length];
            for (int i = 0; i < mxlFiles.length; i++) {
                fileNames[i] = mxlFiles[i].getName();
            }

            // 파일 선택 다이얼로그 표시
            new AlertDialog.Builder(this)
                    .setTitle("MXL 파일 선택")
                    .setItems(fileNames, (dialog, which) -> {
                        File selectedFile = mxlFiles[which];
                        selectedMxlFileUri[0] = Uri.fromFile(selectedFile);
                        Toast.makeText(this, "선택된 파일: " + selectedFile.getName(), Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        // 피드백 받기 버튼
        feedbackButton.setOnClickListener(v -> {
            if (selectedMxlFileUri[0] == null) {
                new AlertDialog.Builder(this)
                        .setTitle("MXL 파일 선택")
                        .setMessage("MXL 파일을 먼저 선택해 주세요.")
                        .setPositiveButton("확인", null)
                        .show();
            } else {
                sendFilesToServer(selectedWavFileUri, selectedMxlFileUri[0]);
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

    private String getRealPathFromURI(Uri uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendFilesToServer(Uri wavFileUri, Uri mxlFileUri) {
        // ProgressDialog 표시
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("업로드를 시작합니다. 잠시만 기다려주세요...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // OkHttpClient 설정
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // 연결 타임아웃
                .readTimeout(120, TimeUnit.SECONDS)    // 읽기 타임아웃
                .writeTimeout(60, TimeUnit.SECONDS)    // 쓰기 타임아웃
                .build();

        // WAV
        String wavFilePath = getRealPathFromURI(wavFileUri);
        Log.d("wavfileURI", "wavFileURI: " + wavFileUri);
        Log.d("wavfilepath", "wavFilePath: " + wavFilePath);
        if (wavFilePath == null) {
            showErrorAndDismiss(progressDialog, "WAV 파일 경로가 유효하지 않습니다.");
            return;
        }
        File wavFile = new File(wavFilePath);
        RequestBody wavRequestBody = RequestBody.create(MediaType.parse("audio/wav"), wavFile);

        // MXL 파일 준비
        String mxlFilePath = getRealPathFromURI(mxlFileUri);
        if (mxlFilePath == null) {
            showErrorAndDismiss(progressDialog, "MXL 파일 경로가 유효하지 않습니다.");
            return;
        }
        File mxlFile = new File(mxlFilePath);
        RequestBody mxlRequestBody = RequestBody.create(MediaType.parse("application/octet-stream"), mxlFile);

        // MultipartBody 생성
        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("wav_file", wavFile.getName(), wavRequestBody)
                .addFormDataPart("mxl_file", mxlFile.getName(), mxlRequestBody)
                .build();

        // 서버 요청 설정
        Request request = new Request.Builder()
                .url("http://172.20.10.3:5001/upload") // Flask 서버 URL
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                // ProgressDialog 숨기기 (UI 작업은 runOnUiThread에서 실행)
                runOnUiThread(() -> progressDialog.dismiss());

                if (response.isSuccessful()) {
                    // 서버로부터 응답이 성공적으로 돌아온 경우
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        JSONArray resultArray = jsonResponse.getJSONArray("result");
                        String csvData = jsonResponse.getString("csv_file");

                        // result_return 배열에 점수 저장
                        double[] resultReturn = new double[2];
                        resultReturn[0] = resultArray.getDouble(0);
                        resultReturn[1] = resultArray.getDouble(1);

                        // CSV 파일 저장
                        saveCsvFile(csvData);

                        // UI 업데이트
                        runOnUiThread(() -> {
                            Toast.makeText(getApplicationContext(),
                                    "비트 점수: " + resultReturn[0] + "\n음정 점수: " + resultReturn[1],
                                    Toast.LENGTH_LONG).show();
                            // FeedbackActivity로 이동 및 resultReturn 값 전달
                            Intent intent = new Intent(getApplicationContext(), FeedbackActivity.class);
                            intent.putExtra("beatScore", resultReturn[0]);
                            intent.putExtra("pitchScore", resultReturn[1]);
                            startActivity(intent);
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                        showErrorAndDismiss_string("응답 파싱 중 오류가 발생했습니다.");
                    }

                } else {
                    // 서버가 오류 응답을 보낸 경우
                    final String errorMessage = response.message();
                    runOnUiThread(() -> {
                        Toast.makeText(RecordingManageActivity.this, "서버 오류: " + errorMessage, Toast.LENGTH_SHORT).show();
                        Log.e("UploadError", "Error response: " + errorMessage);
                    });
                }
            }

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                // ProgressDialog 숨기기 (UI 작업은 runOnUiThread에서 실행)
                runOnUiThread(() -> progressDialog.dismiss());

                // 요청이 실패한 경우 (네트워크 오류 등)
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(RecordingManageActivity.this, "파일 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("UploadError", "Network failure: " + e.getMessage());
                });
            }
        });


    }

    // CSV 파일을 내부 디렉토리에 저장하는 메서드
    // CSV 파일을 내부 디렉토리의 Feedback 폴더에 저장하는 메서드
    private void saveCsvFile(String csvData) {
        String fileName = "result.csv";

        try {
            // 내부 디렉토리에 Feedback 폴더 생성 (없으면 생성)
            File feedbackDir = new File(getExternalFilesDir(null), "Feedback");
            if (!feedbackDir.exists()) {
                feedbackDir.mkdirs(); // 폴더 생성
            }

            // Feedback 폴더 안에 result.csv 파일 경로 설정
            File file = new File(feedbackDir, fileName);

            // FileOutputStream을 사용하여 파일 저장 (덮어쓰기 모드)
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(csvData.getBytes());
                fos.flush();
            }

            Log.d("SaveCSV", "CSV 파일이 저장되었습니다: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("SaveCSV", "CSV 파일 저장 실패: " + e.getMessage());
        }
    }

    private void showErrorAndDismiss(ProgressDialog progressDialog, String message) {
        progressDialog.dismiss();
        runOnUiThread(() -> Toast.makeText(RecordingManageActivity.this, message, Toast.LENGTH_SHORT).show());
    }

    private void showErrorAndDismiss_string(String message) {
        runOnUiThread(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

}