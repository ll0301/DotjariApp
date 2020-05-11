package com.example.wante.dotjariapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wante.dotjariapp.DownloadActivity;
import com.example.wante.dotjariapp.Item.ChatItem;
import com.example.wante.dotjariapp.R;
import com.example.wante.dotjariapp.UserProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


// users adapter를 그대로 가져와서 사용

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private Context mContext;
    private List<ChatItem> mChatItem;
    private String imgUri, userName, userId;

    FirebaseUser fuser;


    public ChatAdapter(Context mContext, List<ChatItem> mChatItem, String imgUri, String userName, String userId ) {
        this.mContext = mContext;
        this.mChatItem = mChatItem;
        this.imgUri = imgUri;
        this.userName = userName;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {

        // if 문과 MSG TYPE RIGHT로 메세지 구분
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chatroom_user_right, viewGroup, false);
            return new ChatAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_chatroom_user_left, viewGroup, false);
            return new ChatAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder viewHolder, int position) {

       final ChatItem chat = mChatItem.get(position);
        viewHolder.show_username.setText(userName);

        if(imgUri.equals("default")){
            viewHolder.profile_img.setImageResource(R.drawable.ic_my_photo_24dp);
        }else {
            Glide.with(mContext).load(imgUri).into(viewHolder.profile_img);
        }

        viewHolder.profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserProfileActivity.class);
               intent.putExtra("userId", userId);
                mContext.startActivity(intent);
            }
        });


        // 문자에 위의 내용이 포함되어있으면
        if(chat.getMessage().length()>30){
    Glide.with(mContext).load(chat.getMessage()).into(viewHolder.imgUri_message);
    viewHolder.imgUri_message.setVisibility(View.VISIBLE);
    viewHolder.show_message.setVisibility(View.GONE);

    viewHolder.imgUri_message.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, DownloadActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("userName", userName);
            intent.putExtra("userImgUri",chat.getMessage());
            mContext.startActivity(intent);
        }
    });

        } else {
            viewHolder.show_message.setText(chat.getMessage());
        }





        //메세지확인
        if(position == mChatItem.size()-1) {
            if (chat.isIsseen()) {
                viewHolder.tv_isseen.setText("읽음");
            } else {
                viewHolder.tv_isseen.setText("읽지않음");
            }
        }else {
            viewHolder.tv_isseen.setVisibility(View.GONE);
        }




    }

    @Override
    public int getItemCount() {
        return mChatItem.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message, show_username;
        public ImageView profile_img, imgUri_message;

        public TextView tv_isseen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.chatRoom_message);
            show_username = itemView.findViewById(R.id.chatRoom_user_name);
            profile_img = itemView.findViewById(R.id.myfriend_profilePic);

            imgUri_message = itemView.findViewById(R.id.iv_chatRoom_imgUri);

            tv_isseen = itemView.findViewById(R.id.tv_seen);



        }
    }


    //리사이클러뷰에 여러종류의 뷰타입을 넣는메소드
    @Override
    public int getItemViewType(int position) {
        fuser= FirebaseAuth.getInstance().getCurrentUser();

        if(mChatItem.get(position).getSender().equals(fuser.getUid())){

            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }



    }
}