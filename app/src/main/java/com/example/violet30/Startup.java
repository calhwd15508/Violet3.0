package com.example.violet30;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by howardzhang on 7/6/18.
 */

/*
Receiver called on phone startup
used to start up the app on phone startup
 */
public class Startup extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //gives main info so it can perform startup tasks
        i.putExtra("startup", true);
        context.startActivity(i);
    }
}
