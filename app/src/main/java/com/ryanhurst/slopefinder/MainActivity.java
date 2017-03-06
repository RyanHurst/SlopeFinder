package com.ryanhurst.slopefinder;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;


public class MainActivity extends Activity implements ServiceConnection,
        DashboardFragment.OnFragmentInteractionListener, SensorEventListener {

    public static final String TAG = "MainActivity";

    SlopeService.LocalBinder binder;
    SensorEventListener fragmentListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null) {
            getFragmentManager().beginTransaction()
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
    public void onAttachFragment(Fragment fragment) {
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
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, CurrentAngleFragment.newInstance(), CurrentAngleFragment.TAG)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    @Override
    public void cameraFinderSelected() {

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
    }
}
