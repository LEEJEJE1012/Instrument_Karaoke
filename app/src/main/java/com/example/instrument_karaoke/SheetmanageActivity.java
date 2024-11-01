package com.example.instrument_karaoke;

import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SheetmanageActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 1;
    private static final int PICK_IMAGES = 1;
    private List<Item> itemList = new ArrayList<>();
    private ArrayAdapter<Item> adapter;
    private String currentItemName = null;
    private ImageView imageView;
    private Uri imageUri;
    private List<Uri> imageUriList = new ArrayList<>();
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sheetmanage);

        // 시스템 바 여백 추가
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ListView 설정
        ListView listView = (ListView)findViewById(R.id.listview_sheetmanage_sheetlist);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, itemList);
        listView.setAdapter(adapter);

        // ListView 항목 클릭 리스너 설정
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Item selectedItem = itemList.get(position);
            showItemDialog(selectedItem); // 클릭된 항목을 팝업으로 표시
        });


        // 버튼 설정
        Button addButton = (Button)findViewById(R.id.button_sheetmanage_makesheet); // activity_sheetmanage.xml에 버튼 추가 필요
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

        // 레이아웃을 생성하여 두 개의 EditText 추가
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

        builder.setView(layout);

        // "확인" 버튼 설정
        builder.setPositiveButton("확인", (dialog, which) -> {
            String songTitle = titleInput.getText().toString().trim();
            String artistName = artistInput.getText().toString().trim();

            if (!songTitle.isEmpty() && !artistName.isEmpty()) {
                // 제목과 아티스트 이름을 모두 입력한 경우에만 갤러리 열기
                currentItemName = songTitle + " - " + artistName; // 예: "곡 제목 - 아티스트"
                openGallery();
            } else {
                // 제목 또는 아티스트 이름을 입력하지 않으면 경고 메시지 표시
                Toast.makeText(this, "제목과 아티스트 이름을 모두 입력하세요", Toast.LENGTH_SHORT).show();
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

        // 이름 입력 필드
        final EditText nameInput = new EditText(this);
        nameInput.setText(item.getName());
        layout.addView(nameInput);

        builder.setView(layout);

        // "저장" 버튼 설정
        builder.setPositiveButton("저장", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();
            if (!newName.isEmpty()) {
                item.setName(newName); // 새로운 이름 설정
                adapter.notifyDataSetChanged(); // ListView 갱신
            } else {
                Toast.makeText(this, "이름을 입력하세요", Toast.LENGTH_SHORT).show();
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
                addItemToList(currentItemName, imageUris);
            }
        }
    }

    // ListView에 항목 추가
    private void addItemToList(String name, List<Uri> imageUris) {
        Item newItem = new Item(name, imageUris);
        itemList.add(newItem);
        adapter.notifyDataSetChanged();
    }

    // 선택된 항목을 팝업으로 표시하는 메서드
    private void showItemDialog(Item item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getName());

        // 팝업 메시지 설정
        builder.setMessage("선택한 곡 정보입니다.");

        // "수정" 버튼 설정
        builder.setPositiveButton("수정", (dialog, which) -> {
            showEditDialog(item); // 예시로 수정 다이얼로그를 띄우는 메서드 호출
        });

        // "닫기" 버튼 설정
        builder.setNegativeButton("닫기", (dialog, which) -> dialog.dismiss());

        builder.setNeutralButton("업로드", (dialog, which) -> {
            try {
                uploadImagesToServer(item.getImages()); // 업로드 메서드 호출
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to upload images", Toast.LENGTH_SHORT).show();
            }
        });


        builder.show();
    }

    // 이미지를 Flask 서버로 업로드하는 메서드
    private void uploadImagesToServer(List<Uri> imageUriList) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Uri imageUri = imageUriList.get(0);

        String filePath = getRealPathFromURI(imageUri);
        if (filePath == null) {
            Log.e("UploadError", "File path is null");
            Toast.makeText(this, "파일 경로가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        File file = new File(filePath);

        // multipart/form-data로 파일 전송
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(),
                        RequestBody.create(MediaType.parse("image/png"), file))
                .build();

        // 서버의 URL 설정 (Flask 서버)
        Request request = new Request.Builder()
                .url("http://10.0.2.2:5000/upload")  // Flask 서버 URL
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();  // 콘솔에 전체 스택 트레이스 출력

                // 긴 메시지는 Logcat에 출력
                Log.e("UploadError", "Failed to upload image: " + e.getMessage());

                // Toast에서는 짧은 메시지만 출력
                runOnUiThread(() -> Toast.makeText(SheetmanageActivity.this, "Failed to upload image. Check Logcat for details.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    byte[] musicXMLBytes = response.body().bytes();
                    File mxlFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "test.mxl");

                    // 파일 저장
                    try (FileOutputStream fos = new FileOutputStream(mxlFile)) {
                        fos.write(musicXMLBytes);
                    }

                    runOnUiThread(() -> Toast.makeText(SheetmanageActivity.this, "MusicXML saved: " + mxlFile.getAbsolutePath(), Toast.LENGTH_LONG).show());
                } else {
                    runOnUiThread(() -> Toast.makeText(SheetmanageActivity.this, "Failed to process image", Toast.LENGTH_SHORT).show());
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

    // Item 클래스 정의
    public static class Item {
        private String name;
        private final List<Uri> images;

        public Item(String name, List<Uri> images) {
            this.name = name;
            this.images = images;
        }

        @Override
        public String toString() {
            return name; // ListView에 표시될 항목 이름
        }

        public String getName() {
            return name;
        }

        public void setName(String name) { // 이름을 설정하는 메서드 추가
            this.name = name;
        }

        public List<Uri> getImages() {
            return images;
        }
    }
}

