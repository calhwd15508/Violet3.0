package com.example.violet30;

import android.annotation.TargetApi;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

/**
 * Created by howardzhang on 12/22/17.
 */

public class TTS implements TextToSpeech.OnInitListener {

    //object variables
    private MainActivity main;
    private TextToSpeech tts;

    //class variables
    public static Locale locale = Locale.UK;

    public TTS(MainActivity main){
        Log.d("Order", "Text to Speech initialization start");
        this.main = main;
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
            }
            main.initialize();
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
        tts.speak(message,TextToSpeech.QUEUE_ADD, null, null);
    }

    //speak version 20 or below
    @SuppressWarnings("deprecation")
    private void ttsSpeakDeprecated(String message){
        tts.speak(message, TextToSpeech.QUEUE_ADD, null);
    }

    //releases TextToSpeech resources
    public void destroy(){
        tts.stop();
        tts.shutdown();
    }
}
