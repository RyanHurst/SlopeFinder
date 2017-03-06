package com.ryanhurst.slopefinder;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Ryan on 3/6/2017.
 * fragment to determine slope of line from current position to where the camera is pointing
 */
public class ViewFinderFragment extends Fragment implements SensorEventListener {

    public static final String TAG = "ViewFinderFragment";

    @BindView(R.id.view_finder_text)
    TextView viewFinderText;

    public ViewFinderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ViewFinderFragment.
     */
    public static ViewFinderFragment newInstance() {
        return new ViewFinderFragment();
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
        View view = inflater.inflate(R.layout.fragment_view_finder, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Log.d(TAG, "event");

        double angle = SlopeService.getAngleFromSensorEvent(sensorEvent);

        angle = 90 - angle;

        viewFinderText.setText(SlopeService.formatAngle(angle));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
