package com.example.chatapp2.Model;

public class Chat {

    private String sender;
    private String receiver;
    private String message;
    private boolean is_seen;
    private boolean is_image;

    public Chat() {
    }

    public Chat(String sender, String receiver, String message, boolean is_seen, boolean is_image) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.is_seen = is_seen;
        this.is_image = is_image;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIs_seen() {
        return is_seen;
    }

    public void setIs_seen(boolean is_seen) {
        this.is_seen = is_seen;
    }

    public boolean isIs_image() {
        return is_image;
    }

    public void setIs_image(boolean is_image) {
        this.is_image = is_image;
    }
}
