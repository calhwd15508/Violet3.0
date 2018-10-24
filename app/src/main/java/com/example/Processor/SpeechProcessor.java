package com.example.Processor;

import com.example.Database.TaskerMethod;
import com.example.violet30.MainActivity;

/**
 * Created by howardzhang on 7/6/18.
 */

/*
Management class that handles speech processing and execution
phase 1, initial parsing (Speech) ->
phase 2, description parsing (DescProcessor) ->
phase 3, task execution (Tasker)
 */
public class SpeechProcessor {

    //reference to tasker
    private Tasker tasker;
    //reference to speech
    private Speech speech;
    //reference to descProcessor (unlike speech and tasker, unique object for every task)
    private DescProcessor descProcessor;
    //reference to main
    private MainActivity main;

    //killWord that stops processing immediately
    private String killWord = "kill";

    //stores the speech to be processed
    private String speechString;
    /*
    boolean value that stores whether or not something is being processed
        if something is being processed that means speech is simply extra info
        requested by DescProcessor
    set to false if task is executed
    set to true when new speech arrives to be parsed and it was originally false
      */
    private boolean isProcessing;

    //constructor
    public SpeechProcessor(MainActivity main){
        //setup
        tasker = new Tasker(main);
        speech = new Speech(this);
        this.main = main;
        isProcessing = false;
        descProcessor = null;
    }

    /*
    stops current "conversation"
     */
    public void stopProcessing(){
        descProcessor = null;
        isProcessing = false;
    }

    /*
    called by Speech when initial parsing is done
    phase 1, initial parsing -> phase 2, description parsing
    creates new DescProcessor
     */
    public void getDesc(TaskerMethod result){
        descProcessor = new DescProcessor(new TaskData(result.getTaskId()),
                result, speechString, main, this);
    }

    /*
    called by main when speech arrives from Google Speech Recognition to be processed
    isProcessing true -> speech is extra info for phase 2, sends to descProcessor
    isProcessing false -> new task, calls Speech object for phase 1 initial parsing
     */
    public void processSpeech(String speechString){
        this.speechString = speechString.toLowerCase().trim();
        if(speechString.contains(killWord)){
            stopProcessing();
            runDefault();
        }
        if(!isProcessing){
            isProcessing = true;
            speech.processSpeech(this.speechString);
        }else{
            descProcessor.setInfo(this.speechString);
        }
    }


    //called by Firebase to manage local array of taskerMethods stored in Speech
    public void clearTaskerMethods(){
        speech.clearTaskerMethods();
    }
    public void addTaskerMethod(TaskerMethod taskerMethod){
        speech.addTaskerMethod(taskerMethod);
    }

    /*
    phase 3, task execution
    sets isProcessing to false
    error parsing -> default method run
    successful parsing -> correct task is run
     */
    public void runDefault(){
        isProcessing = false;
        descProcessor = null;
        tasker.taskDefault();
    }
    public void runTask(TaskData taskData){
        isProcessing = false;
        descProcessor = null;
        tasker.runTask(taskData);
    }

}
