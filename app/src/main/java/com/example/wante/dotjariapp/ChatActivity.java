package com.example.wante.dotjariapp;

import android.app.usage.NetworkStats;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wante.dotjariapp.Adapter.UsersAdapter;
import com.example.wante.dotjariapp.Item.ChatItem;
import com.example.wante.dotjariapp.Item.ChatList;
import com.example.wante.dotjariapp.Item.UserProfile;
import com.example.wante.dotjariapp.Notifications.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatActivity extends AppCompatActivity {

    // 채팅방 구현하기

    private RecyclerView recyclerView;

    private UsersAdapter usersAdapter;
    private List<UserProfile> mUsers;

    private DatabaseReference databaseReference;
   private FirebaseUser fuser;


  /*  private List<String> userList;*/

    // 안읽은 메세지 표시
    private TextView chatCount;
    private CircleImageView chatCountCir;

    private List<ChatList> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        // 채팅방 리스트 리사이클러뷰
        recyclerView = findViewById(R.id.rv_chat_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        userList = new ArrayList<>();


        //채팅리스트에 정보담기
        databaseReference =FirebaseDatabase.getInstance().getReference("ChatList").child(fuser.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatList chatList = snapshot.getValue(ChatList.class);
                    userList.add(chatList);
                }
                readChatList();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
/*
        // 데이터베이스 접근
        databaseReference= FirebaseDatabase.getInstance().getReference("Chats");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatItem chatItem= snapshot.getValue(ChatItem.class);

                    if(chatItem.getSender().equals(fuser.getUid())){
                        userList.add(chatItem.getReceiver());
                    }
                    if(chatItem.getReceiver().equals((fuser.getUid()))){
                        userList.add(chatItem.getSender());
                    }
                }

                readChats();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/


        ImageView homeButton = (ImageView) findViewById(R.id.home_button);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, HomeActivity.class);
                ChatActivity.this.startActivity(intent);
            }
        });

        ImageView searchButton = (ImageView) findViewById(R.id.ticket_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, SearchActivity.class);
                ChatActivity.this.startActivity(intent);
            }
        });
        ImageView moreButton = (ImageView) findViewById(R.id.more_button);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(ChatActivity.this, MoreActivity.class);
                ChatActivity.this.startActivity(chatIntent);
            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        // 읽지않은메세지숫자 표시하기

        chatCount = findViewById(R.id.chat_count_tv);
        chatCountCir = findViewById(R.id.chat_count_civ);
        //빨간색 원안에 숫자를 카운트하여 읽지않은 메세지를 표시해준다.

        databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        // chats 내부에 있는 데이터를 참조한다.
        // 내부에 있는 데이터가 변화할때 작동한다.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatItem chatItem = snapshot.getValue(ChatItem.class);

                    // 리시버가 현재사용유저이고 메세지를 읽지않으면
                    if(chatItem.getReceiver().equals(fuser.getUid())&&!chatItem.isIsseen()) {
                        unread ++;
                        // 읽지않음이 플러스된다.
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



    }

    private void updateToken(String token) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }

    private void readChatList() {
        mUsers = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    for (ChatList chatList : userList) {
                        if (userProfile.getUserId().equals(chatList.getId())){
                            mUsers.add(userProfile);
                        }
                    }
                }
                usersAdapter = new UsersAdapter(ChatActivity.this, mUsers,true);
                recyclerView.setAdapter(usersAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

/*
    //채팅방 읽어오기
    private void readChats() {

        mUsers = new ArrayList<>();

        //users에서 정보를 참조한다.
        databaseReference= FirebaseDatabase.getInstance().getReference("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);

                    // display 1 user from chats
                    // String id : userList 에서  ConcurrentModificationExcpetion 발생
                    // try catch로 예외처리    아직 불안한부분이 있음
                    try {
                        for(String id : userList) {
                            if(userProfile.getUserId().equals(id)){
                                if(mUsers.size() !=0){
                                    for (UserProfile userProfile1 : mUsers){
                                        if(!userProfile.getUserId().equals(userProfile1.getUserId())){
                                            mUsers.add(userProfile);
                                        }
                                    }
                                } else  {
                                    mUsers.add(userProfile);
                                }
                            }
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                usersAdapter = new UsersAdapter(ChatActivity.this, mUsers, true);
                recyclerView.setAdapter(usersAdapter);

            }






            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
*/



}
