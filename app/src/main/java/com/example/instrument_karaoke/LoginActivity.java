package com.example.instrument_karaoke;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.*;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        Button signin_button = (Button) findViewById(R.id.button_login_signin);
        signin_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        EditText Email = findViewById(R.id.Login_InsertEmail);
        EditText Password = findViewById(R.id.Login_InsertPassword);
        Button SigninButton = findViewById(R.id.button_login_signin);
        Button ForgotPasswordButton = findViewById(R.id.button_login_forgotpwd);
    }
}
