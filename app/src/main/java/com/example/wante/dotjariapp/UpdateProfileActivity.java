package com.example.wante.dotjariapp;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wante.dotjariapp.Item.UserProfile;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {

    private CircleImageView editProfileImage;
    private TextView email;
    private EditText username, status;
    private Button confirm, cancel;
    private ConstraintLayout shop_info_edit;



   private String getUsername, getUserstatus, getUserEmail,getImgUri, sName, sTime, sIntro, sAddress, sAddressPlus,
    sPhone,sType,sChatTime;
   private String mUri;


    private  DatabaseReference reference;
    private  FirebaseUser fuser;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private  static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;

    private ProgressBar progressBar;
    private Handler mHandler;

    private boolean refuseUser;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);



        // 데이터로딩 프로그래스바
        // 이미지를 가져올 때
        progressBar = findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);


        //스토리지
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        //변경할 프로필
        editProfileImage = findViewById(R.id.profile_edit_image);
        username = findViewById(R.id.et_profile_name);
        status = findViewById(R.id.et_profile_satus);
        email = findViewById(R.id.tv_profile_email);
        confirm = findViewById(R.id.user_profile_confirm);
       cancel = findViewById(R.id.user_profile_cancel);

        confirm.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {

               getUsername = username.getText().toString();
               getUserstatus = status.getText().toString();
               getUserEmail = email.getText().toString();


               reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
               HashMap<String, Object> hashMaps = new HashMap<>();
               hashMaps.put("userName", getUsername);
               hashMaps.put("userStatus", getUserstatus);
               hashMaps.put("userEmail",getUserEmail);
               hashMaps.put("search", getUsername.toLowerCase());
               reference.updateChildren(hashMaps);

               startActivity(new Intent(UpdateProfileActivity.this, MoreActivity.class));
               finish();
           }
       });

       cancel.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               onBackPressed();
               finish();
           }
       });


        //프로필이미지 클릭하면
        editProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImage();

                mHandler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                };

                mHandler.sendEmptyMessageDelayed(0,1000);

            }
        });

        //스토리지
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                username.setText(userProfile.getUserName());
                status.setText(userProfile.getUserStatus());
                email.setText(userProfile.getUserEmail());
                if(userProfile.getUserImgUri().equals("default")) {
                } else { Glide.with(getApplicationContext()).load(userProfile.getUserImgUri()).into(editProfileImage);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }  //---------------------------oncreate


    //사진첩열고 가져오기
    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);

    }



    private String getFileExtension (Uri uri) {
        //content resolver는 객체의 메소드를 통해 데이터 생성,검색,업데이트,삭제등을 할수있다.
        ContentResolver contentResolver = this.getContentResolver();
        // 확장자 알아내기
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    // 사진 업로드하기
    private  void uploadImage() {
        if (imageUri != null) {
            //currenttimemillis -> utc시간 1970년 1월1일 자정부터 현재까지 카운트된시간을 표시
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+ getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            //연속작업만들기
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("userImgUri", mUri);
                        reference.updateChildren(map);


                        //핸들러 종료되고 프로그래스바 숨김
                        mHandler.removeMessages(0);
                        progressBar.setVisibility(View.GONE);



                    }else {
                        Toast.makeText(UpdateProfileActivity.this, "업로드실패", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            Toast.makeText(this, "이미지를 선택하지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == IMAGE_REQUEST && resultCode == RESULT_OK
                &&data != null && data.getData() != null) {
            imageUri = data.getData();

            if(uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(this, "업로드진행중 ", Toast.LENGTH_SHORT).show();
            }else {
                uploadImage();
            }
        }

    }





}
