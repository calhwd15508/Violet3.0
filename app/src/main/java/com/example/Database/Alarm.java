package com.example.Database;

/**
 * Created by howardzhang on 5/16/18.
 */

/*
data class for firebase to store alarms
id - unique identifier for alarm, used to remove alarms
title - unique title for alarm
time - time of alarm saved as long from Time class
UID - identifier used by firebase
timeString - string description of time
 */
public class Alarm {
    private int id;
    private String title;
    private long time;
    private String UID;
    private String timeString;

    public int getId(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    public long getTime(){
        return time;
    }
    public String getUID() {
        return UID;
    }
    public String getTimeString(){
        return timeString;
    }

    public void setId(int id){
        this.id = id;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setTime(long time){
        this.time = time;
    }
    public void setUID(String UID){
        this.UID = UID;
    }
    public void setTimeString(String timeString){
        this.timeString = timeString;
    }
}
