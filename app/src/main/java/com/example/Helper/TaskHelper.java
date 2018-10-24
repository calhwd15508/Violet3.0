package com.example.Helper;

import android.content.SharedPreferences;

import com.example.Database.Task;
import com.example.violet30.MainActivity;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by howardzhang on 10/20/18.
 */

/*
manages local storage of tasks on the phone
stores and provides current id number of tasks in preferences
 */
public class TaskHelper {

    //structure to hold local tasks
    private Map<String, Task> taskMap;
    //reference to MainActivity
    private MainActivity main;
    //holds current id number of tasks
    private int currentId;

    public TaskHelper(MainActivity main){
        //setup
        this.main = main;
        taskMap = new HashMap<>();

        //retrieve current id number of tasks from preferences
        SharedPreferences sp = main.getSharedPreferences("violet", MODE_PRIVATE);
        currentId = sp.getInt("taskId", 1);
    }

    //adds task to local storage
    public void addTask(Task task){
        taskMap.put(task.getTitle(), task);
    }

    //removes task from local storage
    public void removeTask(Task task){
        taskMap.remove(task.getTitle());
    }

    public void save(){
        //stores updated currentId into preferences
        SharedPreferences sp = main.getSharedPreferences("violet", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("taskId", currentId);

        //applies changes
        editor.apply();
    }

    //returns if a title is taken
    public boolean isTaken(String title){
        return taskMap.keySet().contains(title);
    }

    //return task given title
    public Task getTask(String title){
        return taskMap.get(title);
    }

    //returns currentId and updates it
    public int retrieveId(){
        currentId++;
        return currentId - 1;
    }
}
