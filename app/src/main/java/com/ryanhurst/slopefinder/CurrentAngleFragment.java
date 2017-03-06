package com.ryanhurst.slopefinder;

import android.app.Fragment;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_HIGH;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_LOW;
import static android.hardware.SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM;
import static android.hardware.SensorManager.SENSOR_STATUS_UNRELIABLE;

/**
 * Created by Ryan on 3/5/2017.
 */

public class CurrentAngleFragment extends Fragment implements SensorEventListener {
    public static final String TAG = "CurrentAngleFragment";

    @BindView(R.id.current_angle_text)
    TextView currentAngleText;

    public CurrentAngleFragment(){
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

        String angle = "";
        for(float f : data) {
            if(f < 1) {
                int i = Math.round(f * 100);

                String dec = i%100 + "";
                if(i%100 < 10)
                    dec = 0 + dec;

                String s = i / 100 + "." + dec + "\n";
                angle += s;
            } else {
                angle += f + "\n";
            }
        }
        return angle;
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
