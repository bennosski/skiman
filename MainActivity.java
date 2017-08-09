package com.example.ben.skiman3;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Random;

import android.os.Handler;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.StrictMath.sqrt;



public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "myfirstapp.MESSAGE";
    public static boolean running_simulation = false;
    public DrawingView myDrawingView;
    public Skier mySkier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TextView textView = (TextView) findViewById(R.id.text_box1);
        myDrawingView = (DrawingView) findViewById(R.id.drawing);
    }

    public void undo(View view) {
        myDrawingView.undo();
    }

    public double[] scaleXCoordinates(float[] xs){
        double[] out = new double[myDrawingView.N];
        for(int i=0; i<myDrawingView.N; i++){
            out[i] = (myDrawingView.xs_user[i]-myDrawingView.xmin)/(myDrawingView.xmax-myDrawingView.xmin);
        }
        return out;
    }

    public double[] scaleYCoordinates(float[] ys){
        double[] out = new double[myDrawingView.N];
        for(int i=0; i<myDrawingView.N; i++){
            out[i] = -(myDrawingView.ys_user[i]-myDrawingView.ymin)/(myDrawingView.ymax-myDrawingView.ymin);
        }
        return out;
    }

    public void startTV(){
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TextView tv = (TextView) findViewById(R.id.textBox1);
                tv.setText(mySkier.messages.get(myDrawingView.myi));
                startTV();
            }
        }, 100);
    }

    public void startMeasurement(View view) {

        final Button button = (Button) findViewById(R.id.start_button);
        TextView tv = (TextView) findViewById(R.id.textBox1);

        double[] xs_scaled = scaleXCoordinates(myDrawingView.xs_user);
        double[] ys_scaled = scaleYCoordinates(myDrawingView.ys_user);

        //interpolate any large jumps
        //do this some other time

        mySkier = new Skier(xs_scaled, ys_scaled, myDrawingView.N);

        mySkier.runSimulation();
        tv.setText(Double.toString(mySkier.TotalTime));
        myDrawingView.makeAnimation(mySkier);


        //String mystr = "";
        //for(int i=0; i<10; i++)
        //    mystr += Double.toString(mySkier.ys_user[i])+" ";
        //tv.setText(mystr);
        //
        //tv.setText(" time "+Double.toString(mySkier.TotalTime)
        //        +" sim len "+Integer.toString(mySkier.xs_sim.size()));

        //tv.setText("TT "+String.format("%1.3f",mySkier.TotalTime)+" "+mySkier.debug);
        //startTV();

        //TextView textView = (TextView) findViewById(R.id.text_box1);
        //TextView textView2 = (TextView) findViewById(R.id.text_box2);
        //textView2.setText(Integer.toString(myDrawingView.width));

        /*
        if(!running_simulation) {

            textView.setText("Hello");

            Handler handler = new Handler(Looper.getMainLooper());
            final Runnable r = new Runnable(){
                public void run(){
                    //button.setText("Measure Vertical");
                    button.setEnabled(true);
                    button.performClick();
                }
            };
            handler.postDelayed(r, 1000);

            button.setText("Running");
            button.setEnabled(false);
        }
        else {
            button.setText("Start");

            // this works for setting the textview with data from my view class
            // after the start button is pressed
            int a = myDrawingView.data;
            textView.setText(Integer.toString(a));
        }
        running_simulation = !running_simulation;
        */

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
