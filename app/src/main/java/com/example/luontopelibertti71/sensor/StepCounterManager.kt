package com.example.luontopelibertti71.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

//null jos laite ei tue kyseistä sensoria
class StepCounterManager(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    //TYPE_STEP_DETECTOR laukeaa jokaisen askeleen kohdalla
    private val stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    //gyroskooppi mittaa laitteen pyörimisnopeuden
    private val gyroSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private var stepListener: SensorEventListener? = null
    private var gyroListener: SensorEventListener? = null

    fun startStepCounting(onStep: () -> Unit) {
        stepListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) { onStep() }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        stepSensor?.let {
            sensorManager.registerListener(stepListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun stopStepCounting() {
        stepListener?.let { sensorManager.unregisterListener(it) }
        stepListener = null
    }


    fun startGyroscope(onRotation: (Float, Float, Float) -> Unit) {
        gyroListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                onRotation(event.values[0], event.values[1], event.values[2])
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        gyroSensor?.let {
            sensorManager.registerListener(gyroListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopGyroscope() {
        gyroListener?.let { sensorManager.unregisterListener(it) }
        gyroListener = null
    }

    //pysäyttää sensorit
    fun stopAll() {
        stopStepCounting()
        stopGyroscope()
    }

    companion object {
        //keskimääräinen askelpituus metreinä. käytetään matkan laskemiseen
        const val STEP_LENGTH_METERS = 0.74f
    }
}