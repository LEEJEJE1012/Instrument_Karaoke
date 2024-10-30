package com.example.instrument_karaoke;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.*;
import android.content.Intent;
import android.view.View;

public class InitialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_initial);

        Button login_button = (Button) findViewById(R.id.button_initial_login);
        login_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        Button signin_button = findViewById(R.id.button_initial_register);
        TextView Maintextview = findViewById(R.id.Main_Title);
    }
}