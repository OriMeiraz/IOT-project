package com.example.tutorial6;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class Login extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button LogIn = (Button) findViewById(R.id.btn_login);
        Button SignUp = (Button) findViewById(R.id.btn_signup);

        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickLogIn();
            }
        });

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickSignUp();
            }
        });


    }
    private void ClickLogIn(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void ClickSignUp(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
