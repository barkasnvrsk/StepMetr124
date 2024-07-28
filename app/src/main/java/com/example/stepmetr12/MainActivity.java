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

import androidx.annotation.NonNull;
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
            long timerMills = System.currentTimeMillis() - startTime;
            int timerSeconds = (int) (timerMills / 1000);
            int timerMin = timerSeconds / 60;
            timerSeconds = timerSeconds % 60;
            timeTextView.setText(String.format(Locale.getDefault(), "Time : %02d:%02d", timerMin, timerSeconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor
                    , SensorManager.SENSOR_DELAY_NORMAL);
            if (isTimerWork) {
                timerHandler.postDelayed(timerRunnable, 0);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stepCountTextView = findViewById(R.id.stepCountTextView);
        distanceTextView = findViewById(R.id.distanceTextView);
        timeTextView = findViewById(R.id.timeTextView);
        switchButton = findViewById(R.id.switchButton);
        stepCountTargetTextView = findViewById(R.id.stepCountTargetTextView);
        progressBar = findViewById(R.id.progressBar);
        checkSelfPermission();

        if(savedInstanceState!=null){
            readBundleIfExist(savedInstanceState);


            timeTextView.setText(savedInstanceState.getString("timeTextViewText"));




        }else {
            startTime = System.currentTimeMillis();
        }


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        progressBar.setMax(stepCountTarget);
        stepCountTargetTextView.setText("Step Goal " + stepCountTarget);

        if (stepCounterSensor == null) {
            stepCountTextView.setText("Step counter not available");
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
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("stepCountInt",stepCountInt);
        outState.putBoolean("isTimerWork",isTimerWork);
        outState.putLong("startTime",startTime);
        outState.putLong("timePausedLong",timePausedLong);
        outState.putFloat("distanceInKm",distanceInKm);
        outState.putString("timeTextViewText",timeTextView.getText().toString());
    }

    private void readBundleIfExist(Bundle bundle) {
        stepCountInt = bundle.getInt("stepCountInt");
        isTimerWork = bundle.getBoolean("isTimerWork");
        startTime = bundle.getLong("startTime");
        timePausedLong = bundle.getLong("timePausedLong");
        distanceInKm = bundle.getFloat("distanceInKm");
        switchButton.setChecked(isTimerWork);
        timeTextView.setText(bundle.getString("timeTextViewText"));
        if (isTimerWork) {
            startTimer();
        } else {
            stopTimer();
        }
    }

    public void onPauseSwitchButtonClicked(View view) {
        SwitchCompat v = (SwitchCompat) view;
        if (v.isChecked()) {
            startTimer();
        } else {
            stopTimer();
        }
    }

    private void startTimer() {
        isTimerWork = true;
        startTime = System.currentTimeMillis() - timePausedLong;
        timerHandler.postDelayed(timerRunnable, 0);
    }

    private void stopTimer() {
        if(isTimerWork){
            isTimerWork = false;
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
