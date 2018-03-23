package mc.assignment.group15;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerHandler implements SensorEventListener {

    private SensorManager senSensorManager = null;
    private Sensor senAccelerometer = null;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;

    public AccelerometerHandler(Context ctx) {
        senSensorManager = (SensorManager) ctx.getSystemService(ctx.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public void registerListener() {
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterListener() {
        senSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor  = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            last_x = event.values[0];
            last_y = event.values[1];
            last_z = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public float readX() {
        return last_x;
    }

    public float readY() {
        return last_y;
    }

    public float readZ() {
        return last_z;
    }

}
