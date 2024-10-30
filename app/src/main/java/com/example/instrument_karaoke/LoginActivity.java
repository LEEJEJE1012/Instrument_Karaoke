package com.example.instrument_karaoke;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.*;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        EditText Email = findViewById(R.id.Login_InsertEmail);
        EditText Password = findViewById(R.id.Login_InsertPassword);
        Button SigninButton = findViewById(R.id.button_login_signin);
        Button ForgotPasswordButton = findViewById(R.id.button_login_forgotpwd);
    }
}
