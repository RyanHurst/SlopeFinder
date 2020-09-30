package com.ryanhurst.slopefinder

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_current_angle.*

/**
 * Created by Ryan on 3/5/2017.
 * fragment to determine slope of surface that device is resting on
 */
class SurfaceAngleFragment : Fragment(R.layout.fragment_current_angle), SensorEventListener {
    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val angle = SlopeService.getAngleFromSensorEvent(sensorEvent)
        surface_angle_text.text = SlopeService.formatAngle(angle)
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
}