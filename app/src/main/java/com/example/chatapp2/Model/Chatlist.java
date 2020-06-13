package com.example.chatapp2.Model;

import java.util.HashMap;

public class Chatlist {
    private String id;
    private long timestamp;

    public Chatlist(String id, long timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public Chatlist() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
