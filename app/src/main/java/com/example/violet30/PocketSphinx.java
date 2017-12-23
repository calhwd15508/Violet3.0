package com.example.violet30;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

/**
 * Created by howardzhang on 11/2/17.
 */

public class PocketSphinx implements RecognitionListener{

    //class variables
    //KeySearch name
    public static final String KWS = "wakeup";
    //KeySearch phrase
    public static final String KEYPHRASE = "ok violet";

    //object variables
    private SpeechRecognizer sphinxrec;
    private MainActivity main;

    public PocketSphinx(MainActivity main){
        this.main = main;
        runSphinxSetup();
    }

    /* POCKETSPHINX SETUP METHOD:
     *  Starts an asynchronous method
     *  Uses PocketSphinx's implementation of Sphinx's RecognitionListener
     *  Starts a keyword search for wakeup call
     *  Resources:
     *      Default Acoustic Model
     *      Default English Dictionary Model*/

    private void runSphinxSetup(){
        Log.d("Order", "Running sphinx setup");
        //Sets up PocketSphinx
        new AsyncTask<Void, Void, Exception>(){
            @Override
            protected Exception doInBackground(Void... params) {
                try{
                    //Brings in PocketSphinx resources
                    Assets assets = new Assets(main);
                    File assetDir = assets.syncAssets();
                    SpeechRecognizerSetup sphinxSetup = SpeechRecognizerSetup.defaultSetup();
                    sphinxSetup.setAcousticModel(new File(assetDir, "en-us-ptm"));
                    sphinxSetup.setDictionary(new File(assetDir, "cmudict-en-us.dict"));

                    // Threshold to tune for keyphrase to balance between false alarms and misses
                    // Lower for lower false positive count
                    sphinxSetup.setKeywordThreshold(1e-20f);
                    sphinxrec = sphinxSetup.getRecognizer();
                    sphinxrec.addKeyphraseSearch(KWS,KEYPHRASE);
                    sphinxrec.addListener(PocketSphinx.this);

                }catch(IOException e){
                    return e;
                }
                return null;
            }
            @Override
            //Starts Keyword Search or displays error
            protected void onPostExecute(Exception result) {
                Log.d("Order", "Finishing sphinx setup");
                if (result != null) {
                    main.error("PocketSphinx Setup Error: " + result.getMessage());
                }
                main.launch();
            }
        }.execute();
    }

    //Implementation of RecognitionListener
    //Checks for keyword, runs main.detected() if detected
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if(hypothesis == null){
            return;
        }
        if(hypothesis.getHypstr().equals(KEYPHRASE)){
            main.detected();
        }
    }
    //handles detection errors
    @Override
    public void onError(Exception e) {
        main.error("PocketSphinx Detection Error: " + e.getMessage());
    }
    //unused implementation methods
    @Override
    public void onBeginningOfSpeech() {
    }
    @Override
    public void onEndOfSpeech() {
    }
    @Override
    public void onResult(Hypothesis hypothesis) {
    }
    @Override
    public void onTimeout() {
    }


    //Detection Control Functions
    //restarts the keyword search
    public void restartKeySearch(){
        sphinxrec.cancel();
        sphinxrec.startListening(KWS);
    }
    //starts keyword search
    public void startListening(){
        sphinxrec.startListening(KWS);
    }
    //cancels keyword search
    public void cancel(){
        sphinxrec.cancel();
    }
    //unlocks resources
    public void destroy(){
        if (sphinxrec != null) {
            sphinxrec.cancel();
            sphinxrec.shutdown();
        }
    }
}