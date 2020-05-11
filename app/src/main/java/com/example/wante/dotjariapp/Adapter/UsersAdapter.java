package com.example.wante.dotjariapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wante.dotjariapp.ChatRoomActivity;
import com.example.wante.dotjariapp.Item.ChatItem;
import com.example.wante.dotjariapp.Item.UserProfile;
import com.example.wante.dotjariapp.R;
import com.example.wante.dotjariapp.UserProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {


    public static final int VIEW_TYPE_USER = 0;
    public static final int VIEW_TYPE_SHOP = 1;

    private Context mContext;
    private List<UserProfile> mUsers;

    //상대방 접속여부
    private boolean isChat;

    String theLastMessage;

    FirebaseUser fuser;


    public UsersAdapter(Context mContext, List<UserProfile> mUsers, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;

    }

    @NonNull
    @Override
    public UsersAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {


            View view = LayoutInflater.from(mContext).inflate(R.layout.item_user_single,viewGroup, false);
            return new UsersAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.ViewHolder viewHolder, final int i) {

        final UserProfile user = mUsers.get(i);
        viewHolder.userName.setText(user.getUserName());
        viewHolder.userEmail.setText(user.getUserEmail());
        viewHolder.userStatus.setText(user.getUserStatus());
        viewHolder.userId.setText(user.getUserId());

        // 이미지 설정
        if (user.getUserImgUri().equals("default")) {
            viewHolder.userImg.setImageResource(R.drawable.ic_my_photo_24dp);
        }else {
            Glide.with(mContext).load(user.getUserImgUri()).into(viewHolder.userImg);
        }

// 마지막 메세지
        if(isChat) {
            lastMessage(user.getUserId(), viewHolder.last_msg);
        } else {
            viewHolder.last_msg.setVisibility(View.GONE);
        }

        //읽지않은 메세지
        //메세지 카운트아이콘 숫자가 0이면 사라지고 아니면 나타난다.

            countMessage(user.getUserId(), viewHolder.chatRoomCount, viewHolder.chatRoomCountCir);


        // 접속여부
        if (isChat) {
            //버튼보이기 숨기기
            if(user.getOnOff().equals("online")){
                viewHolder.img_on.setVisibility(View.VISIBLE);
                viewHolder.img_off.setVisibility(View.GONE);
            } else {
                viewHolder.img_on.setVisibility(View.GONE);
                viewHolder.img_off.setVisibility(View.VISIBLE);
            }
        } else {
            viewHolder.img_on.setVisibility(View.GONE);
            viewHolder.img_off.setVisibility(View.GONE);
        }



        // 마이페이지에 회원정보
        viewHolder.userImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserProfileActivity.class);
                intent.putExtra("userId", user.getUserId());
                intent.putExtra("userName", user.getUserName());
                intent.putExtra("userStatus", user.getUserStatus());
                intent.putExtra("userImgUri", user.getUserImgUri());
                intent.putExtra("onOff", user.getOnOff());
                intent.putExtra("search", user.getSearch());
                intent.putExtra("userEmail",user.getUserEmail());
                mContext.startActivity(intent);

            }
        });

        // 채팅방 아이디로 구분하기
        viewHolder.cl_chatroom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ChatRoomActivity.class);
                intent.putExtra("userId", user.getUserId());
                mContext.startActivity(intent);
            }
        });





    }



    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public ConstraintLayout cl_chatroom;
        public TextView userName, userStatus, userEmail, userId;
        public CircleImageView userImg;

        private ImageView img_on;
        private ImageView img_off;
        private TextView last_msg;

        private CircleImageView chatRoomCountCir, chatCountCir;
        private TextView chatRoomCount,chatCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            userImg = itemView.findViewById(R.id.myfriend_profilePic);
            userEmail = itemView.findViewById(R.id.tv_myFriends_email);
            userName = itemView.findViewById(R.id.tv_myFriends_name);
            userStatus = itemView.findViewById(R.id.tv_single_userStatus);
            userId = itemView.findViewById(R.id.tv_single_userId);

            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);

            last_msg = itemView.findViewById(R.id.last_msg);

            cl_chatroom = itemView.findViewById(R.id.cl_singleUser);


            chatRoomCountCir = itemView.findViewById(R.id.chatroom_count_civ);  // 빨간색원
            chatRoomCount = itemView.findViewById(R.id.chatRoom_count);  // 숫자


        }
    }



    // 안읽음 메세지 카운트
    private void countMessage(final String userId, final TextView chatRoomCount, final CircleImageView chatRoomCountCir) {

        fuser = FirebaseAuth.getInstance().getCurrentUser();

       DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        // chats 내부에 있는 데이터를 참조한다.
        // 내부에 있는 데이터가 변화할때 작동한다.
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int unread = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatItem chatItem = snapshot.getValue(ChatItem.class);
                    // 리시버가 현재사용유저이고 메세지를 읽지않으면
                    if(chatItem.getReceiver().equals(fuser.getUid())&& chatItem.getSender().equals(userId) && !chatItem.isIsseen()) {
                        unread ++;
                        // 읽지않음이 플러스된다.
                    }
                }

                if(unread != 0) {
                    chatRoomCount.setText(String.valueOf(unread));
                }
                else{
                    chatRoomCount.setVisibility(View.GONE);
                    chatRoomCountCir.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // 마지막메세지 체크

    private  void lastMessage (final String userId, final TextView last_msg){

        theLastMessage = "default";

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatItem chatItem = snapshot.getValue(ChatItem.class);
                    if(chatItem.getReceiver().equals(firebaseUser.getUid())&& chatItem.getSender().equals(userId) ||
                    chatItem.getReceiver().equals(userId) && chatItem.getSender().equals(firebaseUser.getUid())) {
                        theLastMessage = chatItem.getMessage();
                    }
                }

                switch (theLastMessage) {
                    case "default" :
                        last_msg.setText("");
                        break;

                        default:
                            if(theLastMessage.length()>30){
                                last_msg.setText("이미지");
                            }else {
                                last_msg.setText(theLastMessage);
                            }

                            break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}




