package com.example.IO;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

/**
 * Created by howardzhang on 12/24/17.
 */

/*
Class that handles speech recognition
Second Phase, GoogleSpeechRecognition -> Third Phase, SpeechProcessor
handled by google's speech recognition api
Works with IOManager to schedule
 */
public class GoogleSpeechRecognition implements RecognitionListener {

    //reference to google api SpeechRecognizer class
    private SpeechRecognizer googlerec;
    //intent object used for speech recognition
    private Intent speechIntent;
    //reference to IOManager
    private IOManager managerIO;
    //handler object to execute on main thread
    private Handler handler;

    //type of speech recognition language model
    private static final String LANGUAGE_MODEL = RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH;

    //constructor
    public GoogleSpeechRecognition(IOManager managerIO) {
        //setup
        this.managerIO = managerIO;
        handler = new Handler(Looper.getMainLooper());
        //setup google speech recognition api
        googlerec = SpeechRecognizer.createSpeechRecognizer(managerIO.getMain());
        googlerec.setRecognitionListener(this);
        speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, LANGUAGE_MODEL);
    }

    //start up speech recognition
    public void startListening() {
        googlerec.startListening(speechIntent);
    }
    //cancels speech recognition -> no processing of input generated so far
    public void cancelListening(){
        googlerec.cancel();
    }

    //releases google speech intent resources
    public void destroy() {
        if (googlerec != null) {
            googlerec.cancel();
            googlerec.destroy();
        }
    }

    //implementation of Recognition Listener
    @Override
    public void onReadyForSpeech(Bundle bundle) {
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float v) {
    }

    @Override
    public void onBufferReceived(byte[] bytes) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    /*
    Error handling:
    timeout, no match -> back to detection phase
    other -> inform IOManager
     */
    @Override
    public void onError(final int e) {
        if (e == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || e == SpeechRecognizer.ERROR_NO_MATCH) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    managerIO.removeNode();
                    managerIO.addNode(IOManager.DET_REQUEST, null);
                }
            });
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    managerIO.error("GoogleSpeechRecognition Error with error code: " + e);
                }
            });
        }
    }

    /*
    Result handling:
    on to next phase -> SpeechProcessor
     */
    @Override
    public void onResults(Bundle results) {
        final String speech = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION).get(0);
        handler.post(new Runnable() {
            @Override
            public void run() {
                managerIO.removeNode();
                managerIO.processSpeech(speech);
            }
        });
    }

    @Override
    public void onPartialResults(Bundle bundle) {
    }

    @Override
    public void onEvent(int i, Bundle bundle) {
    }
}
