package com.example.violet30;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.Queue;

/**
 * Created by howardzhang on 12/24/17.
 */

public class GoogleSpeechRecognition implements RecognitionListener{

    //object variables
    private SpeechRecognizer googlerec;
    private MainActivity main;
    private Intent speechIntent;

    //class variables
    private static final String LANGUAGE_MODEL = RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH;

    public GoogleSpeechRecognition(MainActivity main, Initialization init){
        Log.d("Order", "Starting Google Speech Recognition setup");
        this.main = main;
        googlerec = SpeechRecognizer.createSpeechRecognizer(this.main);
        googlerec.setRecognitionListener(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, LANGUAGE_MODEL);
        init.googleRecSetup();
    }

    public void startListening(){
        Log.d("Order", "Starting Google Search");
        googlerec.startListening(speechIntent);
    }

    //releases google speech intent resources
    public void destroy(){
        if(googlerec != null){
            googlerec.cancel();
            googlerec.destroy();
        }
    }

    //implementation of Recognition Listener
    @Override
    public void onReadyForSpeech(Bundle bundle) {}
    @Override
    public void onBeginningOfSpeech() {}
    @Override
    public void onRmsChanged(float v) {}
    @Override
    public void onBufferReceived(byte[] bytes) {}
    @Override
    public void onEndOfSpeech() {}
    @Override
    public void onError(int e) {
        if(e == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || e == SpeechRecognizer.ERROR_NO_MATCH){
            main.ttsRequest("Unable to process response. Please try again.");
            main.gsRequest();
            main.removeNode();
        }else{
            main.error("GoogleSpeechRecognition Error with error code: " + e);
        }
    }
    @Override
    public void onResults(Bundle results) {
        String speech = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
        main.ttsRequest(speech);
        main.detRequest();
        main.removeNode();
    }
    @Override
    public void onPartialResults(Bundle bundle) {}
    @Override
    public void onEvent(int i, Bundle bundle) {}
}
