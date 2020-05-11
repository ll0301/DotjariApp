package com.example.wante.dotjariapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {



    private EditText userEmail, userName,userStatus, userPassword, userPasswordConfirm;
    private Button btnReg, btnCancel;
    private FirebaseAuth firebaseAuth;
    private CircleImageView userProfilePic;
    String email, name, status, password, passwordConfirm, imgUri;
    String ischat;
    //파이어베이스 스토리지
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private DatabaseReference myRef;
    private static int PICK_IMAGE = 123;
    Uri imagePath;

    private LinearLayout registerLinear;

    //이미지 startActivityForResult 에서 계산된 값이 넘어오면 호출된다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK && data.getData() != null){
                imagePath = data.getData() ;

                //mediastore 안드로이드에서 제공하는 미디어 db
            // getContentResolver contentprovider가 제공하는 데이터에 접근하기위해
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imagePath);
                userProfilePic.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setupUiViews();

    //파이어베이스 auth와 storage의  인스턴스를 얻어온다.
    firebaseAuth = FirebaseAuth.getInstance();
    firebaseStorage =FirebaseStorage.getInstance();

    storageReference = firebaseStorage.getReference();

    registerLinear = findViewById(R.id.register_linear);

// 회원가입버튼
        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validate()) {
                    //데이터베이스에 데이터 업로드 구현
                    String user_email = userEmail.getText().toString().trim();
                    String user_password = userPassword.getText().toString().trim();

                    //이메일과 비밀번호 생성
                    firebaseAuth.createUserWithEmailAndPassword(user_email, user_password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()) {
                                // 인증활용할거면 활성화 시키면 됨
                               // sendEmailVerivication();
                                sendUserData();
                                Toast.makeText(RegisterActivity.this, "회원가입 완료", Toast.LENGTH_SHORT).show();
                                firebaseAuth.signOut();
                                finish();
                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            }else {
                               registerLinear.startAnimation(AnimationUtils.loadAnimation(RegisterActivity.this,R.anim.shake));
                                Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
            }
        });

        //취소버튼
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                }
        });


        } // onCreate-----------------------------


    private void setupUiViews() {
        userEmail = (EditText) findViewById(R.id.et_register_email);
        userName = (EditText) findViewById(R.id.et_register_userName);
        userStatus = (EditText) findViewById(R.id.et_register_userStatus);
        userPassword = (EditText) findViewById(R.id.et_register_password);
        userPasswordConfirm = (EditText) findViewById(R.id.et_register_password_confirm);
        btnReg = (Button) findViewById(R.id.btn_register_register);
        btnCancel = (Button) findViewById(R.id.btn_register_cancel);
        userProfilePic = (CircleImageView) findViewById(R.id.myfriend_profilePic);
    }

    //회원가입 검증
    private Boolean validate() {
        Boolean result = false;
        email = userEmail.getText().toString();
        name = userName.getText().toString();
        status = userStatus.getText().toString();
        password = userPassword.getText().toString();
        passwordConfirm = userPasswordConfirm.getText().toString();


        if(name.isEmpty() || password.isEmpty() || email.isEmpty() || status.isEmpty() ) {
            registerLinear.startAnimation(AnimationUtils.loadAnimation(RegisterActivity.this,R.anim.shake));
            Toast.makeText(this, "빈 칸을 전부 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else if(!password.equals(passwordConfirm)){
            registerLinear.startAnimation(AnimationUtils.loadAnimation(RegisterActivity.this,R.anim.shake));
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
        } else if(password.length() < 6) {
            registerLinear.startAnimation(AnimationUtils.loadAnimation(RegisterActivity.this,R.anim.shake));
            Toast.makeText(this, "비밀번호는 6자 이상입니다.", Toast.LENGTH_SHORT).show();
        }

        else {
            result = true;
        }
        return result;
    }

    //회원가입 인증 이메일 보내기
    private  void sendEmailVerivication() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if(firebaseUser != null) {

            // 이메일 확인 보내기
            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()) {
                        // 유저데이터 보내기
                        sendUserData();
                        Toast.makeText(RegisterActivity.this, "회원가입 완료, 인증메일전송", Toast.LENGTH_SHORT).show();
                        firebaseAuth.signOut();
                        finish();
                        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    }else{
                        Toast.makeText(RegisterActivity.this, "인증메일전송 실패", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // 유저데이터를 보낸다 .
    private void sendUserData() {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            //데이터베이스에 데이터를 읽거나 쓰려면 데이터베이스 레퍼런스의 인스턴스가 필요하다.
            myRef= firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid());  // Uid 사용자 식별자
            DatabaseReference myRef0 = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("friendsList").child(firebaseAuth.getUid());
            DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(firebaseAuth.getUid()).child("MyRequestList").child(firebaseAuth.getUid());
            DatabaseReference myRef3 = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(firebaseAuth.getUid()).child("RequestList").child(firebaseAuth.getUid());

            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();


            //uid 담기
            String userid = firebaseUser.getUid();

            //해쉬맵을 이용하여 실시간데이터베이스에 저장한다.
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("userId",userid);
            hashMap.put("userName", name);
            hashMap.put("userEmail",email);
            hashMap.put("userStatus", status);
            hashMap.put("userImgUri", "default");
            hashMap.put("onOff","offline");
            hashMap.put("search", name.toLowerCase());
            myRef.setValue(hashMap);

        HashMap<String, String> hashMap2 = new HashMap<>();
        hashMap2.put("userId",userid);
        hashMap2.put("userEmail",email);
        hashMap2.put("userName", name);
        hashMap2.put("userStatus", status);
        hashMap2.put("userImgUri", "default");
        hashMap2.put("onOff","offline");
        hashMap2.put("search", name.toLowerCase());
            myRef0.setValue(hashMap2);


            // home으로 이동

            HashMap<String, Object> hash = new HashMap<>();
        hash.put("userId",userid);
        hash.put("userEmail",email);
        hash.put("userName", name);
        hash.put("userStatus", status);
        hash.put("userImgUri", "default");
        hash.put("onOff","offline");
        hash.put("search", name.toLowerCase());
            myRef2.setValue(hash);

            HashMap<String, Object> hash2 = new HashMap<>();
        hash2.put("userId",userid);
        hash2.put("userEmail",email);
        hash2.put("userName", name);
        hash2.put("userStatus", status);
        hash2.put("userImgUri", "default");
        hash2.put("onOff","offline");
        hash2.put("search", name.toLowerCase());
            myRef3.setValue(hash2);


    /*       DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(firebaseAuth.getUid()).child("friendsList");
            HashMap<String, Object> hashMaps = new HashMap<>();
            hashMaps.put(userid, "default");
            reference.updateChildren(hashMaps);*/



    }

}