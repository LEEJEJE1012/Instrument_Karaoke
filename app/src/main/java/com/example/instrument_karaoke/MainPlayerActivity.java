package com.example.instrument_karaoke;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.Manifest;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import java.io.File;

public class MainPlayerActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private boolean permissionToRecordAccepted = false;
    private String[] permissions = {Manifest.permission.RECORD_AUDIO};
    private static final int FILE_SELECT_CODE = 1;

    // TextViews for displaying selected file names
    private TextView[] fileTextViews;

    // Buttons for selecting files
    private Button[] selectButtons;

    private Button playButton;
    private Button playandrecordButton;
    private Button pauseButton;
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    // Mediaplayers for playing wav files
    private MediaPlayer[] mediaPlayers;

    // App-specific directory
    private File appSpecificDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_player);
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        appSpecificDir = new File(getExternalFilesDir(null), "Recorded");
        if (!appSpecificDir.exists()) {
            appSpecificDir.mkdirs(); // Create directory if it doesn't exist
        }

        // Initialize TextViews
        fileTextViews = new TextView[]{
                findViewById(R.id.textview_mainplayer_inst1),
                findViewById(R.id.textview_mainplayer_inst2),
                findViewById(R.id.textview_mainplayer_inst3),
                findViewById(R.id.textview_mainplayer_inst4),
                findViewById(R.id.textview_mainplayer_inst5)
        };

        // Initialize MediaPlayer array
        mediaPlayers = new MediaPlayer[fileTextViews.length];

        // Initialize Buttons
        selectButtons = new Button[]{
                findViewById(R.id.button_mainplayer_select1),
                findViewById(R.id.button_mainplayer_select2),
                findViewById(R.id.button_mainplayer_select3),
                findViewById(R.id.button_mainplayer_select4),
                findViewById(R.id.button_mainplayer_select5)
        };

        // Set click listeners for file selection
        for (int i = 0; i < selectButtons.length; i++) {
            int index = i; // Capture the index for the lambda
            selectButtons[i].setOnClickListener(v -> showFilePickerDialog(index));
        }

        playButton = findViewById(R.id.button_mainplayer_play);
        playButton.setOnClickListener(v -> playSelectedFiles());
        playandrecordButton = findViewById(R.id.button_mainplayer_playandrecord);
        playandrecordButton.setOnClickListener(v -> showRecordingDialog());

    }
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!permissionToRecordAccepted) {
                Toast.makeText(this, "권한 거부됨. 앱을 종료합니다.", Toast.LENGTH_SHORT).show();
                finish(); // 권한 거부 시 앱 종료
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void playSelectedFiles() {
        // Stop and release any existing MediaPlayer instances
        releaseAllMediaPlayers();

        // Initialize MediaPlayer for each selected file
        for (int i = 0; i < fileTextViews.length; i++) {
            String fileName = fileTextViews[i].getText().toString();
            if (!fileName.equals("선택된 파일 없음")) {
                File audioFile = new File(getExternalFilesDir(null), "AudioFiles/" + fileName);
                if (audioFile.exists()) {
                    try {
                        mediaPlayers[i] = new MediaPlayer();
                        mediaPlayers[i].setDataSource(audioFile.getAbsolutePath());
                        mediaPlayers[i].prepare(); // Prepare MediaPlayer
                        mediaPlayers[i].start();   // Start playback
                    } catch (Exception e) {
                        Toast.makeText(this, "Error playing file: " + fileName, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "File not found: " + fileName, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void releaseAllMediaPlayers() {
        for (int i = 0; i < mediaPlayers.length; i++) {
            if (mediaPlayers[i] != null) {
                mediaPlayers[i].stop();
                mediaPlayers[i].release();
                mediaPlayers[i] = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseAllMediaPlayers(); // Release resources on activity destruction
    }




    private void showFilePickerDialog(int textViewIndex) {
        // App-specific directory
        File appSpecificDir = new File(getExternalFilesDir(null), "AudioFiles");

        // Get all WAV files in the directory
        File[] wavFiles = appSpecificDir.listFiles((dir, name) -> name.endsWith(".wav"));

        if (wavFiles == null || wavFiles.length == 0) {
            Toast.makeText(this, "No WAV files found in AudioFiles directory.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a list of file names
        String[] fileNames = new String[wavFiles.length];
        for (int i = 0; i < wavFiles.length; i++) {
            fileNames[i] = wavFiles[i].getName();
        }

        // Show a dialog with the file list
        new AlertDialog.Builder(this)
                .setTitle("Select a WAV file")
                .setItems(fileNames, (dialog, which) -> {
                    // Update the corresponding TextView with the selected file name
                    fileTextViews[textViewIndex].setText(fileNames[which]);
                    fileTextViews[textViewIndex].setTextColor(getResources().getColor(android.R.color.black, null));
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    private void startPlaybackAndRecording(String fileName) {
        playSelectedFiles(); // Play selected WAV files

        File outputFile = new File(getExternalFilesDir(null), "Recorded/" + fileName + ".wav");
        try {
            // Initialize MediaRecorder
            if (mediaRecorder != null) {
                mediaRecorder.release();
            }
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(outputFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;

            Toast.makeText(this, "Recording started.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to start recording.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRecordingDialog() {
        // Inflate custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_record, null);

        // Initialize dialog views
        EditText editTextFileName = dialogView.findViewById(R.id.editTextFileName);
        Button buttonStartRecording = dialogView.findViewById(R.id.buttonStartRecording);
        Button buttonStopRecording = dialogView.findViewById(R.id.buttonStopRecording);

        // Build the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false) // Prevent the dialog from being dismissed
                .create();

        // Start Recording Button
        buttonStartRecording.setOnClickListener(v -> {
            String fileName = editTextFileName.getText().toString().trim();
            if (fileName.isEmpty()) {
                Toast.makeText(this, "Please enter a file name.", Toast.LENGTH_SHORT).show();
            } else {
                startPlaybackAndRecording(fileName);
            }
        });

        // Stop Recording Button
        buttonStopRecording.setOnClickListener(v -> {
            stopRecording();
            dialog.dismiss(); // Dismiss the dialog after stopping the recording
        });

        dialog.show();
    }

    private void stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
                isRecording = false;

                Toast.makeText(this, "Recording stopped and saved.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to stop recording.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            // Determine the index of the button/textview pair
            int index = requestCode - FILE_SELECT_CODE;

            if (index >= 0 && index < fileTextViews.length) {
                // Get the file name and update the corresponding TextView
                String fileName = data.getData().getLastPathSegment();
                fileTextViews[index].setText(fileName);
            }
        } else {
            Toast.makeText(this, "File selection cancelled.", Toast.LENGTH_SHORT).show();
        }
    }


}
