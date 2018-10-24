package com.example.IO;

import android.util.Log;

import com.example.violet30.MainActivity;
import com.example.visualizer.VisualizerView;

import java.util.ArrayList;

/**
 * Created by howardzhang on 12/24/17.
 */

/*
Class that schedules between TextToSpeech, Google Speech Recognition, and Detection classes
Requests made by various classes, called by main
Request represented by a node
Node added to a queue
TTS, GS, and DET communicate with IOManager to progress through the queue
Handles errors called by TTS, GS, and DET
Handles other functions used by TTS, GS, and DET
 */
public class IOManager {

    //unique identifiers for difference request types
    public static final int TTS_REQUEST = 1;        //Text to Speech request
    public static final int GS_REQUEST = 2;         //Google Speech Recognition request
    public static final int DET_REQUEST = 3;        //Detection

    //variable used to track bluetooth reconnection status
    public boolean bluetoothReconnect = false;

    //reference to tts object
    private TTS tts;
    //reference to gs object
    private GoogleSpeechRecognition googlerec;
    //reference to det object
    private Detection detect;
    //reference to Main
    private MainActivity main;

    //queue that stores and schedules requests
    private ArrayList<Node> queue;

    //constructor
    public IOManager(MainActivity main, VisualizerView visualizerView) {
        //setup
        this.queue = new ArrayList<Node>();
        this.main = main;
        tts = new TTS(this, visualizerView);
        googlerec = new GoogleSpeechRecognition(this);
        detect = new Detection(this);
    }

    //watch functions
    //connect to watch
    public void establishConnection(){
        detect.establishConnection();
    }
    //start watch vibration
    public void startWatchVibrate(){
        detect.startWatchVibrate();
    }
    //stop watch vibration
    public void stopWatchVibrate(){
        detect.stopWatchVibrate();
    }
    //start heart rate scan
    public void scanHeartRate(){
        detect.scanHeartRate();
    }
    //called by BluetoothHelper when heart rate is ready
    public void heartRate(byte hr){
        if(hr == 0){
            addNode(TTS_REQUEST, "Unable to scan heart rate at the moment");
        }else {
            addNode(TTS_REQUEST, "Heart rate at " + hr + " beats per minute");
        }
        addNode(DET_REQUEST, null);
    }

    //returns the current request
    public int currentRequest() {
        if (queue.size() == 0) {
            return 0;
        } else {
            return queue.get(0).getRequest();
        }
    }

    /*
    called when a request is added
    if its the only request in the queue, execute it
     */
    public void addNode(int request, String message) {
        queue.add(new Node(request, message));
        if (queue.size() == 1) {
            queue.get(0).execute();
        }
    }

    /*
    called from TTS, GoogleSpeechRecognition, or Detection when request finished
    if queue is not empty, execute next request
     */
    public void removeNode() {
        if(queue.size()!=0) {
            queue.remove(0);
        }
        if (!queue.isEmpty()) {
            queue.get(0).execute();
        }
    }

    //stops all processes and flushes the entire queue
    public void flushQueue(){
        queue = new ArrayList<>();
        disableAll();
    }

    //releases queue resources
    public void destroy() {
        if (tts != null) {
            tts.destroy();
        }
        if (googlerec != null) {
            googlerec.destroy();
        }
        if (detect != null) {
            detect.destroyDetection();
        }
    }

    //reference to main for DET, GS, and TTS
    public MainActivity getMain() {
        return main;
    }

    /*
    called by DET, GS, and TTS for errors
    informs main for display and log
     */
    public void error(String message) {
        main.error(message);
    }

    //called by Detection class when detection occurs
    public void detected() {
        detect.disableDetection();
        removeNode();
        addNode(GS_REQUEST, null);
    }

    //disables detection
    public void disableDetect() {
        detect.disableDetection();
        removeNode();
    }

    //disables TTS, Detection, and GS
    public void disableAll(){
        detect.disableDetection();
        googlerec.cancelListening();
        tts.stop();
    }

    /*
    called by GoogleSpeechRecognition class after speech
    informs main to prepare for speech processing by Processor package
     */
    public void processSpeech(String speech) {
        main.processSpeech(speech);
    }

    /*
    changes detection mode of Detection
    can be called by Tasker
     */
    public void changeDetection(int mode) {
        if (mode == -1) {
            if (detect.getDetectionMode() == Detection.VOICE_DETECT) {
                main.watchDetectionOn();
                detect.watchDetect();
            } else if (detect.getDetectionMode() == Detection.WATCH_DETECT) {
                main.voiceDetectionOn();
                detect.voiceDetect();
            }
        } else {
            if (mode == Detection.VOICE_DETECT) {
                main.voiceDetectionOn();
                detect.voiceDetect();
            } else if (mode == Detection.WATCH_DETECT) {
                main.watchDetectionOn();
                detect.watchDetect();
            }
        }
    }
    //return detection mode
    public int getDetectionMode(){
        return detect.getDetectionMode();
    }
    //inform main class of voice detection
    public void voiceDetectionOn(){
        main.voiceDetectionOn();
    }

    //node class holds information about each node of queue
    private class Node {

        private int request;        //tracks type of request
        private String message;     //used to save extra details

        //constructor
        public Node(int request, String message) {
            this.request = request;
            this.message = message;
        }

        //returns type of request
        public int getRequest() {
            return request;
        }

        /*
        executes request with extra details if needed
        if VIOLET is in testing mode:
            no detection requests, no google speech rec requests
            remove node to continue on with queue
         */
        public void execute() {
            if (request == TTS_REQUEST) {
                tts.speak(message);
            } else if (request == GS_REQUEST) {
                if(!main.testMode){
                    googlerec.startListening();
                }else{
                    removeNode();
                }
            } else if (request == DET_REQUEST) {
                if(!main.testMode) {
                    detect.enableDetection();
                }else{
                    removeNode();
                }
            }
        }
    }
}
