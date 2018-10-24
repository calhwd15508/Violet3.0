package com.example.Database;

/**
 * Created by howardzhang on 10/22/18.
 */

/*
data class for Firebase control of VIOLET through display
    id = type of command
    command = String text to represent command
    UID = String unique identifier used by Firebase
 */
public class Display {
    private int id;
    private String command;
    private String UID;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
