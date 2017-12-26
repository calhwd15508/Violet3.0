package com.example.violet30;

import android.annotation.TargetApi;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by howardzhang on 12/22/17.
 */

public class TTS implements TextToSpeech.OnInitListener{

    //object variables
    private MainActivity main;
    private Initialization init;
    private TextToSpeech tts;

    //class variables
    private static Locale locale = Locale.UK;

    public TTS(MainActivity main, Initialization init){
        Log.d("Order", "Text to Speech initialization start");
        this.main = main;
        this.init = init;
        tts = new TextToSpeech(main, this);
    }

    //run on initialization
    @Override
    public void onInit(int status) {
        Log.d("Order", "Text to Speech initialized");
        if(status == TextToSpeech.SUCCESS){
            int result = tts.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                main.error("Language for Text to Speech not supported!");
            }else{
                tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String s) {}
                    @Override
                    public void onDone(String s) {
                        main.removeNode();
                    }
                    @Override
                    public void onError(String s) {
                        main.error("TTS Error: " + s);
                    }
                });
                init.ttsSetup();
            }
        }else{
            main.error("Error initializing Text to Speech!");
        }
    }

    //adds to speech queue
    public void speak(String message){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ttsSpeak(message);
        }else {
            ttsSpeakDeprecated(message);
        }
    }

    //speak version 21 or above
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void ttsSpeak(String message){

        tts.speak(message,TextToSpeech.QUEUE_FLUSH, null, "normal");
    }

    //speak version 20 or below
    @SuppressWarnings("deprecation")
    private void ttsSpeakDeprecated(String message){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,"normal");
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, map);
    }

    //releases TextToSpeech resources
    public void destroy(){
        if(tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
