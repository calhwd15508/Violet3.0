package com.example.Processor;

import com.example.Database.TaskerMethod;

import java.util.ArrayList;

/**
 * Created by howardzhang on 6/25/18.
 */

/*
handles initial parsing of speech to find TaskerMethod (what type of task it is)
phase 1, initial parsing -> phase 2, description parsing
managed by SpeechProcessor class
 */
public class Speech {

    //holds local array of all TaskerMethods filled by Firebase
    private ArrayList<TaskerMethod> taskerMethods;
    //reference to SpeechProcessor
    private SpeechProcessor speechProcessor;

    //constructor
    public Speech(SpeechProcessor speechProcessor){
        //setup
        this.speechProcessor = speechProcessor;
        taskerMethods = new ArrayList<>();
    }

    //called by firebase to create local array of TaskerMethods
    public void clearTaskerMethods(){
        taskerMethods = new ArrayList<>();
    }
    public void addTaskerMethod(TaskerMethod taskerMethod){
        taskerMethods.add(taskerMethod);
    }

    //initial parsing of speech
    public void processSpeech(String speech){
        int max = 0;
        int confidence;
        TaskerMethod result = null;

        for(TaskerMethod taskerMethod : taskerMethods){
            confidence = findConfidenceScore(speech.split(" "), taskerMethod.getKeywords());
            if(max < confidence){
                max = confidence;
                result = taskerMethod;
            }
        }
        if(result == null){
            speechProcessor.runDefault();
        }else {
            speechProcessor.getDesc(result);
        }

    }

    /*
    helper method that finds confidence for each type of TaskerMethod
    selects max confidence score of all combinations of keywords for a specific TaskerMethod
     */
    private int findConfidenceScore(String[] speech, String keywords){
        String[] keywordList = keywords.split(";");
        int max = 0;
        for(String keyword : keywordList){
            int result = confScoreHelper(speech, keyword.split(" "));
            if(max < result){
                max = result;
            }
        }
        return max;
    }

    //helper method that finds confidence score of each combination of keywords
    private int confScoreHelper(String[] speech, String[] keywords){
        int conf = 0;
        for(String word : speech){
            for(String key : keywords){
                if(word.contains(key)){
                    conf++;
                }
            }
        }
        return conf;
    }
}
