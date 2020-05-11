package com.example.wante.dotjariapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wante.dotjariapp.Adapter.FriendsListAdapter;
import com.example.wante.dotjariapp.Adapter.MyFriendsAdapter;
import com.example.wante.dotjariapp.Item.ChatItem;
import com.example.wante.dotjariapp.Item.FriendsList;
import com.example.wante.dotjariapp.Item.UserProfile;
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

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MoreActivity extends AppCompatActivity {

    private static final String TAG = "MoreActivity" ;
    //일반회원정보
    private CircleImageView image_profile;
    private TextView username, userstatus;
    private ImageView userEdit, shopStar;
    private ConstraintLayout myinfo_card;
    private TextView normalUser, shopUser, shopName, shopAddress, shopPhone;
private String name, userId;
   private DatabaseReference reference;
   private FirebaseUser fuser;

   private     Button btnShopRegister, btnShopRemove;


   private   StorageReference storageReference;

   private CardView shopCardView, userCardView;

   private UserProfile uProfile;
    private FirebaseAuth firebaseAuth;


    // 안읽은 메세지 표시
    private TextView chatCount;
    private CircleImageView chatCountCir;

    // 친구요청보낸 목록
    private RecyclerView recyclerView;
    private List<UserProfile> friendsLists;
    private MyFriendsAdapter friendsListAdapter;
    private TextView sendRefuseCount;
    private ConstraintLayout cl_requestList;

    //progressDialog
    private Handler mHandler;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_view);



/*        // 데이터로딩 프로그래스바
        // 친구목록이 로딩될때까지 프로그래스바를 띄운다.
        progressBar = findViewById(R.id.progressBar);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progressBar.setVisibility(View.VISIBLE);
            }
        };
        mHandler.sendEmptyMessageDelayed(0,1000);*/



        // 친구요청 목록
        recyclerView = findViewById(R.id.rv_send_refuse);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(MoreActivity.this));
        friendsLists = new ArrayList<>();

        //내가 등록요청한 친구보기
        readSendRequestFriends();

        sendRefuseCount = findViewById(R.id.sendRefuseCount);
        cl_requestList = findViewById(R.id.cl_refuseFriendsList);
        cl_requestList.setVisibility(View.GONE);

        //로그아웃 버튼
        TextView tvLogOut = (TextView) findViewById(R.id.tv_logout);
        tvLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                // change this code because pp will crash
                startActivity(new Intent(MoreActivity.this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        // 업체등록버튼
        btnShopRegister = findViewById(R.id.btn_shop_register);
            btnShopRegister.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MoreActivity.this, ShopRegisterActivity.class));
                }
            });

        //프로필변경버튼
        userEdit = findViewById(R.id.user_profile_btn);
        userEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MoreActivity.this, UpdateProfileActivity.class));
            }
        });



        //  나의프로필
        image_profile = findViewById(R.id.myfriend_profilePic);
        username =findViewById(R.id.username);
        userstatus  = findViewById(R.id.userstatus);
        normalUser = findViewById(R.id.normalUser);
        myinfo_card = findViewById(R.id.myInfo_card);
        userCardView = findViewById(R.id.user_cardView);
        btnShopRemove = findViewById(R.id.btn_shop_remove);

        //유저프로필사진 스토리지 접근
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                username.setText(userProfile.getUserName());
                userstatus.setText(userProfile.getUserStatus());

                if(userProfile.getUserImgUri().equals("default")) {
                } else { Glide.with(getApplicationContext()).load(userProfile.getUserImgUri()).into(image_profile);
                }

                // 이미지 크게보기 이미지 다운로드
                image_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(MoreActivity.this,DownloadActivity.class);
                        intent.putExtra("userId",userProfile.getUserId());
                        intent.putExtra("userName", userProfile.getUserName());
                        intent.putExtra("userImgUri", userProfile.getUserImgUri());
                        startActivity(intent);
                        finish();

                    }
                });



            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });




        // 메뉴바
        ImageView homeButton = (ImageView) findViewById(R.id.home_button);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreActivity.this, HomeActivity.class);
                MoreActivity.this.startActivity(intent);
            }
        });

        ImageView ticketButton = (ImageView) findViewById(R.id.ticket_button);
        ticketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MoreActivity.this, SearchActivity.class);
                MoreActivity.this.startActivity(intent);
            }
        });
        ImageView chatButton = (ImageView) findViewById(R.id.chat_button);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(MoreActivity.this, ChatActivity.class);
                MoreActivity.this.startActivity(chatIntent);
            }
        });

        //*****************************************메뉴바*********************************************************//

        // 읽지않은메세지숫자 표시하기

        chatCount = findViewById(R.id.chat_count_tv);
        chatCountCir = findViewById(R.id.chat_count_civ);

       reference = FirebaseDatabase.getInstance().getReference("Chats");
       reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatItem chatItem = snapshot.getValue(ChatItem.class);
                    if(chatItem.getReceiver().equals(fuser.getUid())&&!chatItem.isIsseen()) {
                        unread ++;
                    }
                }

                if(unread == 0) {
                    chatCountCir.setVisibility(View.GONE);
                } else {
                    chatCountCir.setVisibility(View.VISIBLE);
                    chatCount.setText(String.valueOf(unread));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    } // ON CREATE ============================================================================================================


    //친구요청한 목록 불러오기
    private void readSendRequestFriends() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        // FriendsList -> 현재유저 -> 현재유저 -> RequestList 있는 데이터에 접근한다.
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(firebaseUser.getUid()).child("MyRequestList");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                friendsLists.clear();
                try {
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        UserProfile friendsList = snapshot.getValue(UserProfile.class);
                        if(!friendsList.getUserId().equals(firebaseUser.getUid())){

                            Log.e(TAG,friendsList.getUserId()+friendsList.getUserName());
                            friendsLists.add(friendsList);
                        }
                        friendsListAdapter = new MyFriendsAdapter(MoreActivity.this, friendsLists);
                        recyclerView.setAdapter(friendsListAdapter);


                        //친구숫자 표기
                        int count = friendsLists.size();
                        if(count>0){
                            cl_requestList.setVisibility(View.VISIBLE);
                            sendRefuseCount.setText(String.valueOf(count));
                        }

                    }

                }catch (Exception e) {

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void removeID() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        Query removeQuery = ref.child("Users").orderByChild("userId").equalTo(user.getUid());

        removeQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot removeSnapshot : dataSnapshot.getChildren() ){
                    removeSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }






}



