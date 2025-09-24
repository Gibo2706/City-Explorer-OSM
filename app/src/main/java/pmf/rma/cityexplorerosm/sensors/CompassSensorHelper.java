package pmf.rma.cityexplorerosm.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class CompassSensorHelper implements SensorEventListener {

    public interface CompassListener {
        void onNewAzimuth(float azimuth);
    }

    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final Sensor magnetometer;

    private final float[] gravity = new float[3];
    private final float[] geomagnetic = new float[3];
    private float azimuth = 0f;

    private CompassListener listener;

    public CompassSensorHelper(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    public void setListener(CompassListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        }
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, event.values.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, event.values.length);
        }

        float R[] = new float[9];
        float I[] = new float[9];

        if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
            float orientation[] = new float[3];
            SensorManager.getOrientation(R, orientation);
            float azimuthRad = orientation[0];
            float azimuthDeg = (float) Math.toDegrees(azimuthRad);
            azimuthDeg = (azimuthDeg + 360) % 360;

            if (Math.abs(azimuthDeg - azimuth) > 1) { // filter da ne pretrpavamo UI
                azimuth = azimuthDeg;
                if (listener != null) {
                    listener.onNewAzimuth(azimuth);
                }
                Log.d("CompassSensor", "New azimuth: " + azimuth);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // ignorisano
    }
}
