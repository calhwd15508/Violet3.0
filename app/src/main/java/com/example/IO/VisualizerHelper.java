package com.example.IO;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.example.visualizer.VisualizerView;
import com.example.visualizer.renderer.BarGraphRenderer;
import com.example.visualizer.renderer.CircleBarRenderer;
import com.example.visualizer.renderer.CircleRenderer;
import com.example.visualizer.renderer.LineRenderer;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

/**
 * Created by howardzhang on 7/20/18.
 */

/*
Helper class to manage visualizer view component
 */
public class VisualizerHelper {

    //reference to the View component
    private VisualizerView visualizer;
    //reference to the media player
    private MediaPlayer mediaPlayer;
    //reference to the managing class
    private IOManager managerIO;
    //reference to the file location where the wav is stored
    private String filename;

    //constructor
    public VisualizerHelper(VisualizerView visualizer,  IOManager managerIO, String filename){
        //setup
        this.visualizer = visualizer;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        this.managerIO = managerIO;
        this.filename = filename;
        //adds the renderer to the screen
        addBarGraphRenderers();
        addCircleRenderer();
    }

    /*
    outputs speech and displays change in visualizer
    called by TTS after speech synthesis is finished
    starts media player with file
    links with visualizer
     */
    public void speak(){
        try {
            File file = new File(filename);
            FileInputStream inputStream = new FileInputStream(file);
            FileDescriptor descriptor = inputStream.getFD();
            mediaPlayer.reset();
            mediaPlayer.setDataSource(descriptor);
            inputStream.close();
            mediaPlayer.prepare();
            mediaPlayer.start();
            visualizer.link(mediaPlayer, this);
        }catch(java.io.IOException e){
            managerIO.error("TTS IOException");
        }
    }

    /*
    called by VisualizerView upon mediaPlayer completion
    returns control to managerIO
     */
    public void onComplete(){
        managerIO.removeNode();
    }

    /*
    stops mediaPlayer
     */
    public void stop(){
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    /*
    called by main when activity is being destroyed
    properly releases media player
     */
    public void destroy(){
        visualizer.release();
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // Methods for adding renderers to visualizer
    private void addBarGraphRenderers()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(50f);
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(200, 56, 138, 252));
        BarGraphRenderer barGraphRendererBottom = new BarGraphRenderer(16, paint, false);
        visualizer.addRenderer(barGraphRendererBottom);

        Paint paint2 = new Paint();
        paint2.setStrokeWidth(12f);
        paint2.setAntiAlias(true);
        paint2.setColor(Color.argb(200, 181, 111, 233));
        BarGraphRenderer barGraphRendererTop = new BarGraphRenderer(4, paint2, true);
        visualizer.addRenderer(barGraphRendererTop);
    }

    private void addCircleBarRenderer()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(8f);
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.LIGHTEN));
        paint.setColor(Color.argb(255, 222, 92, 143));
        CircleBarRenderer circleBarRenderer = new CircleBarRenderer(paint, 32, true);
        visualizer.addRenderer(circleBarRenderer);
    }

    private void addCircleRenderer()
    {
        Paint paint = new Paint();
        paint.setStrokeWidth(5f);
        paint.setAntiAlias(true);
        paint.setColor(Color.argb(255, 132, 206, 211));
        CircleRenderer circleRenderer = new CircleRenderer(paint, true);
        visualizer.addRenderer(circleRenderer);
    }

    private void addLineRenderer()
    {
        Paint linePaint = new Paint();
        linePaint.setStrokeWidth(1f);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.argb(88, 0, 128, 255));

        Paint lineFlashPaint = new Paint();
        lineFlashPaint.setStrokeWidth(5f);
        lineFlashPaint.setAntiAlias(true);
        lineFlashPaint.setColor(Color.argb(188, 255, 255, 255));
        LineRenderer lineRenderer = new LineRenderer(linePaint, lineFlashPaint, true);
        visualizer.addRenderer(lineRenderer);
    }

}
