package com.example.instrument_karaoke;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.*;
import android.content.Intent;
import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        EditText Email = findViewById(R.id.Login_InsertEmail);
        EditText Password = findViewById(R.id.Login_InsertPassword);
        Button SigninButton = findViewById(R.id.Login_Signin);
        Button ForgotPasswordButton = findViewById(R.id.Login_ForgotPassword);
    }
}
