package com.example.carrotchegg;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;

public class informationOfAns{
    String content;
    boolean image;
    private int postNum;
    private int heart;
    private String imageSource;
    private int views;
    @ServerTimestamp
    private Timestamp timestamp; // server timestamp
    private String userName;
    private String subject;
    private int ansNum;
    private List<String> heartUserId;
    public informationOfAns(){ }
    public informationOfAns(String content,boolean image, int postNum, int heart,String imageSource,int views,String userName,String subject,int ansNum,List<String> heartUserId){
        this.content = content;
        this.image = image;
        this.postNum = postNum;
        this.heart = heart;
        this.imageSource = imageSource;
        this.views = views;
        this.userName = userName;
        this.subject = subject;
        this.ansNum = ansNum;
        this.heartUserId = heartUserId;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }
    public boolean getImage(){
        return image;
    }

    public int getHeart() {
        return heart;
    }

    public int getPostNum() {
        return postNum;
    }
    public int getAnsNum(){return ansNum;}

    public int getViews() {
        return views;
    }
    public String getImageSource(){
        return imageSource;
    }
    public Timestamp getTimestamp() { return timestamp; }
    public String getUserName(){return userName;}
    public List<String> getHeartUserId() {
        return heartUserId;
    }
}
