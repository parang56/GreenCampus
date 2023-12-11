package com.example.carrotchegg;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.List;

public class informationOfAccuse {

    private String userName;
    private String title;
    private String subject;
    private int number;
    private List<String> reason;

    public informationOfAccuse(String userName,String title, List<String> reason,String subject,int number){
        this.reason = reason;
        this.title = title;
        this.userName = userName;
        this.subject = subject;
        this.number = number;
    }

    public List<String> getReason() {
        return reason;
    }

    public int getNumber() {
        return number;
    }

    public String getSubject() {
        return subject;
    }

    public void setReason(List<String> reason) {
        this.reason = reason;
    }

    public String getUserName(){return userName;}
}
