package com.example.wante.dotjariapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wante.dotjariapp.Item.FriendsList;
import com.example.wante.dotjariapp.Item.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

        private static final String TAG ="UserProfileActivity" ;

        //현재유저의 프로필
        private CircleImageView userProfilePic;
        private TextView username, useremail, userstatus,useronoff, friendsId;

        //친구목록 불러오기
        private List<FriendsList> friendsLists;

    private boolean inOut;
        //현재 프로필창의 유저가 친구일때 , 내가 친구요청을했을때, 내가 친구요청을 받았을때
    // 버튼구성이 달라진다.
        private LinearLayout linear_sender, linear_receiver, linear_friend;

        private Button friendsReg, friendsRemove, friendsChat, friendsRegCancel, friendsConfirm, friendsrefuse
                ,friendsblock;

        private ImageView back, starMyFriends;

        //파이어베이스
        private FirebaseUser fuser;
        private DatabaseReference reference;

        private SharedPreferences sharedPreferences;
        private SharedPreferences.Editor editor;
        private List<UserProfile> mUserList;
        private String userid, userName, userStatus, userImgUri,userEmail, userOnOff, userSearch;
        private Intent intent;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_user_profile);

            // 현재 프로필창의 유저정보
            userProfilePic = findViewById(R.id.up_userProfilePic);
            username = findViewById(R.id.up_friendsName);
            useremail = findViewById(R.id.up_friendsEmail);
            userstatus = findViewById(R.id.up_friends_status);
            useronoff = findViewById(R.id.up_friends_onoff);
            friendsId = findViewById(R.id.up_friends_userid);
            friendsLists = new ArrayList<>();
            mUserList = new ArrayList<>();
            //.

            // 유저정보받기
            intent = getIntent();
            userid = intent.getStringExtra("userId");
/*            userName = intent.getStringExtra("userName");
            userStatus = intent.getStringExtra("userStatus");
            userImgUri = intent.getStringExtra("userImgUri");
            userEmail = intent.getStringExtra("userEmail");
            userOnOff = intent.getStringExtra("onOff");
            userSearch = intent.getStringExtra("search");*/
            fuser = FirebaseAuth.getInstance().getCurrentUser();
            //.




            back = findViewById(R.id.up_back_btn); // 뒤로가기버튼
            starMyFriends = findViewById(R.id.star_myFriends); // 친구일때 프로필이미지 위에 별표시가 등장
            //뒤로가기버튼
            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    startActivity(new Intent(UserProfileActivity.this,HomeActivity.class));
                }
            });






            // 아무도 친구신청을 하지 않았을때 ________________________________________________________________________________
            // 내가 친구를 추가하면  친구요청취소를 띄운다.
            linear_sender = findViewById(R.id.linear_sender); // 버튼구성
            friendsReg = findViewById(R.id.up_FriendsReg); // 친구신청버튼
            friendsRegCancel = findViewById(R.id.up_friends_cancel); // 친구신청취소버튼
            friendsReg.setVisibility(View.VISIBLE);
            friendsRegCancel.setVisibility(View.GONE);
            linear_sender.setVisibility(View.VISIBLE);
            friendsRegSend();
            //친구요청버튼
            friendsReg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                //친구요청을한다.
                    if(inOut==true){
                        friendsRegRequest();
                    }

                    startActivity(new Intent(UserProfileActivity.this, MoreActivity.class));
                    Toast.makeText(UserProfileActivity.this, "친구요청을 합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });


            //친구신청 취소버튼
            friendsRegCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    DatabaseReference reference2 =  FirebaseDatabase.getInstance().getReference()
                            .child("FriendsList")
                            .child(fuser.getUid())
                            .child("MyRequestList")
                            .child(userid);
                    reference2.removeValue();
                    //파이어베이스 데이터베이스 삭제

                    DatabaseReference reference3 =  FirebaseDatabase.getInstance().getReference()
                            .child("FriendsList")
                            .child(userid)
                            .child("RequestList")
                            .child(fuser.getUid());
                    reference3.removeValue();
                    //파이어베이스 데이터베이스 삭제

                    startActivity(new Intent(UserProfileActivity.this, HomeActivity.class));
                    Toast.makeText(UserProfileActivity.this, "친구요청을 취소합니다.", Toast.LENGTH_SHORT).show();
                    finish();

                }
            });

            //  ________________________________________________________________________________  아무도 친구신청을 하지 않았을때
            //.





            //프로필창의 유저가 나에게 친구요청을 했을때 _________________________________________________________________________________
                // *** 친구신청을 받은화면
            //만약 현재 프로필창의 유저에게 친구요청을 받았다면
            //즉, 내 RequestList 현재창 유저의 정보가 담겨있고
            // 현재창의 유저의 friendsList 에 내 정보가 담겨있다.
            linear_receiver = findViewById(R.id.linear_receiver); //현재 프로필창의 유저가 친구요청을 한경우 버튼구성
            friendsConfirm = findViewById(R.id.up_friends_confirm); // 친구수락버튼
            friendsrefuse = findViewById(R.id.up_friends_refuse); //거절버튼
            friendsRegGet();
            //.

            //친구수락버튼
            friendsConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // 친구수락버튼 눌렀을때 메소드
                    if(inOut==true){
                        friendsRegConfirm();
                    }
                    startActivity(new Intent(UserProfileActivity.this, HomeActivity.class));
                    finish();
                }
            });

            // 친구거절버튼
            friendsrefuse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    DatabaseReference reference2 =  FirebaseDatabase.getInstance().getReference()
                            .child("FriendsList")
                            .child(userid)
                            .child("MyRequestList")
                            .child(fuser.getUid());
                    reference2.removeValue();
                    //파이어베이스 데이터베이스 삭제

                    DatabaseReference reference3 =  FirebaseDatabase.getInstance().getReference()
                            .child("FriendsList")
                            .child(fuser.getUid())
                            .child("RequestList")
                            .child(userid);
                    reference3.removeValue();
                    //파이어베이스 데이터베이스 삭제


                    startActivity(new Intent(UserProfileActivity.this, HomeActivity.class));
                    Toast.makeText(UserProfileActivity.this, "친구요청을 거절합니다.", Toast.LENGTH_SHORT).show();
                    finish();

                }
            });
            //_________________________________________________________________________________프로필창의 유저가 나에게 친구요청을 했을때
            //.


            //서로친구일때 -------------------------------------------------------------------------
            linear_friend = findViewById(R.id.linear_friend);  // 서로친구일때 버튼구성
            friendsRemove = findViewById(R.id.up_FriendsRemove); //친구삭제버튼
            friendsChat = findViewById(R.id.up_friendsChat); // 메세지보내기버튼
            friendsblock = findViewById(R.id.up_friends_block);  //친구차단버튼

            friendsRelation();  //친구사이일때 구분하는 메소드

            //친구삭제버튼
            friendsRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    friendsRemoveDig();
                }
            });

            //메세지보내기
            friendsChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(UserProfileActivity.this, ChatRoomActivity.class);
                    intent.putExtra("userId", userid);
                    intent.putExtra("onOff",userOnOff);
                    finish();
                    startActivity(intent);
                }
            });

            //---------------------------------------------------------------------------------------서로친구일때
            //.


            //현재 프로필창의 유저정보 세팅하기
            reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {
                  final UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);

                  username.setText(userProfile.getUserName());
                    useremail.setText(userProfile.getUserEmail());
                    userstatus.setText(userProfile.getUserStatus());
                    friendsId.setText(userProfile.getUserId());
                    useronoff.setText(userProfile.getOnOff());
                    if(userProfile.getUserImgUri().equals("default")) {
                        // ChatRoomActivity -> getApplicatrionContext로 변경
                    } else { Glide.with(getApplicationContext()).load(userProfile.getUserImgUri()).into(userProfilePic);
                    }

                    // 이미지 크게보기 이미지 다운로드
                    userProfilePic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(UserProfileActivity.this,DownloadActivity.class);
                            intent.putExtra("userId",userProfile.getUserId());
                            intent.putExtra("userName", userProfile.getUserName());
                            intent.putExtra("userImgUri", userProfile.getUserImgUri());
                            startActivity(intent);
                            finish();

                        }
                    });



                            }



                @Override
                public void onCancelled(@Nonnull DatabaseError databaseError) {

                }
            });


        }// ===============================================onCreate
//친구신청을 한다
    private void friendsRegRequest() {

            //(첫번째) Users 에 내아이디에 접근한다.
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fuser.getUid());
            myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {
                    FriendsList userProfile = dataSnapshot.getValue(FriendsList.class);

                    // FriendsList -> 프로필유저 -> 프로필유저 -> RequestList -> 내아이디
                    //(두번째) 프로필상의 유저 RequestList에 내정보를 추가한다.
                    DatabaseReference friednsListRef2 =  FirebaseDatabase.getInstance().getReference()
                            .child("FriendsList")
                            .child(userid)
                            .child("RequestList")
                            .child(userProfile.getUserId());

                    // 내가친구추가하고자하는 프로필유저에게 내정보를 보내서
                    // 홈화면에 내가 뜨도록하기위함
                    HashMap<String, Object> hash2 = new HashMap<>();
                    hash2.put("userId",userProfile.getUserId());
                    hash2.put("userImgUri", userProfile.getUserImgUri());
                    hash2.put("userEmail", userProfile.getUserEmail());
                    hash2.put("userName",userProfile.getUserName());
                    hash2.put("userStatus", userProfile.getUserStatus());
                    hash2.put("onOff", userProfile.getOnOff());
                    hash2.put("search", userProfile.getUserName().toLowerCase());
                    friednsListRef2.updateChildren(hash2);

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                            .child("FriendsList")
                            .child(userid)
                            .child("RequestList");
                    reference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {
                            friendsLists = new ArrayList<>();
                            friendsLists.clear();
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                final FriendsList userProfile = snapshot.getValue(FriendsList.class);

                                //상대유저의 requestList에 내정보가 있어야만 해당 코드를 실행하도록
                                if(userProfile.getUserId().equals(fuser.getUid())) {


                                    //유저정보 깔아주기
                                    DatabaseReference  reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
                                    reference.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {
                                            final UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);

                                            // (네번째)FriendsList -> 나 -> 상대유저아이디정보를 저장한다.
                                            DatabaseReference myRef2 = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(fuser.getUid()).child("MyRequestList").child(userid);
                                            HashMap<String, Object> hash= new HashMap<>();
                                            hash.put("userId",userid);
                                            hash.put("userImgUri", userProfile.getUserImgUri());
                                            hash.put("userEmail", userProfile.getUserEmail());
                                            hash.put("userName",userProfile.getUserName());
                                            hash.put("userStatus", userProfile.getUserStatus());
                                            hash.put("onOff", userProfile.getOnOff());
                                            hash.put("search", userProfile.getUserName().toLowerCase());
                                            myRef2.updateChildren(hash);


                                        }
                                        @Override
                                        public void onCancelled(@Nonnull DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }


                        }

                        @Override
                        public void onCancelled(@Nonnull DatabaseError databaseError) {

                        }
                    });

                }
                @Override
                public void onCancelled(@Nonnull DatabaseError databaseError) {

                }
            });

        }



    // 상대의 친구신청을 수락했을때
    private void friendsRegConfirm() {
    //나의 Requestlist에는 상대방이 있고 ( 왜냐하면 나를 친구등록했으니까)
    // 상대방의 RequestList 에는 내가 없음  ( 아직 친구가아닌 상황 )

            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("FriendsList").child(fuser.getUid()).child("RequestList");

            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {
                    friendsLists = new ArrayList<>();
                    friendsLists.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final FriendsList userProfile = snapshot.getValue(FriendsList.class);

                        if(userProfile.getUserId().equals(userid)){

                            DatabaseReference reference5 = FirebaseDatabase.getInstance().getReference("Users");
                            reference5.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {

                                    friendsLists = new ArrayList<>();
                                    friendsLists.clear();
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        final FriendsList userProfile1 = snapshot.getValue(FriendsList.class);


                                        if (userProfile1.getUserId().equals(userid)) {

                                            //  내 친구목록 리스트에 친구신청한 유저의 정보 저장하기 ;
                                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("friendsList").child(userid);
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("onOff", userProfile1.getOnOff());
                                            hashMap.put("search", userProfile1.getUserName().toLowerCase());
                                            hashMap.put("userId", userid);
                                            hashMap.put("userEmail", userProfile1.getUserEmail());
                                            hashMap.put("userName", userProfile1.getUserName());
                                            hashMap.put("userImgUri", userProfile1.getUserImgUri());
                                            hashMap.put("userStatus", userProfile1.getUserStatus());
                                            reference1.updateChildren(hashMap);



                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@Nonnull DatabaseError databaseError) {

                                }
                            });


                            // 위에서 저장한것
                            // Users -> 내아이디 -> friendsList 안에
                            // (두번째)현재 프로필의 유저의 정보가 담겨져 있으면 실행하는 코드를 작성한다.
                            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("friendsList");
                            reference2.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {
                                    friendsLists = new ArrayList<>();
                                    friendsLists.clear();
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        final FriendsList userProfile = snapshot.getValue(FriendsList.class);

                                        if(userProfile.getUserId().equals(userid)) {

                                            //(세번째) Users에 있는 나의 정보에 접근하여
                                            DatabaseReference reference3 = FirebaseDatabase.getInstance().getReference("Users");
                                            reference3.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {

                                                    friendsLists = new ArrayList<>();
                                                    friendsLists.clear();
                                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                                        final FriendsList userProfile1 = snapshot.getValue(FriendsList.class);

                                                        if(userProfile1.getUserId().equals(fuser.getUid())) {

                                                            //(네번째)  상대의 친구목록 리스트에 나의 정보 저장하기 ;
                                                            DatabaseReference reference4 = FirebaseDatabase.getInstance().getReference("Users").child(userid).child("friendsList").child(fuser.getUid());
                                                            HashMap<String, Object> hashMap = new HashMap<>();
                                                            hashMap.put("onOff", userProfile1.getOnOff());
                                                            hashMap.put("search", userProfile1.getUserName().toLowerCase());
                                                            hashMap.put("userId", userProfile1.getUserId());
                                                            hashMap.put("userEmail", userProfile1.getUserEmail());
                                                            hashMap.put("userName", userProfile1.getUserName());
                                                            hashMap.put("userImgUri", userProfile1.getUserImgUri());
                                                            hashMap.put("userStatus", userProfile1.getUserStatus());
                                                            reference4.updateChildren(hashMap);


                                                            //(다섯번째) 내 친구목록 안에있는 친구신청한 유저의 정보 지우기
                                                            DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(fuser.getUid()).child("RequestList").child(userid);
                                                            reference1.removeValue();

                                                            //(마지막) 내가 요청한 목록도 지워주어야함
                                                            DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference()
                                                                    .child("FriendsList")
                                                                    .child(userid)
                                                                    .child("MyRequestList")
                                                                    .child(fuser.getUid());
                                                            reference2.removeValue();
                                                            //파이어베이스 데이터베이스 삭제


                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@Nonnull DatabaseError databaseError) {

                                                }
                                            });



                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@Nonnull DatabaseError databaseError) {

                                }
                            });


                        }
                    }


                }

                @Override
                public void onCancelled(@Nonnull DatabaseError databaseError) {

                }
            });
        }





    // 내가 프로필상의 유저에게 친구등록을 신청했을때
    private void friendsRegSend() {

        DatabaseReference friendsSendsRef2 = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(userid).child("RequestList");
        friendsSendsRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {
                friendsLists.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final FriendsList friendsList = snapshot.getValue(FriendsList.class);

                    if(friendsList.getUserId().equals(fuser.getUid())){
                        //상대 request LIST 에는 내정보가 있고  내 REQUEST에는 상대가 없을때
                        DatabaseReference friendsSendsRef = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(fuser.getUid()).child("RequestList");
                        friendsSendsRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {
                                friendsLists = new ArrayList<>();
                                friendsLists.clear();
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                   FriendsList friendsList = snapshot.getValue(FriendsList.class);

                                    //
                                    if(!friendsList.getUserId().equals(userid)){
                                        friendsLists.add(friendsList);
                                        linear_sender.setVisibility(View.VISIBLE);
                                        friendsReg.setVisibility(View.GONE);
                                        friendsRegCancel.setVisibility(View.VISIBLE);
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@Nonnull DatabaseError databaseError) {

                            }
                        });


                        //친구요청버튼을 숨기고 요청취소버튼을 구성한다.
                    }
                }

            }

            @Override
            public void onCancelled(@Nonnull DatabaseError databaseError) {

            }
        });

    }


    //내가 프로필상의 유저에게 친구신청을 받았을때
    private void friendsRegGet() {
        DatabaseReference friendsGetRef2 = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(fuser.getUid()).child("RequestList");
        friendsGetRef2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {
                friendsLists.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final FriendsList friendsList = snapshot.getValue(FriendsList.class);

                    if(friendsList.getUserId().equals(userid)){
                        //상대 Request에는 내정보가 없고 내 request 에는 상대정보가 있을때
                        DatabaseReference friendsSendsRef = FirebaseDatabase.getInstance().getReference().child("FriendsList").child(userid).child("RequestList");
                        friendsSendsRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {
                                friendsLists = new ArrayList<>();
                                friendsLists.clear();
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    FriendsList friendsList = snapshot.getValue(FriendsList.class);

                                    if(!friendsList.getUserId().equals(fuser.getUid())){
                                        friendsLists.add(friendsList);
                                        linear_sender.setVisibility(View.GONE);
                                        linear_receiver.setVisibility(View.VISIBLE);
                                    }

                                }

                            }

                            @Override
                            public void onCancelled(@Nonnull DatabaseError databaseError) {

                            }
                        });


                        //친구요청버튼을 숨기고 요청취소버튼을 구성한다.
                    }
                }

            }

            @Override
            public void onCancelled(@Nonnull DatabaseError databaseError) {

            }
        });
    }



    //친구관계일때
    private void friendsRelation() {
        //각자의 friendsList에 상대방의 정보가 있어야한다.
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("friendsList");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {

                friendsLists = new ArrayList<>();
                friendsLists.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final FriendsList friendsList = snapshot.getValue(FriendsList.class);

                    //(첫번째) 내 friendsList 안에 현재 프로필상의 유저의 정보가 들어있어야한다.
                    if (friendsList.getUserId().equals(userid)) {

                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Users").child(userid).child("friendsList");
                        reference1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@Nonnull DataSnapshot dataSnapshot) {

                                friendsLists = new ArrayList<>();
                                friendsLists.clear();
                                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                    final FriendsList friendsList = snapshot.getValue(FriendsList.class);

                                    //(두번째) 상대방의 friendsList 안에 내정보가 들어있어야한다.
                                    if (friendsList.getUserId().equals(fuser.getUid())) {
                                        linear_receiver.setVisibility(View.GONE);
                                        linear_sender.setVisibility(View.GONE);
                                        linear_friend.setVisibility(View.VISIBLE);

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@Nonnull DatabaseError databaseError) {

                            }
                        });

                    }
                }
            }

            @Override
            public void onCancelled(@Nonnull DatabaseError databaseError) {

            }
        });
        }



    // 채팅방 나가기 다이얼로그
    private void friendsRemoveDig() {
        //시간선택다이얼로그
        final Dialog dialog = new Dialog(this);  //다이얼로그 객체생성
        dialog.setContentView(R.layout.myfriends_remove);  // 다이얼로그 화면등록
        Button friendsRemove = (Button) dialog.findViewById(R.id.friends_remove_coinfirm);
        Button friendsCancel = (Button) dialog.findViewById(R.id.friends_remove_cancel);

        dialog.show();
        dialog.setOwnerActivity(UserProfileActivity.this);
        dialog.setCanceledOnTouchOutside(true);

        //확인버튼
       friendsRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                    DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference("ChatList")
                            .child(fuser.getUid())
                            .child(userid);
                    reference2.removeValue();

                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("FriendsList")
                            .child(fuser.getUid())
                            .child("MyRequestList")
                            .child(userid);
                    reference1.removeValue();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("FriendsList")
                            .child(userid)
                            .child("MyRequestList")
                            .child(fuser.getUid());
                    reference.removeValue();


                    DatabaseReference reference4 = FirebaseDatabase.getInstance().getReference("Users")
                            .child(userid)
                            .child("friendsList")
                            .child(fuser.getUid());
                    reference4.removeValue();

                    DatabaseReference reference3 = FirebaseDatabase.getInstance().getReference("Users")
                            .child(fuser.getUid())
                            .child("friendsList")
                            .child(userid);

                    reference3.removeValue();








                onPause();
                finish();
                dialog.dismiss();
                startActivity(new Intent(UserProfileActivity.this,MoreActivity.class));
                System.exit(0);


            }




        });
        // 취소버튼
        friendsCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        inOut = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        inOut = false;
    }


}
