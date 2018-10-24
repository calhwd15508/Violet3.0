package com.example.IO;

/**
 * Created by howardzhang on 12/22/17.
 */

/*
Class that handles and manages detection methods
First phase, Detection -> Second phase, Speech Recognition by GoogleSpeechRecognition
Works with IOManager to schedule
Watch detection handled by BluetoothHelper
Voice detection handled by PocketSphinx
 */
public class Detection {

    //unique identifiers for detection methods
    public static final int WATCH_DETECT = 1;
    public static final int VOICE_DETECT = 2;

    //reference to voice detection object
    private PocketSphinx PSDetect;
    //reference to watch detection object
    private BluetoothHelper bluetoothHelper;

    //keeps track of detection mode currently enabled
    private int detectionMode = WATCH_DETECT;

    //reference to IOManager
    private IOManager managerIO;

    //constructor
    public Detection(IOManager managerIO) {
        //setup
        this.managerIO = managerIO;
        PSDetect = new PocketSphinx(managerIO);
        bluetoothHelper = new BluetoothHelper(managerIO);
    }

    //attempt to connect to bluetooth
    public void establishConnection(){
        bluetoothHelper.establishConnection();
    }

    //watch function handlers
    public void startWatchVibrate(){
        if(bluetoothHelper.isConnected()){
            bluetoothHelper.vibrateStart();
        }
    }
    public void stopWatchVibrate(){
        if(bluetoothHelper.isConnected()){
            bluetoothHelper.vibrateStop();
        }
    }
    public void scanHeartRate(){
        bluetoothHelper.startHRScan();
    }

    //starts detection for whichever detection mode is currently selected, default: watch
    public void enableDetection() {
        switch (detectionMode) {
            case WATCH_DETECT:
                if (!bluetoothHelper.isConnected()) {
                    managerIO.removeNode();
                    managerIO.addNode(IOManager.TTS_REQUEST,
                            "bluetooth connection not detected, enabling backup voice detection");
                    detectionMode = VOICE_DETECT;
                    managerIO.voiceDetectionOn();
                    managerIO.addNode(IOManager.DET_REQUEST, null);
                }else{
                    bluetoothHelper.enableTouchNotifications();
                    break;
                }
            case VOICE_DETECT:
                PSDetect.startListening();
        }
    }

    //access to detectionMode variable
    public int getDetectionMode() {
        return detectionMode;
    }

    //stops detection for both modes
    public void disableDetection() {
        PSDetect.cancel();
        bluetoothHelper.disableTouchNotifications();
    }

    //changes detection mode to voice
    public void voiceDetect() {
        managerIO.addNode(IOManager.TTS_REQUEST, "Voice detection on.");
        detectionMode = VOICE_DETECT;
    }

    //changes detection mode to watch
    public void watchDetect() {
        managerIO.addNode(IOManager.TTS_REQUEST, "Watch detection on.");
        detectionMode = WATCH_DETECT;
    }

    //releases detection resources
    public void destroyDetection() {
        if (PSDetect != null) {
            PSDetect.destroy();
        }
        if (bluetoothHelper != null) {
            if(bluetoothHelper.isConnected()){
                bluetoothHelper.vibrateStop();
            }
            bluetoothHelper.destroy();
        }
    }
}
