package com.ryanhurst.slopefinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;


public class MainActivity extends AppCompatActivity implements ServiceConnection,
        DashboardFragment.OnFragmentInteractionListener, SensorEventListener {

    public static final String TAG = "MainActivity";

    SlopeService.LocalBinder binder;
    SensorEventListener fragmentListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, DashboardFragment.newInstance(), DashboardFragment.TAG)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, SlopeService.class);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    @Override
    public void onAttachFragment(android.support.v4.app.Fragment fragment) {
        super.onAttachFragment(fragment);
        if(fragment instanceof SensorEventListener) {
            fragmentListener = (SensorEventListener) fragment;
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        binder = (SlopeService.LocalBinder) iBinder;
        binder.getService().registerListener(this);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.w(TAG, "service disconnected");
    }

    @Override
    public void currentAngleSelected() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, CurrentAngleFragment.newInstance(), CurrentAngleFragment.TAG)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    @Override
    public void cameraFinderSelected() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, ViewFinderFragment.newInstance(), ViewFinderFragment.TAG)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(fragmentListener != null) {
            fragmentListener.onSensorChanged(sensorEvent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        if(fragmentListener != null) {
            fragmentListener.onAccuracyChanged(sensor, i);
        }

        switch (i) {
            case SENSOR_STATUS_ACCURACY_LOW:
                Log.d(TAG, "low accuracy for sensor: " + sensor.getName());
                break;
            case SENSOR_STATUS_ACCURACY_HIGH:
                Log.d(TAG, "high accuracy for sensor: " + sensor.getName());
                break;
            case SENSOR_STATUS_ACCURACY_MEDIUM:
                Log.d(TAG, "medium accuracy for sensor: " + sensor.getName());
                break;
            case SENSOR_STATUS_UNRELIABLE:
                Log.d(TAG, "unreliable accuracy for sensor: " + sensor.getName());
                break;
            default:
                Log.d(TAG, "unknown accuracy for sensor: " + sensor.getName() + ": " + i);
        }
    }
}
