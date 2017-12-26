package com.example.violet30;

import android.util.Log;

/**
 * Created by howardzhang on 12/25/17.
 */

public class Initialization {

    //object variables
    private MainActivity main;
    private Detection detect;
    private TTS tts;
    private GoogleSpeechRecognition googlerec;
    private boolean isDetectSetup = false;
    private boolean isTtsSetup = false;
    private boolean isGoogleRecSetup = false;

    public Initialization(MainActivity main){
        Log.d("Order", "Construction Initialization object");
        this.main = main;
        detect = new Detection(this.main, this);
        tts = new TTS(this.main, this);
        googlerec = new GoogleSpeechRecognition(this.main, this);
    }

    /*called when respective classes are done initializing,
    checks whether or not system is ready for launch each time*/
    public void detectSetup(){
        isDetectSetup = true;
        isInit();
    }
    public void ttsSetup(){
        isTtsSetup = true;
        isInit();
    }
    public void googleRecSetup(){
        isGoogleRecSetup = true;
        isInit();
    }

    //returns whether or not system is ready for launch
    public void isInit(){
        if(isDetectSetup && isGoogleRecSetup && isTtsSetup){
            main.launch();
        }
    }

    //used to access Initialization's constructed objects
    public Detection getDetection(){
        return detect;
    }
    public TTS getTts(){
        return tts;
    }
    public GoogleSpeechRecognition getGooglerec(){
        return googlerec;
    }
}
