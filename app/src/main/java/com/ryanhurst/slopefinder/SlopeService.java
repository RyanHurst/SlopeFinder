package com.ryanhurst.slopefinder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;

public class SlopeService extends Service {
    public static final String TAG = "SlopeService";

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final LocalBinder mBinder = new LocalBinder();

    private SensorManager mSensorManager;
    private Sensor rotationSensor;

    private SensorEventListener sensorEventListener;

    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if (rotationSensor == null) {
            // Success! There's a rotation vector.
            Log.e(TAG, "no rotation sensor found");
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(sensorEventListener);
    }

    public void registerListener(SensorEventListener sensorEventListener) {
        if(this.sensorEventListener != null) {
            mSensorManager.unregisterListener(this.sensorEventListener);
        }
        this.sensorEventListener = sensorEventListener;
        mSensorManager.registerListener(sensorEventListener, rotationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public class LocalBinder extends Binder {
        SlopeService getService() {
            return SlopeService.this;
        }
    }
}
