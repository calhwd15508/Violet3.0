package com.example.Database;

/**
 * Created by howardzhang on 10/20/18.
 */

/*
data class for firebase to store tasks
id - unique identifier for task
title - unique title for task
UID - identifier used by firebase
desc - string description of task
 */
public class Task {

    private int id = 0;
    private String title;
    private String UID;
    private String desc;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
