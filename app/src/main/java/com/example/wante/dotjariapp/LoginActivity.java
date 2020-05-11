package com.example.wante.dotjariapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


    private EditText userEmail, userPassword;
    private TextView countInfo;
    private Button btnLogin;
    private Button btnEmailReg;
    private int counter = 5;
    private FirebaseAuth firebaseAuth;
    private TextView forgotPassword;

    private LinearLayout loginLinear;



    @Override
    protected void onStart() {
        super.onStart();
        //현재 유저를 user 담는다.
        FirebaseUser user = firebaseAuth.getCurrentUser();

        // 현재유저가 널값이 아니면 메인화면으로 이동한다.
        if(user != null) {
            Intent intent = new Intent(LoginActivity.this, OnCreateLogoActivity.class);
            startActivity(intent);
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        userEmail = (EditText) findViewById(R.id.et_login_email);
        userPassword = (EditText) findViewById(R.id.et_login_password);
        btnLogin = (Button) findViewById(R.id.btn_login_login);
        btnEmailReg = (Button) findViewById(R.id.btn_login_email_reg);
        forgotPassword = (TextView) findViewById(R.id.tv_forgotPassword);

        loginLinear = findViewById(R.id.register_linear);





        //파이어베이스  공유 인스턴스 받아오기
        firebaseAuth = FirebaseAuth.getInstance();



        //로그인버튼
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validate(userEmail.getText().toString(), userPassword.getText().toString());
            }
        });

        //이메일 회원가입버튼
        btnEmailReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        //비밀번호 찾기 버튼
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, PasswordActivity.class));
            }
        });


    }

    //검증 메소드
    private void validate (String userName, String userPassword) {

    firebaseAuth.signInWithEmailAndPassword(userName, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                checkEmailVerification();
                finish();
            } else {
                loginLinear.startAnimation(AnimationUtils.loadAnimation(LoginActivity.this,R.anim.shake));
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
            }
        }
    });


  //데이터베이스 연동전 임시 코드
   /*     if((userName.equals("Admin")) && (userPassword.equals("1234"))){
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
        }
        else{
            counter --;

            countInfo.setText("로그인 시도 :" + String.valueOf(counter));

            if(counter ==0) {
                btnLogin.setEnabled(false); //로그인 버튼을  비활성화 시킨다
            }
        }*/
    }

//email 체크하기
    private void checkEmailVerification(){
        FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
        Boolean emailflag = firebaseUser.isEmailVerified(); //검증된이메일

        startActivity(new Intent(LoginActivity.this, LoginIntroActivity.class));

/*
        if(emailflag) {
            finish();
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        }else {
            Toast.makeText(this, "이메일에서 인증해주세요.", Toast.LENGTH_SHORT).show();
            firebaseAuth.signOut();

        }*/
    }


}