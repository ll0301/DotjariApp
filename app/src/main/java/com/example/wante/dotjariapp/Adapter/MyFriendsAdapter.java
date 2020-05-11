package com.example.wante.dotjariapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.wante.dotjariapp.ChatRoomActivity;
import com.example.wante.dotjariapp.Item.FriendsList;
import com.example.wante.dotjariapp.Item.UserProfile;
import com.example.wante.dotjariapp.R;
import com.example.wante.dotjariapp.UserProfileActivity;
import com.firebase.ui.auth.data.model.User;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyFriendsAdapter extends RecyclerView.Adapter<MyFriendsAdapter.ViewHolder> {


    private Context mContext;
    private List<UserProfile> mUsers;

    public MyFriendsAdapter(Context mContext, List<UserProfile> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

       View view = LayoutInflater.from(mContext).inflate(R.layout.item_myfirends_single, viewGroup, false);
        return new MyFriendsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        final UserProfile user = mUsers.get(i);
        viewHolder.myFriendsName.setText(user.getUserName());
        viewHolder.myFriendsEmail.setText(user.getUserEmail());
        viewHolder.myFriendsStatus.setText(user.getUserStatus());
        viewHolder.myFriendsId.setText(user.getUserId());
        if (user.getUserImgUri().equals("default")) {
            viewHolder.myFriendsProfilePic.setImageResource(R.drawable.ic_my_photo_24dp);
        }else {
            Glide.with(mContext).load(user.getUserImgUri()).into(viewHolder.myFriendsProfilePic);
        }


        // 마이페이지에 회원정보
        viewHolder.myFriendsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserProfileActivity.class);
                intent.putExtra("userId", user.getUserId());
                intent.putExtra("userName", user.getUserName());
                intent.putExtra("userStatus", user.getUserStatus());
                intent.putExtra("userEmail",user.getUserEmail());
                intent.putExtra("userImgUri", user.getUserImgUri());
                intent.putExtra("onOff", user.getOnOff());
                intent.putExtra("search", user.getSearch());
                mContext.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView myFriendsName, myFriendsStatus, myFriendsEmail, myFriendsId;
        private CircleImageView myFriendsProfilePic;
        private ConstraintLayout myFriendsProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            myFriendsEmail = itemView.findViewById(R.id.tv_myFriends_email);
            myFriendsStatus = itemView.findViewById(R.id.tv_myFriends_status);
            myFriendsName = itemView.findViewById(R.id.tv_myFriends_name);
            myFriendsId = itemView.findViewById(R.id.tv_myFriends_id);
           myFriendsProfilePic = itemView.findViewById(R.id.myfriend_profilePic);

           myFriendsProfile = itemView.findViewById(R.id.cl_myFriends_profile);
        }
    }

}
