package qut.cusumalgorithm;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;
import static java.lang.StrictMath.max;
import static qut.cusumalgorithm.R.id.seekBar2;
import static qut.cusumalgorithm.R.id.seekBarMeanPitchAfterChange;
import static qut.cusumalgorithm.R.id.seekBarThreshold22;


public class MainActivity extends AppCompatActivity {
    private SeekBar seekBar;
    private SeekBar seekBarThreshold;
    private SeekBar seekBarMeanPitch;
    private TextView textView;
    private TextView textViewThreshold;
    private TextView nbSamples;
    private TextView meanPitchs;
    private TextView stdPitchs;
    private TextView cusumsPos;
    private TextView textViewMeanPitchAfterChange;
    private TextView changeDetected;
    private final double multiplier= (float) 0.05;
    private final double multiplierThreshold= (float) 0.5;
    private final double multiplierMeanAfterChange= (float) 0.05;

    private Timer noiseTimer;
    private double pitch;
    private double meanPitch;
    private double sPitch;
    private double stdPitch;
    private int nbSample;
    private double cusumPos;
    private double cusumNeg;
    private double meanPitchExpectedAfterChange;
    private double threshold;
    private int numberOfChange=0;
    private ToggleButton toggle;
    private double progressThreshold;
    private double progress;
    private double progressMeanAfterChange;

    // Graph view attributes
    private LineGraphSeries<DataPoint> pitchSeries;

    private int lastX=0;
    private LineGraphSeries<DataPoint> pitchMeanAfterChangeSeries;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeVariables();
        // A hard coded value for the threshold
        threshold=10;
        meanPitchExpectedAfterChange=1;
        // Initialize the textview with '0'.

        // We get graph view instance
        final GraphView graph = (GraphView) findViewById(R.id.graph);
        // We initialize a series of data
        pitchSeries = new LineGraphSeries<DataPoint>();
        pitchMeanAfterChangeSeries = new LineGraphSeries<DataPoint>();

        graph.addSeries(pitchSeries);
        graph.addSeries(pitchMeanAfterChangeSeries);

        // Customize viewport
        final Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMaxX(100);
        viewport.setMinY(-1);
        viewport.setMaxY(1);
        viewport.setScrollable(true);
        viewport.setScalable(true);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                pitch=progress*multiplier;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Pitch: " + pitch );
            }
        });
        seekBarThreshold.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progressThreshold = progresValue;
                threshold=progressThreshold*multiplierThreshold;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewThreshold.setText("Threshold: " + threshold );
            }
        });
        seekBarMeanPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progressMeanAfterChange = progresValue;
                meanPitchExpectedAfterChange=progressMeanAfterChange*multiplier;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textViewMeanPitchAfterChange.setText("PostMeanPitch: " + meanPitchExpectedAfterChange );
            }
        });


        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cusumsPos.setText("CUSUM = ");
                    changeDetected.setText("noChangeDetected");
                    // The toggle is enabled
                    noiseTimer=new Timer();
                    noiseTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            TimerMethod();
                        }
                    }, 0, 100);
                    initializeCusumVariables();
                    seekBarThreshold.setProgress((int)progressThreshold);
                    seekBarMeanPitch.setProgress((int)progressMeanAfterChange);
                    graph.removeAllSeries();
                    pitchSeries = new LineGraphSeries<DataPoint>();
                    pitchMeanAfterChangeSeries=new LineGraphSeries<DataPoint>();
                    graph.addSeries(pitchMeanAfterChangeSeries);
                    graph.addSeries(pitchSeries);
                    viewport.setMinX(0);
                    viewport.setMaxX(100);
                    lastX=0;
                    // styling series
                    pitchSeries.setTitle("pitch rate");
                    pitchSeries.setColor(Color.BLUE);
                    // styling series
                    pitchMeanAfterChangeSeries.setTitle("MeanAfterChange");
                    pitchMeanAfterChangeSeries.setColor(Color.RED);

                } else {
                    // The toggle is disabled
                    if (noiseTimer != null){
                    noiseTimer.cancel();}
                    seekBar.setProgress(0);

                }
            }
        });

        }

    // Method for GrahView

    private void addEntry(double pitch){
        //Here we choose to display maximum 10 points and we scroll to the end
        if (lastX<100){
            pitchSeries.appendData(new DataPoint(lastX++,pitch), false,100);
            pitchMeanAfterChangeSeries.appendData(new DataPoint(lastX,meanPitchExpectedAfterChange),false,100);
        }
        else{
            pitchSeries.appendData(new DataPoint(lastX++,pitch), true,100);
            pitchMeanAfterChangeSeries.appendData(new DataPoint(lastX,meanPitchExpectedAfterChange),true,100);
        }
    }
    // A private method to help us initialize our variables (view)
    private void initializeVariables() {
        seekBar = (SeekBar) findViewById(seekBar2);
        seekBarThreshold = (SeekBar) findViewById(seekBarThreshold22);
        seekBarMeanPitch=(SeekBar) findViewById(seekBarMeanPitchAfterChange);
        textView = (TextView) findViewById(R.id.textView2);
        textViewThreshold = (TextView) findViewById(R.id.textViewThreshold);
        textViewMeanPitchAfterChange = (TextView) findViewById(R.id.textViewMeanPitchAfterChange);
        nbSamples=(TextView) findViewById(R.id.nbSamples);
        meanPitchs=(TextView) findViewById(R.id.meanPitchs);
        stdPitchs=(TextView) findViewById(R.id.stdPitchs);
        changeDetected=(TextView) findViewById(R.id.changeDetected);
        cusumsPos=(TextView) findViewById(R.id.cusum);

    }
    private void initializeCusumVariables() {
        meanPitch=0;
        sPitch=0;
        stdPitch=0;
        nbSample=0;
        cusumPos=0;
        cusumsPos.setText("CUSUM = "+ cusumPos);
        progressThreshold = threshold/multiplierThreshold;
        progressMeanAfterChange=meanPitchExpectedAfterChange/multiplierMeanAfterChange;
        progress =pitch/multiplier;

    }
    private void processSample(double sample){
        double prev_mean=meanPitch;
        nbSample=nbSample+1;
        nbSamples.setText("nb Samples:  "+nbSample);
        meanPitch=meanPitch+(sample-meanPitch)/nbSample;
        meanPitchs.setText("meanPitch:  "+meanPitch);
        sPitch=sPitch+(sample-meanPitch)*(sample - prev_mean);
        stdPitch=sqrt(sPitch/nbSample);
        stdPitchs.setText("stdPitch:  "+stdPitch);
        // This if statement is to be change according to the estimation we want for the mean
        if(nbSample>100){
            cusumPos=cusumPos + max(0,((meanPitchExpectedAfterChange-meanPitch)/stdPitch)*(sample-(meanPitch+meanPitchExpectedAfterChange)/2));
            cusumsPos.setText("CUSUM = "+ cusumPos);
            if (abs(cusumPos) > threshold){
                numberOfChange++;
                noiseTimer.cancel();
                changeDetected.setText("Change Detecteed !");
                toggle.toggle();
            }
        }

    }

    private void TimerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            // Hard coded value for the noise : gaussian with std 0.1 and mean 0
            double noise = 0.1*Math.random()-0.05;
            pitch=multiplier*seekBar.getProgress()+noise;
            processSample(pitch);
            textView.setText("Pitch: " + pitch);
            textViewThreshold.setText("Threshold"+threshold);
            textViewMeanPitchAfterChange.setText("PostMeanPitch"+meanPitchExpectedAfterChange);
            addEntry(pitch);

            //This method runs in the same thread as the UI.

            //Do something to the UI thread here

        }
    };

}


