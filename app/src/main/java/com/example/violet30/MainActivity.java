package com.example.violet30;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //VARIABLES
    //object variables
    private Initialization init;
    private QueueManager queue;
    private TTS tts;
    private Detection detect;
    private GoogleSpeechRecognition googlerec;

    //permissions
    public static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;

    //logs
    public static final String TAG_ERROR = "ERROR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    //LOADS RESOURCES: constructs needed objects
    public void initialize(){
        Log.d("Order", "Initialize");
        checkPermissions();
        init = new Initialization(this);
    }

    /*ON LAUNCH: called by Initialization class after needed objects are constructed
        creates queue when launching
     */
    public void launch(){
        Log.d("Order","Launching");
        detect = init.getDetection();
        tts = init.getTts();
        googlerec = init.getGooglerec();
        queue = new QueueManager(this);
        ttsRequest("Initialization complete.");
        ttsRequest("VIOLET is online.");
        detRequest();
    }

    //DESTROY
    //unlocks all program resources
    @Override
    protected void onDestroy() {
        if(queue != null){
            queue.destroy();
        }
        super.onDestroy();
    }

    //ERROR
    //displays error message in log, shows error toast
    public void error(String message){
        Log.e(TAG_ERROR,message);
        Toast.makeText(getApplicationContext(),"Error! Check Log for more information.", Toast.LENGTH_SHORT).show();
    }

    //PERMISSIONS
    /*checks permissions:
        RECORD_AUDIO
        ACCESS_FINE_LOCATION
     */
    public void checkPermissions(){
        Log.d("Order", "checking permissions");
        int permissionCheckAudio =
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        int permissionsFineLocation =
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheckAudio != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        if (permissionsFineLocation != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            return;
        }
    }
    //handles permissions: program ends without correct permissions
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSIONS_REQUEST_RECORD_AUDIO:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    finish();
                }
                break;
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkPermissions();
                } else {
                    finish();
                }
        }
    }

    //QUEUE CONTROL
    public void removeNode(){
        queue.removeNode();
    }
    public void ttsRequest(String message){
        queue.addNode(QueueManager.TTS_REQUEST, message);
    }
    public void gsRequest(){
        queue.addNode(QueueManager.GS_REQUEST, null);
    }
    public void detRequest(){
        queue.addNode(QueueManager.DET_REQUEST, null);
    }
    //run when detection occurs
    public void detected(){
        detect.disableDetection();
        gsRequest();
        removeNode();
    }

    //RESOURCE ACCESS METHODS
    public Detection getDetection(){
        return detect;
    }
    public GoogleSpeechRecognition getGooglerec(){
        return googlerec;
    }
    public TTS getTts(){
        return tts;
    }
}
