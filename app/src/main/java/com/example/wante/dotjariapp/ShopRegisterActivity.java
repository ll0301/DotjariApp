package com.example.wante.dotjariapp;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShopRegisterActivity extends AppCompatActivity {


    private TextView shopAddress, shopTime;
    private EditText shopName, shopIntro, shopPhoneNumber, shopAddressPlus;
    private Spinner spShopType, spShopChatTime;
    private ArrayAdapter adpaterShopType, adpaterChatType;


    //시간선택 피커
    final int DIALOG_TIME = 2;


    private String sName, sPhone, sTime, sIntro,
          sType, sChatTime, sAddress, sAddressPlus;

    private Button btnConfirm, btnCancel;

    private FirebaseStorage firebaseStorage;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private FirebaseUser fuser;



    private  static final int IMAGE_REQUEST = 1;
    private  Uri imageUri;
    private StorageTask uploadTask;

    //시간피커
    private Dialog dialog;
    private Dialog dialogDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_register);
        setupView();


        // 지도로 이동
        // 맵으로 이동
        shopAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShopRegisterActivity.this, MapsActivity.class);
                ShopRegisterActivity.this.startActivity(intent);
            }
        });
        //맵의 주소를 넣어준다.
        Intent getMapAddress = getIntent();
        if (getMapAddress != null) {
            String getShopAddress = getMapAddress.getStringExtra("putAddress");
            shopAddress.setText(getShopAddress);
        }


        // 파이어베이스
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();

        storageReference = firebaseStorage.getReference("uploads");
        fuser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());





/*        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ShopItem shopItem = dataSnapshot.getValue(ShopItem.class);

                if(shopItem.getShopImgUrl()==null) {
                } else { Glide.with(getApplicationContext()).load(shopItem.getShopImgUrl()).into(shopImg);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/


/*        //티켓판매 체크박스 체크시
        cbShopTicket.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    clShopTicket.setVisibility(View.VISIBLE);
                } else {
                    clShopTicket.setVisibility(View.GONE);

                }
            }
        });*/



        // 운영시간 선택 피커
        shopTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeDialogStart();
            }
        });
/// 운영시간선택



        //확인버튼
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendShopData();
                finish();
                Toast.makeText(ShopRegisterActivity.this, "업체등록완료", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ShopRegisterActivity.this, MoreActivity.class));
            }
        });


        //취소버튼
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    onBackPressed();
            }
        });


/*        //카메라버튼
        shopImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openImage();
            }
        });*/

    }





    //데이트피커 다이얼로그
    private void dateDialogStart() {
        //티켓종료날짜선택
        final Dialog dialogDate = new Dialog(this);  //다이얼로그 객체생성
        dialogDate.setContentView(R.layout.shop_ticket_date_picker);  // 다이얼로그 화면등록
        final Button dateBtnConfirm = (Button) dialogDate.findViewById(R.id.btn_ticket_date_confirm);
        final Button dateBtnCancel = (Button) dialogDate.findViewById(R.id.btn_ticket_date_cancel);
        final DatePicker toDate = (DatePicker) dialogDate.findViewById(R.id.ticket_date_picker);
        dialogDate.show();
        dialogDate.setOwnerActivity(ShopRegisterActivity.this);
        dialogDate.setCanceledOnTouchOutside(true);


    }


    // 시간피커 다이얼로그
    private void timeDialogStart() {
        //시간선택다이얼로그
        final Dialog dialog = new Dialog(this);  //다이얼로그 객체생성
        dialog.setContentView(R.layout.shoptime_picker);  // 다이얼로그 화면등록
        Button timeBtnConfirm = (Button) dialog.findViewById(R.id.btn_shopTime_confirm);
        Button timeBtnCancel = (Button) dialog.findViewById(R.id.btn_shopTime_cancel);
       final TimePicker toTime = (TimePicker) dialog.findViewById(R.id.timePicker_to);
        final TimePicker fromTime = (TimePicker) dialog.findViewById(R.id.timePicker_from);


        dialog.show();
        dialog.setOwnerActivity(ShopRegisterActivity.this);
        dialog.setCanceledOnTouchOutside(true);
        toTime.setMinute(0);
        toTime.setHour(12);
        fromTime.setHour(12);
        fromTime.setMinute(0);

        //확인버튼
        timeBtnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int toHour = toTime.getHour();
                int toMinute = toTime.getMinute();
                int fromHour = fromTime.getHour();
                int fromMinute = fromTime.getMinute();

                shopTime.setText(fromHour +"시" + fromMinute+"분"+" ~ "+toHour+"시"+toMinute+"분");

                dialog.dismiss();

            }
        });
        // 취소버튼
        timeBtnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });



    }



    // 데이터보내기

    private  void sendShopData() {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

        databaseReference = firebaseDatabase.getReference().child("Users").child(fuser.getUid()).child("Shops");


        sName = shopName.getText().toString();
        sPhone = shopPhoneNumber.getText().toString();
        sTime = shopTime.getText().toString();
        sIntro = shopIntro.getText().toString();
        sAddress = shopAddress.getText().toString();
        sAddressPlus = shopAddressPlus.getText().toString();

        sType = spShopType.getSelectedItem().toString();
        sChatTime = spShopChatTime.getSelectedItem().toString();

        String shopId = fuser.getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("shopName", sName);
        hashMap.put("shopTime", sTime);
        hashMap.put("shopIntro", sIntro);
        hashMap.put("shopAddress", sAddress);
        hashMap.put("shopAddressPlus", sAddressPlus);
        hashMap.put("shopPhoneNumber", sPhone);
        hashMap.put("shopType", sType); //
        hashMap.put("shopChatTime", sChatTime); //
        databaseReference.updateChildren(hashMap);



/*        //업체회원 전환
      DatabaseReference reference = firebaseDatabase.getReference().child("Users").child(firebaseUser.getUid());

       String sShopReg = firebaseUser.getUid();

        HashMap<String, Object> map = new HashMap<>();
        map.put("shopReg","true");
        reference.updateChildren(map);*/

  /*      ShopItem shopItem = new ShopItem(shopid,sName,sUserName,sIntro,sImgUrl,sAddress,sAddressPlus,sPhone,sTime,sChatTime,sType);*/


    }


    private  void setupView() {
        shopName = findViewById(R.id.et_shop_profile_name);
        shopIntro = findViewById(R.id.et_shop_profile_intro);
        shopAddress = findViewById(R.id.tv_shop_profile_district);
        shopAddressPlus = findViewById(R.id.et_shop_profile_plusAddress);
        shopPhoneNumber = findViewById(R.id.et_shop_profile_phone);
        shopTime = findViewById(R.id.tv_shop_profile_time);

        //스피너   샵타입과 상담시간 결정 스피너
        spShopType = findViewById(R.id.sp_shopType);
        spShopChatTime = findViewById(R.id.sp_shop_chatTime);
        adpaterShopType = ArrayAdapter.createFromResource(this,R.array.shopType, android.R.layout.simple_spinner_dropdown_item);
        adpaterChatType = ArrayAdapter.createFromResource(this,R.array.chatTime, android.R.layout.simple_spinner_dropdown_item);
        spShopType.setAdapter(adpaterShopType);
        spShopChatTime.setAdapter(adpaterChatType);



        btnConfirm = findViewById(R.id.register_ok_btn);
        btnCancel = findViewById(R.id.write_cancel_btn);
    }

/*    //사진첩열고 가져오기
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
                        sImgUrl = downloadUri.toString();

                        databaseReference = FirebaseDatabase.getInstance().getReference("Shops").child(fuser.getUid());
                        HashMap<String, Object> maps = new HashMap<>();
                        maps.put("shopImgUri", sImgUrl);
                        databaseReference.updateChildren(maps);


                        Glide.with(ShopRegisterActivity.this).load(sImgUrl).into(shopImg);

*//*
                        //핸들러 종료되고 프로그래스바 숨김
                        mHandler.removeMessages(0);
                        progressBar.setVisibility(View.GONE);
*//*



                    }else {
                        Toast.makeText(ShopRegisterActivity.this, "업로드실패", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ShopRegisterActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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

    }*/


}
