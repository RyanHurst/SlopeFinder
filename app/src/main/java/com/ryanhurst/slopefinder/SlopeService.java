package com.ryanhurst.slopefinder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;
import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

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

    public static String formatAngle(double angle) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(angle) + (char) 0x00B0;
    }

    public static double getAngleFromSensorEvent(SensorEvent sensorEvent) {
        float[] data = sensorEvent.values;

        //first represent quaternion as a matrix;
        Quart q = new Quart(data[3], data[0], data[1], data[2]);

        float x1 = 0;
        float y1 = 0;
        float z1 = 1;

        Quart p = new Quart(0, x1, y1, z1);


        //then perform quaternion rotation on vertical vector
        //p' = qpq^-1

        float[] qp = new float[16];

        Matrix.multiplyMM(qp, 0, q.matrix, 0, p.matrix, 0);

        float[] qInverse = new float[16];

        Matrix.invertM(qInverse, 0, q.matrix, 0);

        float[] qpqInv = new float[16];
        Matrix.multiplyMM(qpqInv, 0, qp, 0, qInverse, 0);

        double x2 = qpqInv[1];
        double y2 = qpqInv[2];
        double z2 = qpqInv[3];

        //finally measure angle between vertical vector and the new vector
        double radians = acos(abs(x1 * x2 + y1 * y2 + z1 * z2)/(sqrt(x1 * x1 + y1 * y1 + z1 * z1) * sqrt(x2 * x2 + y2 * y2 + z2 * z2)));

        return toDegrees(radians);
    }

    private static class Quart {
        float a, b, c, d;
        float[] matrix;

        Quart(float a, float b, float c, float d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            createMatrix();
        }


        /**
         * a -b -c -d
         * b  a -d  c
         * c  d  a -b
         * d -c  b  a
         */

        private void createMatrix() {
            matrix = new float[]{a, b, c, d, -b, a, d, -c, -c, -d, a, b, -d, c, -b, a};
        }
    }


}
