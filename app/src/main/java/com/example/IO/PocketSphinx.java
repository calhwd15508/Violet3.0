package com.example.IO;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

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

/*
Handles voice detection with PocketSphinx api
works with detection for voice keyword detection
 */
public class PocketSphinx implements RecognitionListener {

    /*
    threshold
    larger -> harder to detect, use for smaller keywords
    smaller -> easier to detect, use for larger keywords
    max: 1
    min: 1e-50
     */
    private static final float KEY_THRESHOLD = 1e-5f;       //KeySearch threshold
    //keyword key
    private static final String KWS = "wakeup";              //KeySearch name
    //keyword value used for voice detection
    private static final String KEYPHRASE = "violet";     //KeySearch phrase

    //reference to PocketSphinx api SpeechRecognizer object
    private SpeechRecognizer sphinxrec;
    //reference to IOManager
    private IOManager managerIO;
    //reference to Asynchronous TaskData to setup pocket sphinx
    private AsyncTask sphinxSetup;
    //handler object to execute on main thread
    private Handler handler;

    //constructor
    public PocketSphinx(IOManager managerIO) {
        //setup
        this.managerIO = managerIO;
        handler = new Handler(Looper.getMainLooper());
        //start asynchronous setup
        runSphinxSetup();
    }

    /* POCKETSPHINX SETUP METHOD:
     *  Starts an asynchronous method
     *  Uses PocketSphinx's implementation of Sphinx's RecognitionListener
     *  Starts a keyword search for wakeup call
     *  Resources:
     *      Default Acoustic Model
     *      Default English Dictionary Model*/

    private void runSphinxSetup() {
        /*Sets up PocketSphinx
        Asynchronous -> parallel setup handled by AsynchronousInit
         */
        sphinxSetup = new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    //Brings in PocketSphinx resources
                    Assets assets = new Assets(managerIO.getMain());
                    File assetDir = assets.syncAssets();
                    SpeechRecognizerSetup sphinxSetup = SpeechRecognizerSetup.defaultSetup();
                    sphinxSetup.setAcousticModel(new File(assetDir, "en-us-ptm"));
                    sphinxSetup.setDictionary(new File(assetDir, "cmudict-en-us.dict"));

                    // Threshold to tune for keyphrase to balance between false alarms and misses
                    // Lower for lower false positive count
                    sphinxSetup.setKeywordThreshold(KEY_THRESHOLD);
                    sphinxrec = sphinxSetup.getRecognizer();
                    sphinxrec.addKeyphraseSearch(KWS, KEYPHRASE);
                    sphinxrec.addListener(PocketSphinx.this);

                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            /*executes after setup
            successful -> informs AsynchronousInit
            error -> informs IOManager
             */
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    managerIO.error("PocketSphinx Setup Error: " + result.getMessage());
                }
                managerIO.getMain().psSetup();
            }
        }.execute();
    }

    //Implementation of RecognitionListener
    //Checks for keyword, runs main.detected() if detected
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }
        if (hypothesis.getHypstr().equals(KEYPHRASE)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    managerIO.detected();
                }
            });
        }
    }

    //handles detection errors, informs IOManager
    @Override
    public void onError(final Exception e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                managerIO.error("PocketSphinx Detection Error: " + e.getMessage());
            }
        });
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
    public void restartKeySearch() {
        sphinxrec.cancel();
        sphinxrec.startListening(KWS);
    }

    //starts keyword search
    public void startListening() {
        sphinxrec.startListening(KWS);
    }

    //cancels keyword search
    public void cancel() {
        sphinxrec.cancel();
    }

    //unlocks resources
    public void destroy() {
        if (sphinxrec != null) {
            if(sphinxSetup != null){
                sphinxSetup.cancel(true);
            }
            sphinxrec.cancel();
            sphinxrec.removeListener(this);
            sphinxrec.shutdown();
        }
    }
}