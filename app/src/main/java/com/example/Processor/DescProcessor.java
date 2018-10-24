package com.example.Processor;

import com.example.Database.TaskerMethod;
import com.example.violet30.MainActivity;
import com.example.violet30.Time;

/**
 * Created by howardzhang on 7/14/18.
 */

/*
class responsible for parsing descriptions of tasks
called by SpeechProcessor
phase 2, parse descriptions -> phase 3, execution by Tasker
 */
public class DescProcessor {

    //reference to TaskData object that will be passed to Tasker for execution
    private TaskData taskData;
    //reference to description keywords determined by TaskerMethod type
    private String[] descWords;
    //reference to optional description keywords determined by TaskerMethod type
    private String optional;
    //reference to main
    private MainActivity main;
    //reference to SpeechProcessor
    private SpeechProcessor speechProcessor;

    /*
    stores the most recent description key
    used when getInfo gets extra information by creating a GSRequest
    controls where that extra info gets stored in the TaskData object
     */
    private String recentKey;

    //constructor
    public DescProcessor(TaskData taskData, TaskerMethod taskerMethod, String speech,
                         MainActivity main, SpeechProcessor speechProcessor){
        /*
        checks if TaskerMethod has any descriptions
        no -> send immediately to Tasker
         */
        if(taskerMethod.getDescwords() == null){
            speechProcessor.runTask(taskData);
        }else{
            //setup
            this.optional = taskerMethod.getOptional();
            if(this.optional == null){
                this.optional = "";
            }
            this.taskData = taskData;
            descWords = taskerMethod.getDescwords().split(";");
            this.main = main;
            this.speechProcessor = speechProcessor;
            recentKey = "";
            //phase 1, initial parsing
            setup(speech);
        }
    }

    /*
    phase 1, initial parsing
    finds all descriptions included in original speech
    calls phase 2, filling missing descriptions
     */
    private void setup(String speech){
        String[] speechSplit = speech.split(" ");
        String phrase = "";
        String key = "";
        for (String word : speechSplit) {
            for (String keyWord : descWords) {
                //check if keyword is too short -> equals instead of contains
                if(keyWord.length() < 4){
                    if (word.equals(keyWord)) {
                        if (!key.equals("")) {
                            taskData.addDesc(key, phrase.trim());
                        }
                        key = keyWord;
                        phrase = "";
                        break;
                    }
                }else {
                    if (word.contains(keyWord)) {
                        if (!key.equals("")) {
                            taskData.addDesc(key, phrase.trim());
                        }
                        key = keyWord;
                        phrase = "";
                        break;
                    }
                }
            }
            if (!phrase.equals("")) {
                phrase += word + " ";
            } else {
                if (!key.equals("")) {
                    phrase += " ";
                }
            }
        }
        if (!key.equals("")) {
            taskData.addDesc(key, phrase.trim());
        }
        fillMissing();
    }

    /*
    phase 2, attempts to fill missing descriptions
    calls getInfo if missing description is required
    calls phase 3, filling in the taskData object
    switcher:
        0 -> no missing, call phase 3
        1 -> missing, call gsRequest
        2 -> error, run default taskData
     */
    private void fillMissing(){
        int switcher = 0;
        for(String keyWord : descWords){
            if(taskData.getDesc(keyWord) == null){
                if(!optional.contains(keyWord)) {
                    switcher = getInfo(keyWord);
                    break;
                }
            }
        }
        switch(switcher) {
            case 0:
                createTaskObject();
                break;
            case 1:
                main.gsRequest();
                break;
            case 2:
                speechProcessor.runDefault();
                break;
        }
    }

    /*
    attempts to get extra information
    creates a google speech recognition request for extra information
    speech passed back into SpeechProcessor again
    SpeechProcessor passes extra information back into DescProcessor through setInfo
    error parsing -> run Default taskData in Tasker
    1 -> gs Request required
    2 -> error -> run Default taskData in Tasker
     */
    private int getInfo(String keyWord){
        switch(keyWord){
            case "title":
                recentKey = "title";
                main.ttsRequest("Title required.");
                return 1;
            case "time":
                recentKey = "time";
                main.ttsRequest("Time required.");
                return 1;
            default:
                main.ttsRequest("Error getting info.");
                return 2;
        }
    }

    /*
    called by SpeechProcessor when extra information speech is passed to it
    extra speech passed to setInfo to add to TaskData object
    transitions back into phase 2, filling in missing descriptions
     */
    public void setInfo(String speech){
        taskData.addDesc(recentKey, speech);
        fillMissing();
    }

    /*
    phase 3, filling in taskData object
    fills in taskObject map (look at TaskData object for more info)
    creates the objects to be put into taskObject map
    calls createObjects helper
    switcher:
        0 -> success, runTask
        1 -> error creating object, gsRequest
        2 -> error parsing, run default TaskData
     */
    private void createTaskObject(){
        int switcher = 0;
        for(String keyWord : descWords){
            String value = taskData.getDesc(keyWord);
            if(value != null){
                switcher = createObject(keyWord, value);
                if(switcher != 0){
                    break;
                }
            }
        }
        switch(switcher){
            case 0:
                speechProcessor.runTask(taskData);
                break;
            case 1:
                main.gsRequest();
                break;
            case 2:
                speechProcessor.runDefault();
                break;
        }
    }

    /*
    creates objects to be put into taskObject map
    uses Strings stored in description map (look at TaskData object for more info)
    error creating -> replaces description map value with null, call GSRequest
        go back to phase 2, filling in missing descriptions
    error parsing -> run default taskData in Tasker
     */
    private int createObject(String key, String value){
        switch(key){
            case "title":
                //title is taken is caught - alarm
                if(taskData.getTaskId() == TaskData.idAddAlarm){
                    if(main.alarmIsTaken(value)){
                        taskData.addDesc(key, null);
                        main.ttsRequest("the title " + value + " is taken." +
                                "Please select a valid title.");
                        recentKey = key;
                        return 1;
                    }else{
                        taskData.addObject(key, value);
                        return 0;
                    }
                }
                //title is taken is caught - taskData
                else if(taskData.getTaskId() == TaskData.idAddTask) {
                    if(main.taskIsTaken(value)){
                        taskData.addDesc(key, null);
                        main.ttsRequest("the title " + value + " is taken." +
                                "Please select a valid title.");
                        recentKey = key;
                        return 1;
                    }else{
                        taskData.addObject(key, value);
                        return 0;
                    }
                }
                //title is not used is caught - alarm
                else if(taskData.getTaskId() == TaskData.idRemoveAlarm){
                    if(!main.alarmIsTaken(value)){
                        taskData.addDesc(key, null);
                        main.ttsRequest("I have no record of an alarm titled " + value +
                                ". Please select a valid title.");
                        recentKey = key;
                        return 1;
                    }else{
                        taskData.addObject(key, value);
                        return 0;
                    }
                }
                //title is not used is caught - taskData
                else if (taskData.getTaskId() == TaskData.idRemoveTask){
                    if(!main.taskIsTaken(value)){
                        taskData.addDesc(key, null);
                        main.ttsRequest("I have no record of a task titled " + value +
                                ". Please select a valid title.");
                        recentKey = key;
                        return 1;
                    }else{
                        taskData.addObject(key, value);
                        return 0;
                    }
                }
                return 2;
            case "time":
                //error parsing time is caught
                try{
                    Time time = new Time(value);
                    taskData.addObject(key, time);
                    return 0;
                }catch(IllegalArgumentException e){
                    taskData.addDesc(key, null);
                    main.ttsRequest("Could not parse time speech." +
                            "Could you repeat that?");
                    recentKey = key;
                    return 1;
                }
            case "mode":
                //mode is not possible is caught
                if(!(value.equals("voice")||value.equals("watch"))){
                    taskData.addDesc(key, null);
                    main.ttsRequest(value + " detection mode does not exist." +
                            "Please select a valid mode.");
                    recentKey = key;
                    return 1;
                }else{
                    taskData.addObject(key, value);
                    return 0;
                }
            default:
                main.ttsRequest("Error creating objects from speech.");
                return 2;
        }
    }

}
