package com.example.Processor;

import com.example.Database.Alarm;
import com.example.Database.Task;
import com.example.IO.Detection;
import com.example.violet30.MainActivity;
import com.example.violet30.Time;

/**
 * Created by howardzhang on 12/28/17.
 */

/*
manages task execution
phase 3, task execution
managed by SpeechProcessor
 */
public class Tasker {

    //reference to main
    private MainActivity main;

    //constructor
    public Tasker(MainActivity main) {
        //setup
        this.main = main;
    }

    /*
    runs type of taskData
    no match -> default
    match -> run taskData
     */
    public void runTask(TaskData taskData) {
        switch (taskData.getTaskId()) {
            case TaskData.idShutdown:
                taskShutdown();
                break;
            case TaskData.idDetection:
                taskDetection(taskData);
                break;
            case TaskData.idAddAlarm:
                taskAddAlarm(taskData);
                break;
            case TaskData.idStopAlarm:
                taskStopAlarm();
                break;
            case TaskData.idRemoveAlarm:
                taskRemoveAlarm(taskData);
                break;
            case TaskData.idHeartRate:
                taskHeartRate();
                break;
            case TaskData.idAddTask:
                taskAddTask(taskData);
                break;
            case TaskData.idRemoveTask:
                taskRemoveTask(taskData);
                break;
            default:
                taskDefault();
        }
    }

    //TASKS
    /*
    default task simply creates another detection request,
    back to phase 1 detection
     */
    public void taskDefault() {
        main.detRequest();
    }

    //ends program
    private void taskShutdown() {
        main.finish();
    }

    /*
    changes detection mode
    no description -> switch modes
     */
    private void taskDetection(TaskData taskData) {
        String mode = (String) taskData.getTaskObject("mode");
        if (mode == null) {
            main.changeDetectionMode(-1);
        } else if (mode.contains("watch")) {
            main.changeDetectionMode(Detection.WATCH_DETECT);
        } else if (mode.contains("voice")){
            main.changeDetectionMode(Detection.VOICE_DETECT);
        }
        main.detRequest();
    }

    private void taskAddTask(TaskData taskData){
        String title = (String) taskData.getTaskObject("title");
        Task task = new Task();
        task.setTitle(title);
        task.setDesc("");
        task.setId(main.getCurrentIdTask());
        main.FBaddTask(task);
        main.ttsRequest("Added task titled " + title);
        main.detRequest();
    }

    /*
    adds alarm
    creates alarm object
    adds to firebase, which will call AlarmHelper to add alarm
     */
    private void taskAddAlarm(TaskData taskData) {
        String title = (String) taskData.getTaskObject("title");
        Time time = (Time) taskData.getTaskObject("time");
        Alarm alarm = new Alarm();
        alarm.setTime(time.getMs());
        alarm.setTimeString(time.toString());
        alarm.setId(main.getCurrentIdAlarm());
        alarm.setTitle(title);
        main.FBaddAlarm(alarm);
        main.ttsRequest("Setting alarm " + title + " for " + time);
        main.detRequest();
    }

    /*
    stop ongoing alarm
     */
    private void taskStopAlarm() {
        main.stopAlarm();
        main.detRequest();
    }

    private void taskRemoveTask(TaskData taskData){
        String title = (String) taskData.getTaskObject("title");
        Task task = main.getTask(title);
        main.FBremoveTask(task);
        main.ttsRequest("Removing task titled " + title);
        main.detRequest();
    }

    /*
    removes alarm by retrieving correct alarm
     */
    private void taskRemoveAlarm(TaskData taskData) {
        String title = (String) taskData.getTaskObject("title");
        Alarm alarm = main.getAlarm(title);
        main.FBremoveAlarm(alarm);
        main.ttsRequest("Removing alarm titled " + title
                + " for " + alarm.getTimeString());
        main.detRequest();
    }

    /*
    retrieves heart rate by starting heart rate scan
     */
    private void taskHeartRate() {
        main.ttsRequest("Scanning heart rate");
        main.scanHeartRate();
    }
}
