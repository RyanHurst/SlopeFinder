package com.ryanhurst.slopefinder

import android.Manifest.permission
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import androidx.appcompat.app.AlertDialog.Builder
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_view_finder.*

private const val TAG = "ViewFinderFragment"
private const val PERMISSION_REQUEST_CAMERA = 46

/**
 * Created by Ryan on 3/6/2017.
 * fragment to determine slope of line from current position to where the camera is pointing
 */
class ViewFinderFragment : Fragment(R.layout.fragment_view_finder), SensorEventListener {
    private var permissionDenied = false

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireActivity(), permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else if (!permissionDenied) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission.CAMERA)) {
                Builder(requireContext())
                        .setMessage(R.string.dialog_message)
                        .setTitle(R.string.dialog_title)
                        .setNeutralButton(R.string.ok) { _, _ -> requestPermissions(arrayOf(permission.CAMERA), PERMISSION_REQUEST_CAMERA) }
                        .setNegativeButton(R.string.cancel, null)
                        .setCancelable(false)
                        .create()
                        .show()
            } else {
                requestPermissions(arrayOf(permission.CAMERA), PERMISSION_REQUEST_CAMERA)
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
          val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
          val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(viewFinder.surfaceProvider)
          }
          try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
          } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
          }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
          PERMISSION_REQUEST_CAMERA -> {
            if (permissions.size != 1 || grantResults.size != 1) {
              throw RuntimeException("Error on requesting camera permission.")
            }
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
              permissionDenied = true
            }
          }
        }
    }

    override fun onSensorChanged(sensorEvent: SensorEvent) {
        var angle = SlopeService.getAngleFromSensorEvent(sensorEvent)
        angle = 90 - angle
        view_finder_text.text = SlopeService.formatAngle(angle)
    }

    override fun onAccuracyChanged(sensor: Sensor, i: Int) {}
}