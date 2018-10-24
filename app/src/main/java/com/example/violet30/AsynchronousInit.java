package com.example.violet30;

/**
 * Created by howardzhang on 3/3/18.
 */

/*
class that handles asynchronous setups
TextToSpeech and PocketSphinx both setup with asynchronous tasks
AsynchronousInit allows them to setup in parallel (speeds up initialization)
calls launch() in main method only when everything is setup
 */
public class AsynchronousInit {

    //boolean tracking TextToSpeech setup
    private boolean ttsSetup;
    //boolean tracking PocketSphinx setup
    private boolean psSetup;
    //reference to main
    private MainActivity main;

    //constructor
    public AsynchronousInit(MainActivity main){
        //setup
        this.main = main;
        ttsSetup = false;
        psSetup = false;
    }

    /*
    called by TTS when it is finished setting up
    attempts to launch
     */
    public void ttsSetup(){
        ttsSetup = true;
        launch();
    }
    /*
    called by PocketSphinx when it is finished setting up
    attempts to launch
     */
    public void psSetup(){
        psSetup = true;
        launch();
    }

    /*
    method that attempts to launch
    checks booleans to see if everything is ready
     */
    public void launch(){
        if(psSetup && ttsSetup){
            main.launch();
        }
    }
}
