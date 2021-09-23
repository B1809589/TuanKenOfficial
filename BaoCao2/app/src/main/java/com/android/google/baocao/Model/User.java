package com.android.google.baocao.Model;

public class User {
    public User (){
    }
    private String fullname;
    private String username;
    private String email;
    private String bio;
    private String imageurl;
    private String id;

    public User(String fullname,String username, String id, String email, String bio, String imageurl){
        this.fullname=fullname;
        this.username=username;
        this.id=id;
        this.bio=bio;
        this.imageurl=imageurl;
        this.email=email;
    }


    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
