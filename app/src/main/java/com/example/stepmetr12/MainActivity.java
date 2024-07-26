package com.example.stepmetr12;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private TextView stepCountTextView;
    private TextView distanceTextView;
    private TextView timeTextView;
    private SwitchCompat switchButton;
    private ProgressBar progressBar;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    private int stepCountInt = 0;
    private boolean isTimerWork = false;
    private long timePausedLong = 0;
    private float stepLengthInMeters = 0.762f;
    private long startTime;
    private float distanceInKm;
    private int stepCountTarget = 5000;
    private TextView stepCountTargetTextView;

    private Handler timerHandler = new Handler();

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long mills = System.currentTimeMillis() - startTime;
            int seconds = (int) (mills / 1000);
            int min = seconds / 60;
            seconds = seconds % 60;
            timeTextView.setText(String.format(Locale.getDefault(), "Time : %02d:%02d", min, seconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor
                    , SensorManager.SENSOR_DELAY_FASTEST);//SENSOR_DELAY_NORMAL
            if (isTimerWork) {
                timerHandler.postDelayed(timerRunnable, 0);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (stepCounterSensor != null) {
            sensorManager.unregisterListener(this);
            timerHandler.removeCallbacks(timerRunnable);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stepCountTextView = findViewById(R.id.stepCountTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        timeTextView = findViewById(R.id.timeTextView);
//        pauseButton = findViewById(R.id.pauseButton);
        switchButton = findViewById(R.id.switchButton);
        stepCountTargetTextView = findViewById(R.id.stepCountTargetTextView);
        progressBar = findViewById(R.id.progressBar);

        checkSelfPermission();

        startTime = System.currentTimeMillis();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        progressBar.setMax(stepCountTarget);
        stepCountTargetTextView.setText("Step Goal " + stepCountTarget);

        if (stepCounterSensor == null) {
            stepCountTextView.setText("Step counter not available");
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
//            stepCountInt = (int) sensorEvent.values[0];
            stepCountTextView.setText("Step count : " + stepCountInt);
            progressBar.setProgress(stepCountInt);
            stepCountInt++ ;

            if (stepCountInt >= stepCountTarget) {
                stepCountTargetTextView.setText("Step Goal Achieved");
            }

            distanceInKm = stepCountInt * stepLengthInMeters / 1000;

            distanceTextView.setText(String.format(Locale.getDefault(), "Distance: %.2f km", distanceInKm));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    public void onPauseSwitchButtonClicked(View view) {
        SwitchCompat v = (SwitchCompat) view;
        if (v.isChecked()) {
//            v.setText("Работает");
            isTimerWork = false;
            startTime = System.currentTimeMillis() - timePausedLong;
            timerHandler.postDelayed(timerRunnable, 0);
        } else {
//            v.setText("Выключено");
            isTimerWork = true;
            timerHandler.removeCallbacks(timerRunnable);
            timePausedLong = System.currentTimeMillis() - startTime;
        }
    }

    public void onResetButtonClicked(View view){
        startTime = System.currentTimeMillis();
        progressBar.setProgress(0);
        stepCountInt = 0;
        distanceInKm = 0;
        stepCountTextView.setText("Step count : " + stepCountInt);
        distanceTextView.setText(String.format(Locale.getDefault(), "Distance: %.2f km", distanceInKm));

    }
    private void checkSelfPermission() {
        //проверка есть ли на телефоне датчики FEATURE_SENSOR_STEP_DETECTOR и FEATURE_SENSOR_STEP_COUNTER
        if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_DETECTOR)
                && getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)) {
            // Continue with the part of your app's workflow that requires a
            // front-facing camera.
        } else {
            // Gracefully degrade your app experience.
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{android.Manifest.permission.ACTIVITY_RECOGNITION}, 1);
        }
    }
}
