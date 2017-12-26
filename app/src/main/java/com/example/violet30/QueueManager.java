package com.example.violet30;

import android.util.Log;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by howardzhang on 12/24/17.
 */

//class used to manage between text to speech and google recognition
public class QueueManager {

    //class variables
    public static final int TTS_REQUEST = 1;        //Text to Speech request
    public static final int GS_REQUEST = 2;         //Google Speech Recognition request
    public static final int DET_REQUEST = 3;        //Detection request

    //object variables
    private TTS tts;
    private GoogleSpeechRecognition googlerec;
    private Detection detect;
    private ArrayList<Node> queue;      //arrayList to represent queue

    public QueueManager(MainActivity main){     //construction of new queue
        this.queue = new ArrayList<Node>();
        tts = main.getTts();
        googlerec = main.getGooglerec();
        detect = main.getDetection();
    }

    //called whenever a tts or gs request is needed
    public void addNode(int request, String message){
        Log.d("Debug", "Added node with request: " + request + " and message: " + message);
        queue.add(new Node(request, message));
        if(queue.size()==1){
            queue.get(0).execute();
        }
    }

    //called from TTS and GoogleSpeechRecognition when request finished
    public void removeNode(){
        Log.d("Debug", "removed node");
        queue.remove(0);
        if(!queue.isEmpty()){
            queue.get(0).execute();
        }
    }

    //releases queue resources
    public void destroy(){
        if(tts != null){
            tts.destroy();
        }
        if(googlerec != null){
            googlerec.destroy();
        }
        if(detect != null){
            detect.destroyDetection();
        }
    }

    //node class holds information about each node of queue
    private class Node{

        //object variables
        private int request;        //tracks type of request
        private String message;     //used to save message for tts

        public Node(int request, String message){
            this.request = request;
            if(this.request == TTS_REQUEST){
                this.message = message;
            }else{
                this.message = null;
            }
        }

        public void execute(){
            Log.d("Debug", "Executing node with request: " + request + " and message: " + message);
            if(request == TTS_REQUEST){
                tts.speak(message);
            }else if(request == GS_REQUEST){
                googlerec.startListening();
            }else if(request == DET_REQUEST){
                detect.enableDetection();
            }
        }
    }
}
