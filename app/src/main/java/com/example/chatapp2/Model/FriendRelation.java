package com.example.chatapp2.Model;

public class FriendRelation {
    private String user;
    private String friend;

    public FriendRelation(String user, String friend) {
        this.user = user;
        this.friend = friend;
    }

    public FriendRelation() {

    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getFriend() {
        return friend;
    }

    public void setFriend(String friend) {
        this.friend = friend;
    }
}
