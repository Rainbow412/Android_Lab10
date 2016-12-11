package com.example.rainbow.lab10;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ArrowView mArrowView;
    private TextView mRotationTextView;
    private TextView mShakeTextView;
    private TextView mJingduTextView;
    private TextView mWeiduTextView;
    private TextView mLocationTextView;

    private SensorManager mSensorManager;
    private Sensor mMagneticSensor, mAccelerometerSensor;
    private LocationManager mLocationManager;
    private String provider = LocationManager.NETWORK_PROVIDER;

    private float mCurrentRotation = 0f;
    private int mShakeCounter = 0;

    public static final int THRESHOLD_SHAKE_INTERVAL = 1000;
    public static final int THRESHOLD_SHAKE_SPEED = 15;
    public static final float THRESHOLD_ROTATION_UPDATE = 5.0f;

    private boolean isProcessingShake = false;

    // sensor event listener
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        float[] accValues = null;
        float[] magValues = null;
        long lastShakeTime = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    // do something about values of accelerometer
                    accValues = event.values.clone();

                    long curTime = System.currentTimeMillis();
                    if ((curTime - lastShakeTime) >= THRESHOLD_SHAKE_INTERVAL) {
                        lastShakeTime = curTime;
                        if ((Math.abs(accValues[0]) > THRESHOLD_SHAKE_SPEED
                                || Math.abs(accValues[1]) > THRESHOLD_SHAKE_SPEED
                                || Math.abs(accValues[2]) > THRESHOLD_SHAKE_SPEED)) {
                            if (!isProcessingShake) {
                                isProcessingShake = true;

                                Toast.makeText(MainActivity.this, "SHAKE THE PHONE", Toast.LENGTH_LONG).show();
                                mShakeCounter++;
                                mShakeTextView.setText("摇一摇计数：" + mShakeCounter);

                                isProcessingShake = false;
                            }
                        }
                    }

                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    // do something about values of magnetic field
                    magValues = event.values.clone();

                    break;
                default:
                    break;
            }

            if (accValues != null && magValues != null) {
                float[] R = new float[9];
                float[] values = new float[3];

                SensorManager.getRotationMatrix(R, null, accValues, magValues);
                SensorManager.getOrientation(R, values);

                float newRotationDegree = -(float) Math.toDegrees(values[0]);

                if (Math.abs(newRotationDegree - mCurrentRotation) > THRESHOLD_ROTATION_UPDATE) {
                    mArrowView.onUpdateRotation(newRotationDegree);
                    mRotationTextView.setText("旋转角度：" + String.format("%.2f", newRotationDegree));

                    mCurrentRotation = newRotationDegree;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

//    private LocationListener mGPSListener = new LocationListener() {
//
//        private boolean networkIsRemove = false;
//
//        @Override
//        public void onLocationChanged(Location location) {
//            boolean flag = isBetterLocation(location,
//                    mCurrentLocation);
//            if (flag) {
//                mCurrentLocation = location;
//                if (mOnSensorUpdateListener != null) {
//                    mOnSensorUpdateListener.onSensorUpdate(TYPE_LOCATION_CHANGE,
//                            new float[]{(float) mCurrentLocation.getLongitude(),
//                                    (float) mCurrentLocation.getLatitude(),
//                                    (float) mCurrentLocation.getAltitude()});
//                }
//                makeToast("Location Update from GPS");
//            }
//
//            if (location != null && !networkIsRemove) {
//                if (ActivityCompat.checkSelfPermission(BaseApplication.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED
//                        && ActivityCompat.checkSelfPermission(BaseApplication.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//                mLocationManager.removeUpdates(mNetworkListener);
//                networkIsRemove = true;
//            }
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//            if (LocationProvider.OUT_OF_SERVICE == status) {
//                makeToast("Lost GPS, change to network");
//                if (ActivityCompat.checkSelfPermission(BaseApplication.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED
//                        && ActivityCompat.checkSelfPermission(BaseApplication.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//
//                if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//                    mLocationManager.requestLocationUpdates(
//                            LocationManager.NETWORK_PROVIDER, 0, 0, mNetworkListener);
//                    networkIsRemove = false;
//                }
//            }
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//            makeToast("Enable GPS provider");
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//            makeToast("Disable GPS provider");
//        }
//    };
//
//    private LocationListener mNetworkListener = new LocationListener() {
//        @Override
//        public void onLocationChanged(Location location) {
//            mCurrentLocation = location;
//            if (mOnSensorUpdateListener != null) {
//                mOnSensorUpdateListener.onSensorUpdate(TYPE_LOCATION_CHANGE,
//                        new float[]{(float) mCurrentLocation.getLongitude(),
//                                (float) mCurrentLocation.getLatitude(),
//                                (float) mCurrentLocation.getAltitude()});
//            }
//            makeToast("Location Update from network");
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//            makeToast("Enable network provider");
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//            makeToast("Disable network provider");
//        }
//    };

    private LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // processing new location
            double weidu = location.getLatitude();
            double jingdu = location.getLongitude();

            mJingduTextView.setText("经度：" + jingdu);
            mWeiduTextView.setText("纬度：" + weidu);

            new GeoDecoder().execute(location);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mArrowView = (ArrowView) findViewById(R.id.arrow);
        mRotationTextView = (TextView) findViewById(R.id.arrowText);
        mShakeTextView = (TextView) findViewById(R.id.shake);
        mJingduTextView = (TextView) findViewById(R.id.jingdu);
        mWeiduTextView = (TextView) findViewById(R.id.weidu);
        mLocationTextView = (TextView)findViewById(R.id.addr);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    @Override
    protected void onResume() {
        super.onResume();
        // register magnetic and accelerometer sensor into sensor manager
        mSensorManager.registerListener(mSensorEventListener, mMagneticSensor,
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(mSensorEventListener, mAccelerometerSensor,
                SensorManager.SENSOR_DELAY_GAME);

        // register location update listener
//        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mGPSListener);
//        }
//        if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mNetworkListener);
//        }

        mLocationManager.requestLocationUpdates(provider, 2000, 10, mLocationListener);
        if (PackageManager.PERMISSION_GRANTED == getPackageManager().checkPermission(
                "ACCESS_COARSE_LOCATION", "com.example.rainbow.lab10")) {
            Location lastKnownLocation = mLocationManager.getLastKnownLocation(provider);
            if(lastKnownLocation!=null) {
                double weidu = lastKnownLocation.getLatitude();
                double jingdu = lastKnownLocation.getLongitude();

                mJingduTextView.setText("经度：" + jingdu);
                mWeiduTextView.setText("纬度：" + weidu);
                new GeoDecoder().execute(lastKnownLocation);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregister sensors
        mSensorManager.unregisterListener(mSensorEventListener);

        // unregister update listener
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.removeUpdates(mLocationListener);
    }

    private class GeoDecoder extends AsyncTask<Location, Integer, String> {

        @Override
        protected String doInBackground(Location... params) {
            Location param = params[0];
            try {
                String result = sendHttpRequest(String.format(getString(R.string.coor_change),
                        param.getLongitude(), param.getLatitude(), getString(R.string.server_ak)));
                JSONObject jObject = new JSONObject(result);
                JSONArray jsonArray = jObject.getJSONArray("result");
                JSONObject locObject = jsonArray.getJSONObject(0);
                double x = locObject.getDouble("x");
                double y = locObject.getDouble("y");

                String decode = sendHttpRequest(String.format(getString(R.string.geo_decode),
                        y, x, getString(R.string.server_ak)));
                JSONObject geoObject = new JSONObject(decode);
                return geoObject.getString("formatted_address");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return "Error";
        }

        @Override
        protected void onPostExecute(String s) {
            mLocationTextView.setText(s);
        }

        private String sendHttpRequest(String request) throws IOException {
            URL url = new URL(request);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is = connection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String response = "";
            String readLine;
            while ((readLine = br.readLine()) != null) {
                response = response + readLine;
            }
            is.close();
            br.close();
            connection.disconnect();
            return response;
        }
    }
}
