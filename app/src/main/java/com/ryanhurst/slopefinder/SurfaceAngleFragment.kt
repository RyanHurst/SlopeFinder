package com.ryanhurst.slopefinder

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ryanhurst.slopefinder.databinding.FragmentCurrentAngleBinding

/**
 * Created by Ryan on 3/5/2017.
 * fragment to determine slope of surface that device is resting on
 */
class SurfaceAngleFragment : Fragment(), SensorEventListener {

    private lateinit var binding: FragmentCurrentAngleBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCurrentAngleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        val angle = SlopeService.getAngleFromSensorEvent(sensorEvent)
        binding.surfaceAngleText.text = SlopeService.formatAngle(angle)
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
}