package com.example.Database;

/**
 * Created by howardzhang on 5/16/18.
 */

/*
data class for firebase to store notifications
title - title for notification
UID - identifier used by firebase
text - text for notification
 */
public class Notification {
    private String title;
    private String UID;
    private String text;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
