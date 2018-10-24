package com.example.Alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.violet30.MainActivity;

/**
 * Created by howardzhang on 2/21/18.
 */

/*
receiver class called by alarm manager
receives incoming alarms
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static int id;

    /*
    if running - send broadcast to be caught by main to start alarm
    if not running - start activity with flag to start alarm
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if(MainActivity.running && intent.getIntExtra("id", 0) != id){
            Intent i = new Intent("ALARM");
            i.putExtra("title", intent.getStringExtra("title"));
            context.sendBroadcast(i);
        }else if(!MainActivity.running){
            id = intent.getIntExtra("id", 0);
            Intent i = new Intent(context, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("alarm", true);
            i.putExtra("title", intent.getStringExtra("title"));
            context.startActivity(i);
        }
    }
}
