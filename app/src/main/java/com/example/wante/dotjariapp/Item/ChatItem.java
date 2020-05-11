package com.example.wante.dotjariapp.Item;

public class ChatItem {

    private String sender;
    private String receiver;
    private String message;
    private boolean isseen ;
    private String imgMessage;


    public ChatItem() {
    }

    public ChatItem(String sender, String receiver, String message, boolean isseen, String imgMessage) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.imgMessage = imgMessage;
    }

    public String getlmgMessage() {
        return imgMessage;
    }

    public void setImgMessage(String imgMessage) {
        this.imgMessage = imgMessage;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
