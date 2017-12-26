package com.example.violet30;

import android.util.Log;

/**
 * Created by howardzhang on 12/22/17.
 */

public class Detection {

    //static variables
    private final static int WATCH_DETECT = 1;
    private final static int VOICE_DETECT = 2;

    //object variables
    private PocketSphinx PSDetect;
    private BluetoothHelper bluetoothHelper;
    private MainActivity main;
    private Initialization init;
    private int detectionMode = WATCH_DETECT;

    public Detection(MainActivity main, Initialization init){
        this.main = main;
        this.init = init;
        PSDetect = new PocketSphinx(this.main, this.init);
        bluetoothHelper = new BluetoothHelper(this.main);
    }

    //starts detection for whichever detection mode is currently selected, default: watch
    public void enableDetection(){
        switch(detectionMode){
            case WATCH_DETECT:
                if(!bluetoothHelper.isConnected()){
                    main.ttsRequest("bluetooth connection not detected, enabling backup voice detection");
                    detectionMode = VOICE_DETECT;
                    PSDetect.startListening();
                }
                bluetoothHelper.enableTouchNotifications();
                break;
            case VOICE_DETECT:
                PSDetect.startListening();
        }
    }

    //access to detectionMode variable
    public int getDetectionMode(){
        return detectionMode;
    }

    //stops detection for both modes
    public void disableDetection(){
        PSDetect.cancel();
        bluetoothHelper.disableTouchNotifications();
    }

    //changes detection mode to voice
    public void voiceDetect(){
        detectionMode = VOICE_DETECT;
    }

    //changes detection mode to watch
    public void watchDetect(){
        detectionMode = WATCH_DETECT;
    }

    //releases detection resources
    public void destroyDetection(){
        if(PSDetect != null) {
            PSDetect.destroy();
        }
        if(bluetoothHelper != null) {
            bluetoothHelper.destroy();
        }
    }
}
