package com.example.carrotchegg;


import java.util.List;

public class Subject {

    private List<String> name;
    private String text;
    private long users;


    public Subject() {}

    public Subject(List<String> name, String text, long users) {
        this.name = name;
        this.text = text;
        this.users = users;
    }

    public String getName() {
        return name.get(0);
    }

    public String getText() {
        return text;
    }

    public long getUsers() {
        return users;
    }
}
