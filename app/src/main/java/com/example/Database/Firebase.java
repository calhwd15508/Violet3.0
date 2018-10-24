package com.example.Database;

import com.example.violet30.MainActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by howardzhang on 1/7/18.
 */

/*
Class used to control firebase
Manages firebase listeners
Adds and removes from firebase
 */
public class Firebase {

    //reference to database
    private DatabaseReference database;
    //reference to main
    private final MainActivity main;
    //listener for taskerMethod data
    private ValueEventListener listenerTaskerMethod;
    //listener for alarm data
    private ChildEventListener listenerAlarms;
    //listener for task data
    private ChildEventListener listenerTasks;
    //listener for displayOn
    private ChildEventListener listenerDisplay;
    //variable used for setup
    private static boolean persistenceEnabled = false;

    //database location names
    private final String taskerMethods = "taskerMethods";
    private final String tasks = "tasks";
    private final String alarms = "alarms";
    private final String notifications = "notifications";
    private final String display = "display";

    //display command ids
    private final int displayOnOff = 0;

    //constructor
    public Firebase(MainActivity main) {

        //setup
        if (!persistenceEnabled) {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
            persistenceEnabled = true;
        }
        this.main = main;

        //get database reference
        database = FirebaseDatabase.getInstance().getReference();

        //initialize database listeners
        /*
        taskerMethod listener
        cannot add or remove, can only be changed from firebase console
        calls a local change to taskerMethod list stored by SpeechProcessor -> Speech
         */
        listenerTaskerMethod = database.child(taskerMethods).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Firebase.this.main.clearTaskerMethods();
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    TaskerMethod taskerMethod = data.getValue(TaskerMethod.class);
                    Firebase.this.main.addTaskerMethod(taskerMethod);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Firebase.this.main.error(databaseError.getMessage());
            }
        });
        /*
        display listener
            listens for changes updated by display program
         */
        listenerDisplay = database.child(display).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Display displayObject = dataSnapshot.getValue(Display.class);
                switch(displayObject.getId()){
                    case displayOnOff:
                        if(displayObject.getCommand().equals("true")) {
                            Firebase.this.main.displayMode(true);
                        }else{
                            Firebase.this.main.displayMode(false);
                        }
                        break;
                    default:
                        database.child(display).child(displayObject.getUID()).removeValue();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Display displayObject = dataSnapshot.getValue(Display.class);
                switch(displayObject.getId()){
                    case displayOnOff:
                        if(displayObject.getCommand().equals("true")) {
                            Firebase.this.main.displayMode(true);
                        }else{
                            Firebase.this.main.displayMode(false);
                        }
                        break;
                    default:
                        database.child(display).child(displayObject.getUID()).removeValue();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*
        alarm listener
            calls for changes handled by AlarmHelper
         */
        listenerAlarms = database.child(alarms).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Alarm alarm = dataSnapshot.getValue(Alarm.class);
                Firebase.this.main.addAlarm(alarm);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                //Alarm alarm = dataSnapshot.getValue(Alarm.class);
                //Firebase.this.main.changeAlarm(alarm);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Alarm alarm = dataSnapshot.getValue(Alarm.class);
                Firebase.this.main.removeAlarm(alarm);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Firebase.this.main.error(databaseError.getMessage());
            }
        });
        /*
        task listener
            can be added or removed by TaskHelper
         */
        listenerTasks = database.child(tasks).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Task task = dataSnapshot.getValue(Task.class);
                Firebase.this.main.addTask(task);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Task task = dataSnapshot.getValue(Task.class);
                Firebase.this.main.removeTask(task);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Firebase.this.main.error(databaseError.getMessage());
            }
        });
    }

    /*
    releases firebase resources
    removes all listeners
     */
    public void destroy() {
        database.child(taskerMethods).removeEventListener(listenerTaskerMethod);
        database.child(alarms).removeEventListener(listenerAlarms);
        database.child(tasks).removeEventListener(listenerTasks);
        database.child(display).removeEventListener(listenerDisplay);
        database = null;
    }

    //NOTIFICATION CALLS
    public void addNotification(Notification notification){
        notification.setUID(database.child(notifications).push().getKey());
        database.child(notifications).child(notification.getUID()).setValue(notification);
    }

    //ALARM CALLS
    public void addAlarm(Alarm alarm){
        alarm.setUID(database.child(alarms).push().getKey());
        database.child(alarms).child(alarm.getUID()).setValue(alarm);
    }
    public void removeAlarm(Alarm alarm){
        database.child(alarms).child(alarm.getUID()).removeValue();
    }

    //TASK CALLS
    public void addTask(Task task){
        task.setUID(database.child(tasks).push().getKey());
        database.child(tasks).child(task.getUID()).setValue(task);
    }
    public void removeTask(Task task) {
        database.child(tasks).child(task.getUID()).removeValue();
    }

}
