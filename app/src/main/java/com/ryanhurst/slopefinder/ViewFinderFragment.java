package com.ryanhurst.slopefinder;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.cameraview.CameraView;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Ryan on 3/6/2017.
 * fragment to determine slope of line from current position to where the camera is pointing
 */
public class ViewFinderFragment extends Fragment implements SensorEventListener {

    public static final String TAG = "ViewFinderFragment";

    private static final int PERMISSION_REQUEST_CAMERA = 46;

    private boolean permissionDenied;

    @BindView(R.id.view_finder_text)
    TextView viewFinderText;

    @BindView(R.id.camera_view)
    CameraView cameraView;

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

        permissionDenied = false;

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

        cameraView.setFacing(CameraView.FACING_BACK);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            cameraView.start();
        } else if (!permissionDenied) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.CAMERA)) {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title)
                        .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestPermissions(new String[]{Manifest.permission.CAMERA},
                                        PERMISSION_REQUEST_CAMERA);
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .setCancelable(false);

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.stop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA: {
                if (permissions.length != 1 || grantResults.length != 1) {
                    throw new RuntimeException("Error on requesting camera permission.");
                }
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    permissionDenied = true;
                }
                // No need to start camera here; it is handled by onResume
                break;
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double angle = SlopeService.getAngleFromSensorEvent(sensorEvent);

        angle = 90 - angle;

        viewFinderText.setText(SlopeService.formatAngle(angle));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}
