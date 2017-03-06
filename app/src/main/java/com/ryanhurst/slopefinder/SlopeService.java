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

    class LocalBinder extends Binder {
        SlopeService getService() {
            return SlopeService.this;
        }
    }

    public static String formatAngle(double angle) {
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(angle) + (char) 0x00B0;
    }

    public static double getAngleFromSensorEvent(SensorEvent sensorEvent) {
        float[] data = sensorEvent.values;

        //represent quaternion as a matrix;
        Quart q = new Quart(data[3], data[0], data[1], data[2]);

        Vector xyNormalVector = new Vector(0, 0, 1);

        //represent vector as a quaternion with 0 as the real value
        Quart p = new Quart(0, xyNormalVector.x, xyNormalVector.y, xyNormalVector.z);

        // perform quaternion rotation on xyNormalVector
        // where
        // p = xyNormalVector
        // p' = deviceVector
        //
        // https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation#The_conjugation_operation
        // p' = qpq^-1

        float[] qp = new float[16];

        Matrix.multiplyMM(qp, 0, q.matrix, 0, p.matrix, 0);

        float[] qInverse = new float[16];

        Matrix.invertM(qInverse, 0, q.matrix, 0);

        float[] qpqInv = new float[16];
        Matrix.multiplyMM(qpqInv, 0, qp, 0, qInverse, 0);

        Vector deviceVector = new Vector(qpqInv[1], qpqInv[2], qpqInv[3]);

        //measure angle between vertical vector and the new vector
        double radians = measureAngleBetweenTwoVectors(xyNormalVector, deviceVector);

        return toDegrees(radians);
    }

    /**
     * measure the acute angle between two vectors
     *
     * http://www.vitutor.com/geometry/distance/angle_planes.html
     * @return radians
     */
    private static double measureAngleBetweenTwoVectors(Vector v1, Vector v2) {
        return acos(
                abs(v1.x * v2.x + v1.y * v2.y + v1.z * v2.z)/
                (sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z) * sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z))
        );
    }


    /**
     * mathematical vector object
     */
    private static class Vector {
        float x, y, z;
        Vector(float x,  float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * class to represent a Quaternion as a matrix
     *         //https://en.wikipedia.org/wiki/Quaternion
     */
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
