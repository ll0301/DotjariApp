package com.example.wante.dotjariapp;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wante.dotjariapp.Adapter.UsersAdapter;
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

public class PlusFriendsList extends AppCompatActivity {

    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth firebaseAuth;
    private FirebaseStorage firebaseStorage;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;



    private ConstraintLayout clPlusFriendsSearch;
    private ImageView backButton;

    //업체프로필
    private CircleImageView shopProfileImg;
    private TextView shopName, username;


    //progressDialog
    private Handler mHandler;
    private ProgressBar progressBar;


   private RecyclerView recyclerView;
    private UsersAdapter usersAdapter;
    private List<UserProfile> mUserList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plus_friends_list);


        //친구목록
        recyclerView = findViewById(R.id.rv_plusFriends_list);
        recyclerView.setHasFixedSize(true);  // 크기맞추기
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mUserList = new ArrayList<>();
        readUsers();

        // 데이터로딩 프로그래스바
        // 친구목록이 로딩될때까지 프로그래스바를 띄운다.
        progressBar = findViewById(R.id.plusFriends_progress);
 

        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                progressBar.setVisibility(View.VISIBLE);
            }
        }; mHandler.sendEmptyMessageDelayed(0,1000);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

/*
        databaseReference = firebaseDatabase.getReference().child("Users").child(firebaseAuth.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserProfile userProfile = dataSnapshot.getValue(UserProfile.class);
                username.setText(userProfile.getUserName());
                shopName.setText(userProfile.getShopName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
*/



        backButton = findViewById(R.id.plusFriends_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });




    }

//친구목록읽기

    private void readUsers() {

        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("friendsList");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                mUserList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    UserProfile userProfile = snapshot.getValue(UserProfile.class);
                    assert userProfile != null;
                    assert firebaseUser != null;
                        mUserList.add(userProfile);
                }

                usersAdapter = new UsersAdapter(PlusFriendsList.this, mUserList, true);
                recyclerView.setAdapter(usersAdapter);


                //로드완료되면 프로그래스바 종료
                mHandler.removeMessages(0);
                progressBar.setVisibility(View.GONE);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
