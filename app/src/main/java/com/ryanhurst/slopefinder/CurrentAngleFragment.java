package com.ryanhurst.slopefinder;

import android.app.Fragment;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;
import static java.lang.Math.*;

/**
 * Created by Ryan on 3/5/2017.
 * fragment to determine slope of surface that device is resting on
 */

public class CurrentAngleFragment extends Fragment implements SensorEventListener {
    public static final String TAG = "CurrentAngleFragment";

    @BindView(R.id.current_angle_text)
    TextView currentAngleText;

    public CurrentAngleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CurrentAngleFragment.
     */
    public static CurrentAngleFragment newInstance() {
        return new CurrentAngleFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //keep screen on
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_current_angle, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        currentAngleText.setText(getAngleFromSensorEvent(sensorEvent));
    }

    private String getAngleFromSensorEvent(SensorEvent sensorEvent) {
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

        double degrees = toDegrees(radians);

        return roundTwoDecimals(degrees) + (char) 0x00B0;
    }

    private class Quart {
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


    private String roundTwoDecimals(double f) {
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return df.format(f);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
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
