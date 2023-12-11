package com.example.carrotchegg;

import com.google.firebase.Timestamp;

public class UserModel {
    private int answer;
    private String email;
    private  int exp;
    String id;
    boolean image;
    String introduction;
    int level;
    private String nickname;
    String password;
    private int question;
    private String fcmToken;

    public UserModel() {
    }

    public UserModel( int answer,String email, int exp, String id, boolean image, String introduction, int level,String nickname,String password,String[] subject, String fcmToken) {
        this.nickname = nickname;
        this.fcmToken = fcmToken;
    }



    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}