package com.example.violet30;

import android.widget.TextView;

/**
 * Created by howardzhang on 10/21/18.
 */

/*
Class that tracks multiple states of the app:
    Detection Mode
    Display Connection
    Testing Mode
 */
public class InfoManager {
    public String detection = "Watch";
    public String display = "Off";
    public String testing = "Off";

    private TextView info;

    public InfoManager(TextView textView){
        info = textView;
    }
    public void setDetection(String detection){
        this.detection = detection;
        changeText(this.detection, this.display, this.testing);
    }
    public void setDisplay(String display){
        this.display = display;
        changeText(this.detection, this.display, this.testing);
    }
    public void setTesting(String testing){
        this.testing = testing;
        changeText(this.detection, this.display, this.testing);
    }
    private void changeText(String detection, String display, String testing){
        info.setText("Detect:\t"+detection
                +"\nDisplay:\t"+display
                +"\nTesting:\t"+testing);
    }
}
