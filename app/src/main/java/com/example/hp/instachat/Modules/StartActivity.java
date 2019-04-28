package com.example.hp.instachat.Modules;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.hp.instachat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity {

    private Button startNewuser;
    private Button loginUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        startNewuser = findViewById(R.id.start_newuser);
        loginUser = findViewById(R.id.start_login);

        startNewuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent newuserintent = new Intent(StartActivity.this,RegisterActivity.class);
                startActivity(newuserintent);
            }
        });
        loginUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newuserintent = new Intent(StartActivity.this,LoginActivity.class);
                startActivity(newuserintent);

            }
        });

    }


}
