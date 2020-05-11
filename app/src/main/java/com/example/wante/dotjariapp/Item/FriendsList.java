package com.example.wante.dotjariapp.Item;

public class FriendsList {

    public String  userId,userName,userImgUri,userEmail,userStatus,search,onOff;


    public FriendsList() {
    }

    public FriendsList(String userId, String userName, String userImgUri, String userEmail, String userStatus, String search, String onOff) {
        this.userId = userId;
        this.userName = userName;
        this.userImgUri = userImgUri;
        this.userEmail = userEmail;
        this.userStatus = userStatus;
        this.search = search;
        this.onOff = onOff;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImgUri() {
        return userImgUri;
    }

    public void setUserImgUri(String userImgUri) {
        this.userImgUri = userImgUri;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public String getOnOff() {
        return onOff;
    }

    public void setOnOff(String onOff) {
        this.onOff = onOff;
    }
}
