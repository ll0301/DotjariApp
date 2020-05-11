package com.example.wante.dotjariapp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class LoginIntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_intro);

        // 아이디를 치고 로그인할 때에 일정시간동안 인트로 화면을 띄워준다.

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(LoginIntroActivity.this, HomeActivity.class);

                startActivity(i);
                finish();
            }
        }, 1500);



    }
}
