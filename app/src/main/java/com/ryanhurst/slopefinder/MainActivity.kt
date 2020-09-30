package com.ryanhurst.slopefinder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener
import com.ryanhurst.slopefinder.SlopeService.LocalBinder
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(), ServiceConnection, SensorEventListener {
    private var serviceBinder: LocalBinder? = null
    private var fragmentListener: SensorEventListener? = null
    private var isCameraView = false
    private val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
          R.id.navigation_surface_angle -> if (isCameraView) surfaceAngleSelected()
          R.id.navigation_camera_finder -> if (!isCameraView) cameraFinderSelected()
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        if (savedInstanceState == null) {
            surfaceAngleSelected()
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, SlopeService::class.java)
        bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        unbindService(this)
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        if (fragment is SensorEventListener) {
            fragmentListener = fragment
        }
    }

    override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
        serviceBinder = iBinder as LocalBinder
        serviceBinder!!.service.registerListener(this)
    }

    override fun onServiceDisconnected(componentName: ComponentName) {
        Log.w(TAG, "service disconnected")
    }

    private fun surfaceAngleSelected() {
        isCameraView = false
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SurfaceAngleFragment()).commitAllowingStateLoss()
    }

    private fun cameraFinderSelected() {
        isCameraView = true
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, ViewFinderFragment()).commitAllowingStateLoss()
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        if (fragmentListener != null) {
            fragmentListener!!.onSensorChanged(sensorEvent)
        }
    }

    override fun onBackPressed() {
        if (isCameraView) {
            navigation.selectedItemId = R.id.navigation_surface_angle
        } else {
            super.onBackPressed()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {
        if (fragmentListener != null) {
            fragmentListener!!.onAccuracyChanged(sensor, i)
        }
        when (i) {
          SensorManager.SENSOR_STATUS_ACCURACY_LOW -> Log.d(TAG, "low accuracy for sensor: " + sensor.name)
          SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> Log.d(TAG, "high accuracy for sensor: " + sensor.name)
          SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> Log.d(TAG, "medium accuracy for sensor: " + sensor.name)
          SensorManager.SENSOR_STATUS_UNRELIABLE -> Log.d(TAG, "unreliable accuracy for sensor: " + sensor.name)
            else -> Log.d(TAG, "unknown accuracy for sensor: " + sensor.name + ": " + i)
        }
    }
}