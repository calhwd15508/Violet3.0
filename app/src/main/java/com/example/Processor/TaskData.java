package com.example.Processor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by howardzhang on 6/25/18.
 */

/*
task object to manage task creation
created by phase 1, initial parsing (Speech)
added to phase 2, description parsing (DescProcessor)
executed by phase 3, task execution (Tasker)
 */
public class TaskData {

    /*
    Why 2 maps?
    one stores the string description of each description type
    other stores the object description of each description type
    used by DescProcessor as a way to manage description parsing
    first speech is parsed and added to string map
    then individual strings are passed and turned into objects (if necessary)
     */

    //map of description keywords to string descriptions
    private Map<String, String> descriptions;
    //map of description keywords to description objects
    private Map<String, Object> taskObject;
    //unique identifier controlling type of task, used by Tasker
    private int taskId;

    //unique identifiers for type of task
    public static final int idShutdown = 1;
    public static final int idAddTask = 2;
    public static final int idDetection = 3;
    public static final int idAddAlarm = 4;
    public static final int idStopAlarm = 5;
    public static final int idRemoveAlarm = 6;
    public static final int idHeartRate = 7;
    public static final int idRemoveTask = 8;

    //constructor
    public TaskData(int taskId){
        //setup
        this.taskId = taskId;
        descriptions = new HashMap<>();
        taskObject=  new HashMap<>();
    }

    //adds an object to taskObject
    public void addObject(String key, Object value){
        taskObject.put(key, value);
    }
    //adds a string to descriptions
    public void addDesc(String key, String value){
        descriptions.put(key, value);
    }

    //access method for taskID
    public int getTaskId(){
        return taskId;
    }
    //access method for descriptions map
    public String getDesc(String key){
        return descriptions.get(key);
    }
    //access method for task object map
    public Object getTaskObject(String key){
        return taskObject.get(key);
    }

}
