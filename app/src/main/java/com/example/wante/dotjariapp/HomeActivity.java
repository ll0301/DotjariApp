package com.example.wante.dotjariapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeActivity extends AppCompatActivity {
    private static final String TAG ="HomeActivity" ;

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;
    //친구목록 리사이클러뷰
    private RecyclerView recyclerView; // 친구목록
    private RecyclerView recyclerView2; // 나를등록한 친구목록
    /*private UsersAdapter usersAdapter; //  채팅창 아이템에 사용됨 */
private FriendsListAdapter friendsListAdapter; //  나를등록하거나 내가 등록한 친구목록에 활용된다
    private MyFriendsAdapter myFriendsAdapter; //  서로 친구가 확정된 친구목록에 활용된다.

    //유저 프로필리스트
    //유저의 정보가 Users 에 저장이되고 그것을 재활용하기위함
    private List<UserProfile> mUserList;

    // 유저프로필리스트와 동일한기능
    // 나를 등록한 친구의정보 즉 , 내가 누군가를 친구로 추가했을때 내 정보가 친구에게 전달된다.
    // 그때 사용되는 부분 , 아이템뷰 모양이 다르다
    private List<FriendsList> friendsLists;

    private ImageView searchBtn; // 검색버튼  검색으로 이동한다.
    private TextView frinedsCount; // 친구목록 친구숫자

    // 안읽은 메세지 표시
    private TextView chatCount;
   private CircleImageView chatCountCir;

    //유저프로필
    private CircleImageView image_profile;
    private TextView username, userstatus, friendsListMsg;
    private ConstraintLayout clPlusFriends;
    private TextView requestFriendsCount;


    //progressDialog
    private Handler mHandler;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //내친구목록
        recyclerView = findViewById(R.id.rv_home_userList);

        //친구요청 목록
        recyclerView2 = findViewById(R.id.rv_request_friends);
        friendsListMsg = findViewById(R.id.regFriendsMessage);
        friendsListMsg.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);  // 크기맞추기
        recyclerView.setLayoutManager(new LinearLayoutManager(HomeActivity.this));

        recyclerView2.setHasFixedSize(true);  // 크기맞추기
        recyclerView2.setLayoutManager(new LinearLayoutManager(HomeActivity.this));
        friendsLists = new ArrayList<>();
        mUserList = new ArrayList<>();
        // 나에게 친구신청한 목록 불러오기
        readRequestUser();

        //내친구불러오기
        readUsers();

        frinedsCount = findViewById(R.id.tv_friends_count);

        // 데이터로딩 프로그래스바
        // 친구목록이 로딩될때까지 프로그래스바를 띄운다.
       progressBar = findViewById(R.id.progressBar);

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progressBar.setVisibility(View.VISIBLE);
            }
        };
        mHandler.sendEmptyMessageDelayed(0,1000);


        // 검색버튼
        searchBtn = findViewById(R.id.iv_friends_search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(HomeActivity.this, SearchActivity.class));
            }
        });


// 파이어베이스에서 유저네임 이미지 상태메세지 받아와서 셋팅
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        username = findViewById(R.id.tv_userName);
        userstatus = findViewById(R.id.tv_userStatus);
        image_profile = findViewById(R.id.user_profile_image);
        clPlusFriends = findViewById(R.id.cl_plusFriends);
        requestFriendsCount = findViewById(R.id.getRefuseCount);
        clPlusFriends.setVisibility(View.GONE);


        // 현재유저의 프로필정보를 담는다.
        //실시간 데이터베이스에 user라는 폴더를 만들고 uid로 구분하여 저장한다.
        databaseReference = firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid());
        // 실시간 데이터베이스에 변화가있는지 감지하는 리스너
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                final UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                username.setText(userProfile.getUserName());
                userstatus.setText(userProfile.getUserStatus());
                if (userProfile.getUserImgUri().equals("default")) {
                } else {
                    Glide.with(getApplicationContext()).load(userProfile.getUserImgUri()).into(image_profile);
                }
                // 이미지 크게보기
                image_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(HomeActivity.this, DownloadActivity.class);
                        intent.putExtra("userId", userProfile.getUserId());
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
        ImageView chatButton = (ImageView) findViewById(R.id.chat_button);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });

        ImageView searchButton = (ImageView) findViewById(R.id.ticket_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                HomeActivity.this.startActivity(intent);
            }
        });
        ImageView moreButton = (ImageView) findViewById(R.id.more_button);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(HomeActivity.this, MoreActivity.class);
                HomeActivity.this.startActivity(chatIntent);
            }
        });



        // 읽지않은메세지숫자 표시하기


        chatCount = findViewById(R.id.chat_count_tv);
        chatCountCir = findViewById(R.id.chat_count_civ);


        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatItem chatItem = snapshot.getValue(ChatItem.class);
                    if(chatItem.getReceiver().equals(firebaseUser.getUid())&&!chatItem.isIsseen()) {
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

    }  //****************************************ON create*********************************************************//

    //나에게 친구요청한 유저목록
    private void readRequestUser() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // FriendsList -> 현재유저 -> 현재유저 -> RequestList 있는 데이터에 접근한다.
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(firebaseUser.getUid()).child("RequestList");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    friendsLists.clear();
                try {

                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        FriendsList friendsList = snapshot.getValue(FriendsList.class);
                        if(!friendsList.getUserId().equals(firebaseUser.getUid())){
                            friendsLists.add(friendsList);

                        }

                        friendsListAdapter = new FriendsListAdapter(HomeActivity.this, friendsLists);
                        recyclerView2.setAdapter(friendsListAdapter);

                 /*       Log.e(TAG,friendsList.getId()+friendsList.getName());*/

                        //친구숫자 표기
                        int count = friendsLists.size();
                        if(count>0){
                            clPlusFriends.setVisibility(View.VISIBLE);
                            requestFriendsCount.setText(String.valueOf(count));
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


    //친구목록읽기

    private void readUsers() {

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

        // 실시간데이터베이스 안에 현재유저안에 친구리스트를 만들고 그안에 저장되어있는 값만 리사으클러뷰로 뿌려준다.
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fuser.getUid()).child("friendsList");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUserList.clear();
                // 아무도 없는경우 오류가뜨기때문에 예외처리
                try{
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserProfile friendsList = snapshot.getValue(UserProfile.class);
                        assert friendsList != null;
                        assert firebaseUser != null;

                        if(!friendsList.getUserId().equals(fuser.getUid())){

                            mUserList.add(friendsList);
                        }

                        myFriendsAdapter = new MyFriendsAdapter(HomeActivity.this, mUserList);
                        recyclerView.setAdapter(myFriendsAdapter);
                    }


                    //친구숫자 표기
                    int count = mUserList.size();
                    if(count>0){
                        frinedsCount.setText(String.valueOf(count));
                    } else {
                        friendsListMsg.setVisibility(View.VISIBLE);
                        frinedsCount.setVisibility(View.GONE);
                    }
//로드완료되면 프로그래스바 종료
                    mHandler.removeMessages(0);
                    progressBar.setVisibility(View.GONE);

                }catch (Exception e) {

                }


            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
