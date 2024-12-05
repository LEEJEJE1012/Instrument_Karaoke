package com.example.instrument_karaoke;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SheetUploadActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 1;
    private static final int PICK_IMAGES = 1;
    private List<Item> itemList = new ArrayList<>();
    private ArrayAdapter<Item> adapter;
    private AlertDialog progressDialog;
    private String currentTitle = null;
    private String currentArtist = null;
    private String currentInstrument = null;
    private int currentTempo; // 템포 값 저장
    private ImageView imageView;
    private Uri imageUri;
    private List<Uri> imageUriList = new ArrayList<>();
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sheetupload);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.listview_sheetupload_sheetlist), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ListView listView = (ListView)findViewById(R.id.listview_sheetupload_sheetlist);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);


        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Item selectedItem = itemList.get(position);
            showItemDialog(selectedItem); // 클릭된 항목을 팝업으로 표시
        });


        // 버튼 설정
        Button addButton = (Button)findViewById(R.id.button_sheetupload_makesheet); // activity_sheetmanage.xml에 버튼 추가 필요
        addButton.setOnClickListener(view -> showNameInputDialog());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Media Read Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Media Read Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showNameInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("곡 정보 입력");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 곡 제목 입력 필드
        final EditText titleInput = new EditText(this);
        titleInput.setHint("곡 제목");
        layout.addView(titleInput);

        // 아티스트 이름 입력 필드
        final EditText artistInput = new EditText(this);
        artistInput.setHint("아티스트 이름");
        layout.addView(artistInput);

        // 악기 선택 Spinner 추가
        final Spinner instrumentSpinner = new Spinner(this);
        String[] instruments = {"일렉기타", "어쿠스틱 기타", "피아노", "베이스 기타", "드럼"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, instruments);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentSpinner.setAdapter(spinnerAdapter);
        layout.addView(instrumentSpinner);

        // 템포 입력 필드
        final EditText tempoInput = new EditText(this);
        tempoInput.setHint("템포 (40-300)");
        tempoInput.setInputType(InputType.TYPE_CLASS_NUMBER); // 숫자 입력 전용
        layout.addView(tempoInput);

        builder.setView(layout);

        // "확인" 버튼 설정
        builder.setPositiveButton("확인", (dialog, which) -> {
            String songTitle = titleInput.getText().toString().trim();
            String artistName = artistInput.getText().toString().trim();
            String selectedInstrument = instrumentSpinner.getSelectedItem().toString();
            String tempoStr = tempoInput.getText().toString().trim();

            if (!songTitle.isEmpty() && !artistName.isEmpty() && !tempoStr.isEmpty()) {
                int tempo = Integer.parseInt(tempoStr);
                if (tempo < 5 || tempo > 300) {
                    // 템포가 유효하지 않으면 경고 메시지 표시
                    Toast.makeText(this, "템포는 5에서 300 사이의 값이어야 합니다", Toast.LENGTH_SHORT).show();
                } else {
                    currentTitle = songTitle;
                    currentArtist = artistName;
                    currentInstrument = selectedInstrument;
                    currentTempo = tempo; // 템포 저장
                    openGallery();
                }
            } else {
                // 입력값 누락 시 경고 메시지 표시
                Toast.makeText(this, "제목, 아티스트 이름, 템포를 모두 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        // "취소" 버튼 설정
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showEditDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("항목 수정");

        // 수정 레이아웃 생성
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        // 곡 제목 입력 필드
        final EditText nameInput = new EditText(this);
        nameInput.setText(item.getTitle());
        nameInput.setHint("곡 제목");
        layout.addView(nameInput);

        // 아티스트 이름 입력 필드
        final EditText artistInput = new EditText(this);
        artistInput.setText(item.getArtist()); // Instrument 필드가 현재 아티스트 이름을 저장한다고 가정
        artistInput.setHint("아티스트 이름");
        layout.addView(artistInput);

        // 악기 선택 Spinner 추가
        final Spinner instrumentSpinner = new Spinner(this);
        String[] instruments = {"일렉기타", "어쿠스틱 기타", "피아노", "베이스 기타", "드럼"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, instruments);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        instrumentSpinner.setAdapter(spinnerAdapter);
        instrumentSpinner.setSelection(spinnerAdapter.getPosition(item.getInstrument())); // 기존 선택 값으로 설정
        layout.addView(instrumentSpinner);

        // 템포 입력 필드
        final EditText tempoInput = new EditText(this);
        tempoInput.setText(String.valueOf(item.getTempo()));
        tempoInput.setHint("템포 (5-300)");
        tempoInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(tempoInput);

        builder.setView(layout);

        // "저장" 버튼 설정
        builder.setPositiveButton("저장", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();
            String newArtist = artistInput.getText().toString().trim();
            String newInstrument = instrumentSpinner.getSelectedItem().toString();
            String tempoStr = tempoInput.getText().toString().trim();

            if (!newName.isEmpty() && !newArtist.isEmpty() && !tempoStr.isEmpty()) {
                int newTempo = Integer.parseInt(tempoStr);
                if (newTempo < 5 || newTempo > 300) {
                    Toast.makeText(this, "템포는 5에서 300 사이의 값이어야 합니다", Toast.LENGTH_SHORT).show();
                } else {
                    item.setTitle(newName);
                    item.setInstrument(newInstrument);
                    item.setTempo(newTempo); // 템포 값 설정
                    adapter.notifyDataSetChanged(); // ListView 갱신
                }
            } else {
                Toast.makeText(this, "모든 필드를 올바르게 입력하세요", Toast.LENGTH_SHORT).show();
            }
        });

        // "취소" 버튼 설정
        builder.setNegativeButton("취소", (dialog, which) -> dialog.cancel());

        builder.show();
    }


    // 갤러리 열기
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES && resultCode == RESULT_OK) {
            List<Uri> imageUris = new ArrayList<>();
            if (data != null) {
                if (data.getClipData() != null) { // 여러 이미지 선택
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        imageUris.add(data.getClipData().getItemAt(i).getUri());
                    }
                } else if (data.getData() != null) { // 단일 이미지 선택
                    imageUris.add(data.getData());
                }
                addItemToList(currentTitle, currentArtist, currentInstrument, imageUris);
            }
        }
    }

    // ListView에 항목 추가
    private void addItemToList(String title, String artist, String instrument, List<Uri> imageUris) {
        Item newItem = new Item(title, artist, instrument, currentTempo, imageUris);
        itemList.add(newItem);
        adapter.notifyDataSetChanged();
    }

    private String generateFileName(String title, String artist, String instrument, int tempo, String extension) {
        // 특수 문자를 "_"로 치환하여 안전한 파일 이름 생성
        String safeTitle = title.replaceAll("[^a-zA-Z0-9._-]", "_");
        String safeArtist = artist.replaceAll("[^a-zA-Z0-9._-]", "_");
        String safeInstrument = instrument.replaceAll("[^a-zA-Z0-9._-]", "_");

        return safeTitle + "_" + safeArtist + "_" + safeInstrument + "_" + tempo + extension;
    }

    // 선택된 항목을 팝업으로 표시하는 메서드
    private void showItemDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("곡 정보");

        // 곡 정보를 보기 쉽게 메시지로 구성
        String message = "곡 제목: " + item.getTitle() + "\n" +
                "아티스트: " + item.getArtist() + "\n" +  // 아티스트 정보가 저장된 필드라고 가정
                "악기: " + item.getInstrument() + "\n" +
                "템포: " + item.getTempo() + " BPM";

        // 팝업 메시지 설정
        builder.setMessage(message);

        // "수정" 버튼 설정
        builder.setPositiveButton("수정", (dialog, which) -> {
            showEditDialog(item); // 수정 다이얼로그 호출
        });

        // "닫기" 버튼 설정
        builder.setNegativeButton("닫기", (dialog, which) -> dialog.dismiss());

        // "업로드" 버튼 설정
        builder.setNeutralButton("업로드", (dialog, which) -> {
            try {
                uploadImagesToServer(item.getImages(), item); // 업로드 메서드 호출
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to upload images", Toast.LENGTH_SHORT).show();
            }
        });

        builder.show();
    }

    private void extractAndSaveFiles(File zipFile, String mxlFileName, String wavFileName) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String fileName = zipEntry.getName();

                File saveDir;
                String newFileName;
                if (fileName.endsWith(".wav")) {
                    saveDir = new File(getExternalFilesDir(null), "AudioFiles");
                    newFileName = wavFileName; // 새 이름
                } else if (fileName.endsWith(".mxl")) {
                    saveDir = new File(getExternalFilesDir(null), "MXLFiles");
                    newFileName = mxlFileName; // 새 이름
                } else {
                    continue;
                }

                if (!saveDir.exists()) {
                    saveDir.mkdirs();
                }

                // 새 이름으로 파일 저장
                File extractedFile = new File(saveDir, newFileName);
                try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
                Log.d("FileExtract", "Saved file: " + extractedFile.getAbsolutePath());
            }
            zis.closeEntry();
            zipFile.delete(); // ZIP 파일 삭제 (선택 사항)
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Failed to extract ZIP file", Toast.LENGTH_SHORT).show());
        }
    }


    private void extractZipFile(File zipFile) {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String fileName = zipEntry.getName();

                // 저장 디렉터리 설정
                File saveDir;
                if (fileName.endsWith(".wav")) {
                    saveDir = new File(getExternalFilesDir(null), "AudioFiles");
                    if (!saveDir.exists()) {
                        saveDir.mkdirs(); // 폴더가 없으면 생성
                    }
                } else if (fileName.endsWith(".mxl")) {
                    saveDir = new File(getExternalFilesDir(null), "MXLFiles");
                    if (!saveDir.exists()) {
                        saveDir.mkdirs(); // 폴더가 없으면 생성
                    }
                } else {
                    continue; // WAV나 MXL 파일이 아니면 스킵
                }

                if (!saveDir.exists()) {
                    saveDir.mkdirs();
                }

                // 파일 저장
                File extractedFile = new File(saveDir, fileName);
                try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }

                Log.d("FileExtract", "Saved file: " + extractedFile.getAbsolutePath());
            }
            zis.closeEntry();
            zipFile.delete();
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(this, "Failed to extract ZIP file", Toast.LENGTH_SHORT).show());
        }
    }

    private void showProgressDialog(String message) {
        // AlertDialog 빌더 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("진행 중");
        builder.setMessage(message);
        builder.setCancelable(false); // 사용자가 수동으로 닫지 못하게 설정

        // Dialog 표시
        progressDialog = builder.create();
        progressDialog.show();
    }

    private void updateProgressDialog(String newMessage) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.setMessage(newMessage);
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    // 이미지를 Flask 서버로 업로드하는 메서드
    private void uploadImagesToServer(List<Uri> imageUriList, Item item) throws IOException {
        showProgressDialog("업로드를 시작합니다. 잠시만 기다려주세요...");
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS) // 연결 타임아웃
                .readTimeout(60, TimeUnit.SECONDS)    // 읽기 타임아웃 (서버 응답 대기 시간)
                .writeTimeout(60, TimeUnit.SECONDS)   // 쓰기 타임아웃 (데이터 업로드 시간)
                .build();

        // multipart/form-data로 여러 파일 전송
        MultipartBody.Builder requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        requestBodyBuilder.addFormDataPart("tempo", String.valueOf(item.getTempo()));
        int instrumentCode = mapInstrumentToCode(item.getInstrument());
        requestBodyBuilder.addFormDataPart("instrument", String.valueOf(instrumentCode));

        // 이미지 URI 리스트 순회
        for (Uri imageUri : imageUriList) {
            String filePath = getRealPathFromURI(imageUri);
            if (filePath == null) {
                Log.e("UploadError", "File path is null for image: " + imageUri.toString());
                Toast.makeText(this, "파일 경로가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            File file = new File(filePath);

            // 각 파일을 MultipartBody에 추가
            requestBodyBuilder.addFormDataPart("files[]", file.getName(),
                    RequestBody.create(MediaType.parse("image/png"), file));
        }

        RequestBody requestBody = requestBodyBuilder.build();

        // 서버의 URL 설정 (Flask 서버)
        Request request = new Request.Builder()
                .url("http://172.20.10.3:5001/upload") // Flask 서버 URL
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();  // 콘솔에 전체 스택 트레이스 출력

                hideProgressDialog();

                // 긴 메시지는 Logcat에 출력
                Log.e("UploadError", "Failed to upload images: " + e.getMessage());

                // Toast에서는 짧은 메시지만 출력
                runOnUiThread(() -> Toast.makeText(SheetUploadActivity.this, "Failed to upload images. Check Logcat for details.", Toast.LENGTH_SHORT).show());
            }



            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    updateProgressDialog("결과를 다운로드 중입니다...");
                    // 서버에서 반환된 WAV 파일 데이터 읽기
//                    byte[] wavBytes = response.body().bytes();
//
//                    String safeFileName = item.getTitle().replaceAll("[^a-zA-Z0-9._-]", "_") + "_" +
//                            item.getArtist().replaceAll("[^a-zA-Z0-9._-]", "_") + "_" +
//                            mapInstrumentToEng(item.getInstrument()).replaceAll("[^a-zA-Z0-9._-]", "_") + "_" +
//                            item.getTempo() + ".wav";
//
//                    // 앱 전용 디렉터리 내에 저장
//                    File appSpecificDir = new File(getExternalFilesDir(null), "AudioFiles"); // "AudioFiles" 폴더 생성
//                    if (!appSpecificDir.exists()) {
//                        appSpecificDir.mkdirs(); // 폴더가 없으면 생성
//                    }
//
//                    // WAV 파일 저장 위치 설정
//                    File wavFile = new File(appSpecificDir, safeFileName);
//                    // 파일 저장
//                    try (FileOutputStream fos = new FileOutputStream(wavFile)) {
//                        fos.write(wavBytes);
//                    }
//
//                    // UI 업데이트: 사용자에게 저장 경로 알림
//                    runOnUiThread(() -> Toast.makeText(SheetUploadActivity.this, "WAV saved: " + wavFile.getAbsolutePath(), Toast.LENGTH_LONG).show());

                    byte[] zipBytes = response.body().bytes();

                    File zipFile = new File(getExternalFilesDir(null), "output_files.zip");
                    try (FileOutputStream fos = new FileOutputStream(zipFile)) {
                        fos.write(zipBytes);
                    }

                    // MXL 및 WAV 파일 이름 생성
                    String mxlFileName = generateFileName(
                            item.getTitle(),
                            item.getArtist(),
                            mapInstrumentToEng(item.getInstrument()),
                            item.getTempo(),
                            ".mxl"
                    );

                    String wavFileName = generateFileName(
                            item.getTitle(),
                            item.getArtist(),
                            mapInstrumentToEng(item.getInstrument()),
                            item.getTempo(),
                            ".wav"
                    );

                    extractAndSaveFiles(zipFile, mxlFileName, wavFileName);

                    runOnUiThread(() -> Toast.makeText(SheetUploadActivity.this, "Files downloaded and extracted", Toast.LENGTH_LONG).show());
                    hideProgressDialog();
                } else {
                    // 서버에서 응답 실패
                    runOnUiThread(() -> Toast.makeText(SheetUploadActivity.this, "Failed to process images", Toast.LENGTH_SHORT).show());
                    hideProgressDialog();
                }
            }
        });
    }


    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        try (Cursor cursor = getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(columnIndex);
            }
        }
        return null;
    }

    private int mapInstrumentToCode(String instrument) {
        switch (instrument) {
            case "일렉기타":
                return 1;
            case "어쿠스틱 기타":
                return 2;
            case "피아노":
                return 3;
            case "베이스 기타":
                return 4;
            case "드럼":
                return 5;
            default:
                return 0; // 기본값, 매핑되지 않은 악기
        }
    }

    private String mapInstrumentToEng(String instrument) {
        switch (instrument) {
            case "일렉기타":
                return "ElectricGuitar";
            case "어쿠스틱 기타":
                return "AccousticGuitar";
            case "피아노":
                return "Piano";
            case "베이스 기타":
                return "BassGuitar";
            case "드럼":
                return "Drum";
            default:
                return "Unknown"; // 기본값, 매핑되지 않은 악기
        }
    }

    // Item 클래스 정의
    public static class Item {
        private String title;
        private String artist;
        private String instrument;
        private int tempo;
        private final List<Uri> images;

        public Item(String title, String artist, String instrument, int tempo, List<Uri> images) {
            this.title = title;
            this.artist = artist;
            this.instrument = instrument;
            this.images = images;
            this.tempo = tempo;
        }

        @Override
        public String toString() {
            return title; // ListView에 표시될 항목 이름
        }

        public String getTitle() {
            return title;
        }

        public String getArtist() {
            return artist;
        }

        public int getTempo() {
            return tempo;
        }


        public String getInstrument(){
            return instrument;
        }

        public void setTitle(String title) { // 이름을 설정하는 메서드 추가
            this.title = title;
        }

        public void setArtist(String artist) {
            this.artist = artist;
        }

        public void setTempo(int tempo) {
            this.tempo = tempo;
        }

        public void setInstrument(String instrument) {
            this.instrument = instrument;
        }


        public List<Uri> getImages() {
            return images;
        }
    }
}

