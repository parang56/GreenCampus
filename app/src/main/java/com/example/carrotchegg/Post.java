package com.example.carrotchegg;


import android.content.Intent;
import android.util.Log;

public class Post implements Comparable<Post>{

    private String title, text, imageSource;
    private long answer, heart, postNum;

    private boolean image;
    public int image_path;


    public Post() {}

    public Post(String title, String text, long heart, long answer, boolean image, String imageSource, long postNum) {
        this.title = title;
        this.text = text;
        this.answer = answer;
        this.heart = heart;
        this.image = image;
        this.imageSource = imageSource;
        this.postNum = postNum;
    }

    public String getTitle() {
        return title;
    }
    public String getText() {
        return text;
    }
    public String getAnswer() {
        return Long.toString(answer);
    }
    public String getHeart() {
        return Long.toString(heart);
    }
    public boolean getImage() {
        return image;
    }
    public String getImageSource() {
        return imageSource;
    }
    public int getPostNum() {
        return (int) postNum;
    }


    @Override
    public int compareTo(Post post) {

        int a = Integer.parseInt(this.getHeart());
        int b = Integer.parseInt(post.getHeart());
        return b-a;
    }
}
