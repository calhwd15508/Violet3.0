package com.example.Alarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.Database.Alarm;
import com.example.violet30.MainActivity;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by howardzhang on 2/18/18.
 */

/*
Adds alarms to phone
Saves alarms to preferences - necessary to keep
    alarm manager alarms and local alarms synced
Manages various alarm features
 */
public class AlarmHelper {

    //used to add alarms to phone
    private AlarmManager alarmManager;

    //reference to Main
    private MainActivity main;

    //unique alarm identifier
    private int currentId;

    //all alarms stored locally in map
    private Map<String, Alarm> localAlarms;

    //constructor
    public AlarmHelper(AlarmManager alarmManager, MainActivity main) {
        //setup
        this.alarmManager = alarmManager;
        this.main = main;
        localAlarms = new HashMap<>();

        //saves next available id to avoid repeats
        SharedPreferences sp = main.getSharedPreferences("violet", MODE_PRIVATE);
        currentId = sp.getInt("alarmId", -1);
    }

    //checks saved alarms to see if a title is already being used
    public boolean isTaken(String title){
        return localAlarms.keySet().contains(title);
    }

    /*
    returns saved alarm with provided title
    returns null if not available
     */
    public Alarm getAlarm(String title){
        return localAlarms.get(title);
    }

    /*
    adds alarm to phone
    saves alarm to preferences
    should only be called by firebase to make sure phone data matches firebase data

    on launch:
        firebase will store all alarms on firebase to local map
     */
    @TargetApi(21)
    public void addAlarm(Alarm alarm){
        if(!isTaken(alarm.getTitle())) {
            Intent i = new Intent(main, AlarmReceiver.class);
            i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            i.putExtra("title", alarm.getTitle());
            i.putExtra("id", alarm.getId());
            PendingIntent pi = PendingIntent.getBroadcast(main, alarm.getId(), i, 0);
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(alarm.getTime(), null), pi);
        }
        localAlarms.put(alarm.getTitle(), alarm);
    }

    /*
    removes alarm from phone
    removes alarm from preferences
    should only be called by firebase to make sure phone data matches firebase data
     */
    public void removeAlarm(Alarm alarm){
        alarmManager.cancel(PendingIntent.getBroadcast(main, alarm.getId(),
                new Intent(main, AlarmReceiver.class), 0));
        localAlarms.remove(alarm.getTitle());
        SharedPreferences sp = main.getSharedPreferences("violet", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("alarm" + alarm.getTitle());
        editor.apply();
    }

    /*
    adds alarms back to alarm manager
    only run on startup since alarm manager deletes alarms on phone restart
     */
    @TargetApi(21)
    public void loadAlarms(){
        for(Alarm alarm : localAlarms.values()){
            Intent i = new Intent(main, AlarmReceiver.class);
            i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            i.putExtra("title", alarm.getTitle());
            i.putExtra("id", alarm.getId());
            PendingIntent pi = PendingIntent.getBroadcast(main, alarm.getId(), i, 0);
            alarmManager.setAlarmClock(new AlarmManager.AlarmClockInfo(alarm.getTime(), null), pi);
        }
    }

    //returns next available id
    public int retrieveId() {
        currentId--;
        return currentId + 1;
    }

    //saves local variables to preferences (for persistent memory)
    public void save(){
        //saves next available id to preferences
        SharedPreferences sp = main.getSharedPreferences("violet", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("alarmId", currentId);

        //saves local array into memory
        for(Alarm alarm : localAlarms.values()){
            String store = alarm.getTitle() + ";" + alarm.getId() + ";" + alarm.getTime()
                    + ";" + alarm.getUID() + ";" + alarm.getTimeString();
            editor.putString("alarm" + alarm.getTitle(), store);
        }

        //commit these changes
        editor.apply();
    }
}
