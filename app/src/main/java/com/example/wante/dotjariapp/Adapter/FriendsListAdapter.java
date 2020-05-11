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

import com.bumptech.glide.Glide;
import com.example.wante.dotjariapp.Item.FriendsList;
import com.example.wante.dotjariapp.R;
import com.example.wante.dotjariapp.UserProfileActivity;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.ViewHolder> {

    private Context mContext;
    private List<FriendsList> mFriendsList;

    public FriendsListAdapter(Context mContext, List<FriendsList> mFriendsList) {
        this.mContext = mContext;
        this.mFriendsList = mFriendsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.item_refuse_friends_single, viewGroup, false);
        return new FriendsListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        // 아이템에 텍스트와 이미지를 셋팅한다.
        final FriendsList list = mFriendsList.get(i);
        viewHolder.myFriendsName.setText(list.getUserName());
        viewHolder.myFriendsId.setText(list.getUserId());
        viewHolder.myFriendsEmail.setText(list.getUserEmail());
        viewHolder.myFriendsStatus.setText(list.getUserStatus());
        if(list.getUserImgUri().equals("default")){
            viewHolder.myFriendsProfilePic.setImageResource(R.drawable.ic_my_photo_24dp);
        }else {
            Glide.with(mContext).load(list.getUserImgUri()).into(viewHolder.myFriendsProfilePic);
        }



        // 마이페이지에 회원정보
        // 아이템을 누르면 유저프로필 액티비티로 이동하고
        // 정보들이 인텐트로 넘어간다.
        viewHolder.myFriendsProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, UserProfileActivity.class);
                intent.putExtra("userId", list.getUserId());
                intent.putExtra("userName", list.getUserName());
                intent.putExtra("userEmail",list.getUserEmail());
                intent.putExtra("userImgUri", list.getUserImgUri());
                intent.putExtra("userStatus", list.getUserStatus());
                intent.putExtra("search", list.getSearch());
                intent.putExtra("onOff", list.getOnOff());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mFriendsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView myFriendsName,myFriendsId,myFriendsEmail,myFriendsStatus;
        private CircleImageView myFriendsProfilePic;
        private ConstraintLayout myFriendsProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            myFriendsStatus = itemView.findViewById(R.id.tv_myFirends_status);
            myFriendsEmail = itemView.findViewById(R.id.tv_myFriends_email);
            myFriendsId = itemView.findViewById(R.id.tv_myFriends_id);
            myFriendsName = itemView.findViewById(R.id.tv_myFriends_name);
            myFriendsProfilePic = itemView.findViewById(R.id.myfriend_profilePic);
            myFriendsProfile = itemView.findViewById(R.id.cl_refuseFriendsProfile);


        }
    }

}
