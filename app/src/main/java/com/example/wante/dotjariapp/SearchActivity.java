package com.example.wante.dotjariapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wante.dotjariapp.Adapter.MyFriendsAdapter;
import com.example.wante.dotjariapp.Adapter.UsersAdapter;
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

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchActivity extends AppCompatActivity {



    private FirebaseUser firebaseUser;
    private EditText searchFriends;


    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    //친구목록 리사이클러뷰
    private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private MyFriendsAdapter myFriendsAdapter;
    private List<UserProfile> mUserList;
    private List<FriendsList> friendsLists;


    // 안읽은 메세지 표시
    private TextView chatCount;
    private CircleImageView chatCountCir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        recyclerView = findViewById(R.id.rv_search_friends_list);
        recyclerView.setHasFixedSize(true);  // 크기맞추기
        recyclerView.setLayoutManager(new LinearLayoutManager(SearchActivity.this));
        friendsLists = new ArrayList<>();
        mUserList = new ArrayList<>();
        //업체목록

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();



        //친구검색
        searchFriends = findViewById(R.id.et_friends_search);

        //자판감추기
        searchFriends.setInputType(0);
        searchFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchFriends.setInputType(1);
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.showSoftInput(searchFriends,InputMethodManager.SHOW_IMPLICIT);
            }
        });

        //에디트 입력변화 이벤트
        searchFriends.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString().toLowerCase());  // 문자열을 소문자로 반환한다.

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });





        // 메뉴바
        ImageView homeButton = (ImageView) findViewById(R.id.home_button);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, HomeActivity.class);
                SearchActivity.this.startActivity(intent);
            }
        });

        ImageView moreButton = (ImageView) findViewById(R.id.more_button);
        moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SearchActivity.this, MoreActivity.class);
                SearchActivity.this.startActivity(intent);
            }
        });
        ImageView chatButton = (ImageView) findViewById(R.id.chat_button);
        chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chatIntent = new Intent(SearchActivity.this, ChatActivity.class);
                SearchActivity.this.startActivity(chatIntent);
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


    }

    //친구검색

    private void searchUsers(String s) {

        final FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

        //query 누군가에게 질문하여 정보를 요청하는것
        // 데이터베이스에 정보를요청한다면 쿼리한다고 말할수 있다.
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("search")
                .startAt(s)
                .endAt(s + "\uf88f");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    mUserList= new ArrayList<>();
                   mUserList.clear();
                if(!searchFriends.getText().toString().equals("")) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                      UserProfile userProfile = snapshot.getValue(UserProfile.class);
                        assert userProfile != null;
                        assert fuser != null;
                        if (!userProfile.getUserId().equals(fuser.getUid())) {
                            mUserList.add(userProfile);



                        }
                    }
                }
      /*          usersAdapter = new UsersAdapter(SearchActivity.this, mUserList, false);*/
                myFriendsAdapter = new MyFriendsAdapter(SearchActivity.this, mUserList);
                recyclerView.setAdapter(myFriendsAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
