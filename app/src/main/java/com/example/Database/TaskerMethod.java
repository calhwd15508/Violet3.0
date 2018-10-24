package com.example.Database;

/*
data class used by firebase to store types of tasks
used to process speech
taskId - unique identifier for task
descwords - stores description keywords
keywords - stores keywords for task selection
title - unique title for tasks
optional - stores optional keywords for task selection
 */
public class TaskerMethod{

    //object variables
    private int taskId;
    private String descwords;
    private String keywords;
    private String title;
    private String optional;

    public int getTaskId(){
        return taskId;
    }
    public String getKeywords(){
        return keywords;
    }
    public String getTitle(){
        return title;
    }
    public String getDescwords(){
        return descwords;
    }
    public String getOptional(){
        return optional;
    }

    public void setTaskId(int taskId){
        this.taskId = taskId;
    }
    public void setKeywords(String keywords){
        this.keywords = keywords;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setDescwords(String descwords){
        this.descwords = descwords;
    }
    public void setOption(String optional){
        this.optional = optional;
    }
}