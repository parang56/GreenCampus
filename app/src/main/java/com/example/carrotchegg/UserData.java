package com.example.carrotchegg;


public class UserData {

    private String subject;
    private long post;


    public UserData() {
    }

    public UserData(String subject, Long post) {
        this.subject = subject;
        this.post = post;
    }

    public String getSubject() {
        return subject;
    }

    public long getPost() {
        return post;
    }
}