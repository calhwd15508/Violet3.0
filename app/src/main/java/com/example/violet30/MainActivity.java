package com.example.violet30;

import android.Manifest;
import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.Alarm.AlarmHelper;
import com.example.Alarm.AlarmReceiver;
import com.example.Database.Alarm;
import com.example.Database.Firebase;
import com.example.Database.Notification;
import com.example.Database.Task;
import com.example.Database.TaskerMethod;
import com.example.Helper.TaskHelper;
import com.example.IO.Detection;
import com.example.IO.IOManager;
import com.example.Processor.SpeechProcessor;
import com.example.visualizer.VisualizerView;

import java.util.Locale;


/*
Class that manages all other classes
checks permissions
handles initialization of all other other classes
allows communication between other classes
controls flow of information between other classes
unlocks resources upon on shutdown
 */
public class MainActivity extends AppCompatActivity {

    //reference to management class IOManager
    private IOManager managerIO;
    //reference to management class SpeechProcessor
    private SpeechProcessor speechProcessor;
    //reference to Firebase
    private Firebase firebase;
    //reference to AsynchronousInit to help with parallel setup classes
    private AsynchronousInit asynchronousInit;
    //reference to AlarmHelper
    private AlarmHelper alarmHelper;
    //reference to Media Player
    private MediaPlayer alarmMP = null;
    //infoManager to display states
    private InfoManager infoManager;

    //helper references
    private TaskHelper taskHelper;

    //boolean tracking whether or not VIOLET is launched
    public static boolean running = false;
    //boolean tracking whether or not VIOLET is in testing mode
    public boolean testMode = false;
    //boolean tracking whether or not display is connected
    public boolean displayMode = false;

    //UI variables

    //helps silent testing
    //button to process speech that is in text box
    private Button testButton;
    //text box that holds speech to be processed
    private EditText testText;
    //toggle buttons that toggles abcd mode
    private Button toggleTestOn;
    private Button toggleTestOff;
    //title TextView
    private TextView title;
    //info TextView
    private TextView info;
    //VisualizerView that displays audio visualizer
    private VisualizerView visualizer;
    //Linear Layout
    private LinearLayout testingMenu;

    //unique identifiers for permission requests
    public final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    public final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2;

    //logs
    public final String TAG_ERROR = "ERROR";
    public final String TAG_SPEECH = "SPEECH";

    /*
    called when activity is first created
    sets running boolean to true
    checks permissions
    starts initialization of all classes
    sets up UI
    creates receiver to start alarm, received from AlarmReceiver
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        running = true;

        //setup VIOLET
        checkPermissions();
        asynchronousInit = new AsynchronousInit(this);
        //create visualizer to pass into IOManager to be used by VisualizerHelper
        visualizer = (VisualizerView) findViewById(R.id.visualizerView);
        managerIO = new IOManager(this, visualizer);
        speechProcessor = new SpeechProcessor(this);
        alarmHelper = new AlarmHelper((AlarmManager) getSystemService(Context.ALARM_SERVICE), this);
        taskHelper = new TaskHelper(this);

        //firebase last because it uses methods in other classes
        firebase = new Firebase(this);

        setupUI();
    }

    //receiver that stops VIOLET and starts the alarm
    BroadcastReceiver mainAlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            stopAllProcesses();
            startAlarm(intent.getStringExtra("title"));
        }
    };

    //receiver that catches bluetooth connection changes
    BroadcastReceiver mainBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        stopAllProcesses();
                        ttsRequest("Establishing bluetooth connection.");
                        managerIO.bluetoothReconnect = true;
                        managerIO.establishConnection();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        stopAllProcesses();
                        ttsRequest("Lost bluetooth connection." +
                                "Enabling backup voice detection.");
                        changeDetectionMode(Detection.VOICE_DETECT);
                        detRequest();
                        break;
                }
            }
        }
    };

    /*
    ON LAUNCH: called by Initialization class after needed objects are constructed
        creates queue when launching
    alarm -> starts alarm (AlarmRecevier for more information)
    no alarm -> creates detRequest to start detection
     */
    public void launch() {

        //registers alarm receiver (AlarmReceiver for more information)
        registerReceiver(mainAlarmReceiver, new IntentFilter("ALARM"));
        //registers bluetooth receiver
        IntentFilter bluetoothIntentFilter =
                new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mainBluetoothReceiver, bluetoothIntentFilter);

        title.setVisibility(View.VISIBLE);
        info.setVisibility(View.VISIBLE);
        toggleTestOn.setVisibility(View.VISIBLE);
        Intent i = getIntent();
        if (!i.getBooleanExtra("alarm", false)) {
            if (i.getBooleanExtra("startup", false)) {
                alarmHelper.loadAlarms();
            }
            ttsRequest("Launched");
            detRequest();
        } else {
            startAlarm(i.getStringExtra("title"));
        }
    }

    /*
    Stops all currently running processes
    flushes the IOManager queue
    "stops" a conversation by removing previous info
     */
    public void stopAllProcesses() {
        managerIO.flushQueue();
        speechProcessor.stopProcessing();
    }

    //PAUSE
    //saves any resources required since onDestroy is not guaranteed
    @Override
    protected void onPause() {
        if (alarmHelper != null) {
            alarmHelper.save();
        }
        if (taskHelper != null) {
            taskHelper.save();
        }
        super.onPause();
    }

    //DESTROY
    //unlocks all program resources
    @Override
    protected void onDestroy() {
        running = false;
        if (managerIO != null) {
            managerIO.destroy();
        }
        if (firebase != null) {
            firebase.destroy();
        }
        unregisterReceiver(mainAlarmReceiver);
        unregisterReceiver(mainBluetoothReceiver);
        //stop running alarms
        if (alarmMP != null) {
            if (alarmMP.isPlaying()) {
                alarmMP.stop();
            }
            alarmMP.release();
            alarmMP = null;
        }

        super.onDestroy();
    }

    //ERROR
    //displays error message in log, shows error toast
    public void error(String message) {
        Log.e(TAG_ERROR, message);
        Toast.makeText(getApplicationContext(), "Error! Check Log for more information.", Toast.LENGTH_SHORT).show();
    }

    //PERMISSIONS
    /*checks dangerous permissions:
        RECORD_AUDIO
        ACCESS_FINE_LOCATION
     */
    private void checkPermissions() {
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

    //method to setup UI
    private void setupUI(){
        //style load in font
        AssetManager am = getApplicationContext().getAssets();
        Typeface typeface = Typeface.createFromAsset(am,
                String.format(Locale.US, "fonts/%s", "Stark.otf"));
        //setup UI: used to modularly test functions without speech
        title = (TextView) findViewById(R.id.title);
        title.setTypeface(typeface);
        Shader textShader = new LinearGradient(0, 0, 0, 20,
                new int[]{Color.rgb(132,206,211),Color.rgb(171,199,209) },
                new float[]{0,1}, Shader.TileMode.CLAMP);
        title.getPaint().setShader(textShader);

        info = (TextView) findViewById(R.id.info);
        info.setTypeface(typeface);
        //setting up infoManager to update info textView
        infoManager = new InfoManager(info);
        if (managerIO.getDetectionMode()==1){
            infoManager.setDetection("Watch");
        }else{
            infoManager.setDetection("Voice");
        }
        if(displayMode){
            infoManager.setDisplay("On");
        }else{
            infoManager.setDisplay("Off");
        }


        testingMenu = (LinearLayout) findViewById(R.id.testingMenu);

        testButton = (Button) findViewById(R.id.testButton);
        testButton.setTypeface(typeface);
        testText = (EditText) findViewById(R.id.testText);
        testText.setTypeface(typeface);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!testMode) {
                    managerIO.disableDetect();
                    testText.setText("");
                    ttsRequest("Testing mode is not enabled." +
                            "Please enable testing mode to use this feature.");
                    detRequest();
                } else {
                    String text = testText.getText().toString();
                    testText.setText("");
                    processSpeech(text);
                }

            }
        });
        toggleTestOff = (Button) findViewById(R.id.testToggleOff);
        toggleTestOn = (Button) findViewById(R.id.testToggleOn);
        toggleTestOff.setTypeface(typeface);
        toggleTestOn.setTypeface(typeface);
        toggleTestOff.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                testingMenu.setVisibility(View.INVISIBLE);
                toggleTestOn.setVisibility(View.VISIBLE);
                testMode = false;
                infoManager.setTesting("Off");
                stopAllProcesses();
                ttsRequest("Testing mode disabled.");
                detRequest();
            }
        });
        toggleTestOn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                toggleTestOn.setVisibility(View.INVISIBLE);
                testingMenu.setVisibility(View.VISIBLE);
                testMode = true;
                infoManager.setTesting("On");
                stopAllProcesses();
                ttsRequest("Testing mode enabled.");
            }
        });
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

    //IOMANAGER CONTROL
    public void removeNode() {
        managerIO.removeNode();
    }
    public void ttsRequest(String message) {
        managerIO.addNode(IOManager.TTS_REQUEST, message);
    }
    public void gsRequest() {
        managerIO.addNode(IOManager.GS_REQUEST, null);
    }
    public void detRequest() {
        managerIO.addNode(IOManager.DET_REQUEST, null);
    }
    public void changeDetectionMode(int mode) {
        if(infoManager!=null) {
            if (mode == -1) {
                if (managerIO.getDetectionMode() == 1) {
                    infoManager.setDetection("Voice");
                } else {
                    infoManager.setDetection("Watch");
                }
            } else {
                if (mode == 1) {
                    infoManager.setDetection("Watch");
                } else if (mode == 2) {
                    infoManager.setDetection("Voice");
                }
            }
        }
        managerIO.changeDetection(mode);
    }
    public void watchDetectionOn(){
        if(infoManager!=null){
            infoManager.setDetection("Watch");
        }
    }
    public void voiceDetectionOn(){
        if(infoManager!=null) {
            infoManager.setDetection("Voice");
        }
    }
    public void scanHeartRate() {
        managerIO.scanHeartRate();
    }

    //SPEECHPROCESSOR CONTROL
    public void clearTaskerMethods() {
        speechProcessor.clearTaskerMethods();
    }
    public void addTaskerMethod(TaskerMethod taskerMethod) {
        speechProcessor.addTaskerMethod(taskerMethod);
    }
    public void processSpeech(String speech) {
        Log.d(TAG_SPEECH, speech);
        speechProcessor.processSpeech(speech);
    }

    //FIREBASE CONTROL
    public void FBaddAlarm(Alarm alarm) {
        firebase.addAlarm(alarm);
    }
    public void FBremoveAlarm(Alarm alarm) {
        firebase.removeAlarm(alarm);
    }
    public void FBaddTask(Task task){
        firebase.addTask(task);
    }
    public void FBremoveTask(Task task){
        firebase.removeTask(task);
    }
    //called by Firebase
    public void displayMode(boolean mode) {
        displayMode = mode;
        if(infoManager!=null) {
            if (mode) {
                infoManager.setDisplay("On");
            } else {
                infoManager.setDisplay("Off");
            }
        }
    }

    //ALARMHELPER CONTROL
    public void addAlarm(Alarm alarm) {
        alarmHelper.addAlarm(alarm);
    }
    public void removeAlarm(Alarm alarm) {
        alarmHelper.removeAlarm(alarm);
    }
    public int getCurrentIdAlarm() {
        return alarmHelper.retrieveId();
    }
    public Alarm getAlarm(String title) {
        return alarmHelper.getAlarm(title);
    }
    public boolean alarmIsTaken(String title) {
        return alarmHelper.isTaken(title);
    }
    public void startAlarm(String title) {
        Alarm alarm = alarmHelper.getAlarm(title);
        firebase.removeAlarm(alarm);
        managerIO.startWatchVibrate();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("VIOLET Alarm")
                .setContentText(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(0, builder.build());

        Notification notification = new Notification();
        notification.setTitle("VIOLET Alarm");
        notification.setText(title);
        firebase.addNotification(notification);

        /*Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        alarmMP = MediaPlayer.create(this, alarmSound);
        if(!alarmMP.isPlaying()){
            alarmMP.start();
        }*/
        detRequest();
    }
    public void stopAlarm() {
        managerIO.stopWatchVibrate();
        if (alarmMP != null) {
            if (alarmMP.isPlaying()) {
                alarmMP.stop();
            }
            alarmMP.release();
            alarmMP = null;
        }
    }

    //TASKHELPER CONTROL
    public void addTask(Task task){
        taskHelper.addTask(task);
    }
    public void removeTask(Task task){
        taskHelper.removeTask(task);
    }
    public boolean taskIsTaken(String title) {
        return taskHelper.isTaken(title);
    }
    public int getCurrentIdTask(){
        return taskHelper.retrieveId();
    }
    public Task getTask(String title){
        return taskHelper.getTask(title);
    }

    //ASYNCHRONOUSINIT CONTROL
    public void psSetup() {
        asynchronousInit.psSetup();
    }
    public void ttsSetup() {
        asynchronousInit.ttsSetup();
    }
}
