package com.example.IO;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import com.example.visualizer.VisualizerView;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by howardzhang on 12/22/17.
 */

/*
Text to Speech handling class
works with IOManager to schedule
 */
public class TTS implements TextToSpeech.OnInitListener {

    //reference to IOManager
    private IOManager managerIO;
    //reference to TextToSpeech object
    private TextToSpeech tts;
    //handler object to execute on main thread
    private Handler handler;

    //speech locale variable
    private Locale locale = Locale.UK;

    //reference to helper class VisualizerHelper
    private VisualizerHelper visHelper;

    //name of file where wavs are stored for visualizer
    private String filename;

    //constructor
    public TTS(IOManager managerIO, VisualizerView visualizerView) {
        //setup
        this.managerIO = managerIO;
        handler = new Handler(Looper.getMainLooper());
        filename = managerIO.getMain().getFilesDir() + "/tts.wav";
        visHelper = new VisualizerHelper(visualizerView, managerIO, filename);
        //tts setup requires OnInitListener implementation
        tts = new TextToSpeech(managerIO.getMain(), this);
    }

    /*OnInitListener Implementation
    Asynchronous setup, inform Asynchronous Init when setup complete
    Success -> informs AsynchronousInit
    Language Error -> informs IOManager
    Error -> informs IOManager
     */
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                managerIO.error("Language for Text to Speech not supported!");
            } else {
                /*
                tts onUtteranceProgressListener:
                informs IOManager that speech has completed for scheduling
                 */
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {
                    }

                    @Override
                    public void onDone(String s) {
                        visHelper.speak();
                    }

                    @Override
                    public void onError(final String s) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                managerIO.error("TTS Error: " + s);
                            }
                        });
                    }
                });
                managerIO.getMain().ttsSetup();
            }
        } else {
            managerIO.error("Error initializing Text to Speech!");
        }
    }

    //adds speech to queue
    public void speak(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ttsSpeak(message);
        } else {
            ttsSpeakDeprecated(message);
        }
    }

    //speak version 21 or above
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsSpeak(String message) {
        tts.synthesizeToFile(message, null, new File(filename), "normal");
    }

    //speak version 20 or below
    @SuppressWarnings("deprecation")
    private void ttsSpeakDeprecated(String message) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "normal");
        tts.synthesizeToFile(message, map, filename);
    }

    //stops tts output
    public void stop(){
        tts.stop();
        visHelper.stop();
    }

    //releases TextToSpeech resources
    public void destroy() {
        if (tts != null) {
            visHelper.destroy();
            tts.stop();
            tts.shutdown();
        }
    }
}
