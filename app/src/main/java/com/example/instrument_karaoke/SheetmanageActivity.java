package com.example.instrument_karaoke;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class SheetmanageActivity extends AppCompatActivity {

    private static final int PICK_IMAGES = 1;
    private List<Item> itemList = new ArrayList<>();
    private ArrayAdapter<Item> adapter;
    private String currentItemName = null;

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

        // 버튼 설정
        Button addButton = (Button)findViewById(R.id.button_sheetmanage_makesheet); // activity_sheetmanage.xml에 버튼 추가 필요
        addButton.setOnClickListener(view -> showNameInputDialog());
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

    // Item 클래스 정의
    public static class Item {
        private final String name;
        private final List<Uri> images;

        public Item(String name, List<Uri> images) {
            this.name = name;
            this.images = images;
        }

        @Override
        public String toString() {
            return name; // ListView에 표시될 항목 이름
        }

        public List<Uri> getImages() {
            return images;
        }
    }
}

