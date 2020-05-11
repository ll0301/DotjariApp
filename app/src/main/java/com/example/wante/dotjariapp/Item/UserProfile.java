package com.example.wante.dotjariapp.Item;



public class UserProfile {


        public String userId;
        public String userEmail;
        public String userName;
        public String userStatus;
        public String userImgUri;

        // 사용자 접속여부
        public String onOff;
        public String search;

        public UserProfile() {

        }

    public UserProfile(String userId, String userEmail, String userName, String userStatus,
                       String userImgUri, String onOff, String search) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.userStatus = userStatus;
        this.userImgUri = userImgUri;
        this.onOff = onOff;
        this.search = search;

    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
    }

    public String getUserImgUri() {
        return userImgUri;
    }

    public void setUserImgUri(String userImgUri) {
        this.userImgUri = userImgUri;
    }

    public String getOnOff() {
        return onOff;
    }

    public void setOnOff(String onOff) {
        this.onOff = onOff;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }
}
