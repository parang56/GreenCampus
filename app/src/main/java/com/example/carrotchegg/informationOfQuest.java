package com.example.carrotchegg;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;

public class informationOfQuest{
    private String title;
    private String imageSource;
    private String name;
    private String text;
    private int views;
    private int answer;
    private int heart;
    private boolean image;
    private String subject;
    private int postNum;
    private List<String> heartUserId;
    private boolean alarm;
    private boolean push_alarm;

    @ServerTimestamp private Timestamp timestamp; // server timestamp
    public informationOfQuest() {}
    public informationOfQuest(String title, String imageSource, String name,String text, int views, int answer, String subject,int heart,
                              boolean image,int postNum,List<String> heartUserId,boolean alarm,boolean push_alarm) {
        this.title = title;
        this.imageSource = imageSource;
        this.views = views;
        this.name = name;
        this.answer = answer;
        this.text = text;
        this.subject = subject;
        this.heart = heart;
        this.image = image;
        this.postNum = postNum;
        this.heartUserId = heartUserId;
        this.alarm = alarm;
        this.push_alarm = push_alarm;
    }
    public String getTitle(){ return  title; }
    public String getImageSource(){ return  imageSource; }
    public String getName(){ return  name; }
    public String getText(){ return  text; }
    public int getViews(){ return  views; }
    public int getAnswer(){ return  answer; }
    public int getHeart(){ return  heart; }
    public boolean getImage(){return image;}
    public String getSubject(){return subject;}
    public int getPostNum(){return postNum;}
    public Timestamp getTimestamp() { return timestamp; }

    public boolean isAlarm() {
        return alarm;
    }

    public boolean isPush_alarm() {
        return push_alarm;
    }

    public List<String> getHeartUserId() {
        return heartUserId;
    }
}