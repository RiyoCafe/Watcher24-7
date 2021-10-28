package com.example.watcher;

public class Contacts {
    private String username,image,type,email,uid;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public Contacts(String username, String image, String type, String email, String uid) {
        this.username = username;
        this.image = image;
        this.type = type;
        this.email = email;
        this.uid = uid;
    }

    public Contacts() {
    }

    public String getusername() {
        return username;
    }

    public void setusername(String username) {
        this.username = username;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
