package com.ryanhurst.slopefinder;

import android.support.v4.app.Fragment;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Ryan on 3/5/2017.
 * fragment to determine slope of surface that device is resting on
 */

public class SurfaceAngleFragment extends Fragment implements SensorEventListener {
    public static final String TAG = "SurfaceAngleFragment";

    @BindView(R.id.surface_angle_text)
    TextView surfaceAngleText;

    public SurfaceAngleFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SurfaceAngleFragment.
     */
    public static SurfaceAngleFragment newInstance() {
        return new SurfaceAngleFragment();
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
        double angle = SlopeService.getAngleFromSensorEvent(sensorEvent);

        surfaceAngleText.setText(SlopeService.formatAngle(angle));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
