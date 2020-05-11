package com.example.wante.dotjariapp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class OnCreateLogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_create_logo);


        // 로그인 되어있을때 처음 화면을 킬때 로고화면을 일정시간동안 보여준다.
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(OnCreateLogoActivity.this, HomeActivity.class);
                startActivity(i);
                finish();
            }
        }, 1000);

    }
}
