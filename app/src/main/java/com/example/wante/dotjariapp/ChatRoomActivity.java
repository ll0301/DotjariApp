package com.example.wante.dotjariapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wante.dotjariapp.Adapter.ChatAdapter;
import com.example.wante.dotjariapp.Item.ChatItem;
import com.example.wante.dotjariapp.Item.ChatList;
import com.example.wante.dotjariapp.Item.UserProfile;
import com.example.wante.dotjariapp.Notifications.APIService;
import com.example.wante.dotjariapp.Notifications.Client;
import com.example.wante.dotjariapp.Notifications.Data;
import com.example.wante.dotjariapp.Notifications.MyResponse;
import com.example.wante.dotjariapp.Notifications.Sender;
import com.example.wante.dotjariapp.Notifications.Token;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity {

    private static final String TAG = "ChatRoomActivity" ;
    //개인정보
 private CircleImageView profile_imageView;
 private TextView username;
 private ImageView btnChatRoomBack, removeChat, img_message;

    // 채팅도구
   private Button btnSend;
   private EditText etMessage;
  String userid;
    //파이어베이스
  FirebaseUser fuser;
DatabaseReference reference;

 private Intent intent;

  private ValueEventListener seenListener;


    // 채팅 리사이클러뷰
      RecyclerView recyclerView;
    ChatAdapter chatAdapter;
     List<ChatItem> mChatItem;

    private APIService apiService;
    private boolean notify = false;

    private boolean inOut;

    private StorageReference storageReference;
    private  static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    private String mUri ;
    private ProgressBar progressBar;
    private Handler mHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // 유저정보받기
        intent = getIntent();
        userid = intent.getStringExtra("userId");
        // 내가 선택해서 들어간 채팅방 유저의 아이디

//스토리지
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());


        //이미지메세지 보내기 카메라 열기
        img_message = findViewById(R.id.iv_chatroom_imgMessage);
        img_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chatRemoveCamera();

            }
        });


        //chat 리사이클러뷰
        // 메세지를 리사이클러뷰에 업로드? 한다
        recyclerView = findViewById(R.id.rv_chatroom);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //뒤로가기
        btnChatRoomBack = findViewById(R.id.iv_chatroom_back);
        btnChatRoomBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userid = null;
                finish();
              startActivity(new Intent(ChatRoomActivity.this, ChatActivity.class));
            }
        });

        //채팅방나가기버튼
        removeChat = findViewById(R.id.remove_chat);
        removeChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatRemoveDig();
            }
        });

        //fcm api
        apiService = Client.getClient("http://fcm.googleapis.com/").create(APIService.class);


        //메세지입력
        btnSend = findViewById(R.id.btn_chatroom_send);
        etMessage = findViewById(R.id.et_chatroom_message);

        // 친구정보
        profile_imageView = findViewById(R.id.civ_chatRoom_fuser);
        username = findViewById(R.id.tv_chatRoom_fuserName);


        //메세지보내기
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notify = true;
                String msg = etMessage.getText().toString();
                if(!msg.equals("")){
                    sendMessage(fuser.getUid(), userid, msg, "default");
                }else {
                    Toast.makeText(ChatRoomActivity.this, "메세지를 보낼 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                etMessage.setText("");
            }
        });



//--------------------------------------------------------------------------------------------------------------------
        //파이에베이스 데이터에 데이터 담기  (일반유저)

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                username.setText(userProfile.getUserName());
                if(userProfile.getUserImgUri().equals("default")) {
                        // ChatRoomActivity -> getApplicatrionContext로 변경
                } else { Glide.with(getApplicationContext()).load(userProfile.getUserImgUri()).into(profile_imageView);
                    }


                readMessages(fuser.getUid(), userid, userProfile.getUserImgUri(), userProfile.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

            seenMessage(userid);




    }//===============================on create



    // 카메라선택 다이얼로그
    private void chatRemoveCamera() {

        final Dialog dialog = new Dialog(this);  //다이얼로그 객체생성
        dialog.setContentView(R.layout.chat_room_camera);  // 다이얼로그 화면등록
/*        Button chatCamera = (Button) dialog.findViewById(R.id.chat_camera);*/
        Button chatGallery = (Button) dialog.findViewById(R.id.chat_gallery);

        dialog.show();
        dialog.setOwnerActivity(ChatRoomActivity.this);
        dialog.setCanceledOnTouchOutside(true);
/*
        //카메라버튼
        chatCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(ChatRoomActivity.this, "카메라", Toast.LENGTH_SHORT).show();

            }
        });*/

        //갤러리버튼
        chatGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openImage();
                dialog.dismiss();

            }
        });

    }

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

                        notify = true;

                        if(!mUri.equals("")){
                            sendMessage(fuser.getUid(), userid, mUri, mUri);
                        }else {
                            Toast.makeText(ChatRoomActivity.this, "이미지를 보낼 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }


/*

                        //핸들러 종료되고 프로그래스바 숨김
                        mHandler.removeMessages(0);
                        progressBar.setVisibility(View.GONE);
*/



                    }else {
                        Toast.makeText(ChatRoomActivity.this, "업로드실패", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(ChatRoomActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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



    // 채팅방 나가기 다이얼로그
    private void chatRemoveDig() {
        //시간선택다이얼로그
        final Dialog dialog = new Dialog(this);  //다이얼로그 객체생성
        dialog.setContentView(R.layout.chat_room_remove);  // 다이얼로그 화면등록
        Button chatRemoveConfirm = (Button) dialog.findViewById(R.id.chat_remove_confirm);
        Button chatRemoveCancel = (Button) dialog.findViewById(R.id.chat_remove_cancel);

        dialog.show();
        dialog.setOwnerActivity(ChatRoomActivity.this);
        dialog.setCanceledOnTouchOutside(true);

        //확인버튼
        chatRemoveConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fuser = FirebaseAuth.getInstance().getCurrentUser();
                reference = FirebaseDatabase.getInstance().getReference().child("ChatList").child(fuser.getUid()).child(userid);
                reference.removeValue();
                Toast.makeText(ChatRoomActivity.this, "채팅방을 나갑니다.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ChatRoomActivity.this,ChatActivity.class));
                finish();
                dialog.dismiss();
            }
        });
        // 취소버튼
        chatRemoveCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });



    }






    //메세지 봣는지 안봤는지
    private void seenMessage (final String userid) {


        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatItem chatItem = snapshot.getValue(ChatItem.class);
                    // 계속 실행하길래 chatRoom에 현재유저가 채팅방 들어왔을때에만 실행하도록
                    if (inOut == true) {
                        if (chatItem.getReceiver().equals(fuser.getUid()) && chatItem.getSender().equals(userid)) {
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("isseen", true);
                            // 매개변수이름이 지정된 경우 지정된 매개변수값을 자바프로그래밍 언어 ref개체로 검색
                            snapshot.getRef().updateChildren(hashMap);
                        }

                        Log.e(TAG, "receiver" + chatItem.getReceiver() + "sender" + chatItem.getSender());

                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




    //메세지보내기
    private void sendMessage (final String sender, final String receiver, String message, String imgMessage) {

    DatabaseReference  databaseReference = FirebaseDatabase.getInstance().getReference();

        //해쉬맵에 담는다.
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("imgMessage", imgMessage);
        hashMap.put("isseen", false);

        databaseReference.child("Chats").push().setValue(hashMap);


        // 여기는 문제없음
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(sender)
                .child(receiver);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(receiver);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        final DatabaseReference chatRef2 = FirebaseDatabase.getInstance().getReference("ChatList")
                .child(receiver)
                .child(sender);
        chatRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()) {
                    chatRef2.child("id").setValue(sender);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




        //fcm 메세지
        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);

                if(notify){
                    sendNotification(receiver, userProfile.getUserName(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendNotification(String receiver, final String userName, final String msg) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);

                    if(msg.length()>30){
                        Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, userName+":"+ "이미지", "새로운메세지", userid);
                        Sender sender = new Sender(data, token.getToken());

                        apiService.sendNotification(sender)
                                .enqueue(new Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                        if(response.code() == 200) {
                                            if(response.body().success != 1) {
                                                /* Toast.makeText(ChatRoomActivity.this, "Failed", Toast.LENGTH_SHORT).show();*/
                                                Log.e(TAG,"Failed");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) {

                                    }
                                });
                    } else {

                        Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, userName+":"+ msg, "새로운메세지", userid);
                        Sender sender = new Sender(data, token.getToken());

                        apiService.sendNotification(sender)
                                .enqueue(new Callback<MyResponse>() {
                                    @Override
                                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                        if(response.code() == 200) {
                                            if(response.body().success != 1) {
                                                /* Toast.makeText(ChatRoomActivity.this, "Failed", Toast.LENGTH_SHORT).show();*/
                                                Log.e(TAG,"Failed");
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<MyResponse> call, Throwable t) {

                                    }
                                });

                    }




                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    //메세지 읽기
    private void readMessages(final String myid, final String userid, final String imguri, final String username) {

        mChatItem = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mChatItem.clear();
                //snapshot 정보값 얻어오기
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatItem chatItem = snapshot.getValue(ChatItem.class);
                    if(chatItem.getReceiver().equals(myid) && chatItem.getSender().equals(userid) ||
                            chatItem.getReceiver().equals(userid) && chatItem.getSender().equals(myid)){
                        mChatItem.add(chatItem);
                    }

                    chatAdapter = new ChatAdapter(ChatRoomActivity.this, mChatItem,imguri,username,userid);
                    recyclerView.setAdapter(chatAdapter);



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });





    }



    // 사용자 on off 메소드
    private void userOnOff(String onOff) {

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onOff", onOff);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        inOut = true;
        userOnOff("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        inOut = false;
        userOnOff("offline");

    }


}
