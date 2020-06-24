package com.example.chatapp2.Model;

public class Post {
    private String userid;
    private String status;
    private String image;
    private int like_count;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Post(String userid, String status, String image, int like_count, String id) {
        this.userid = userid;
        this.status = status;
        this.image = image;
        this.like_count = like_count;
        this.id = id;
    }

    public Post() {

    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getLike_count() {
        return like_count;
    }

    public void setLike_count(int like_count) {
        this.like_count = like_count;
    }
}
