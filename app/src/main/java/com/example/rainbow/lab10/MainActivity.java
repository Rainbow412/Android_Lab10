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
import android.util.Log;
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
    private String provider = LocationManager.NETWORK_PROVIDER; //利用网络来定位

    private float mCurrentRotation = 0f; //记录当前手机朝向
    private int mShakeCounter = 0; //记录摇一摇次数

    public static final int THRESHOLD_SHAKE_INTERVAL = 1000; //摇一摇间隔阈值
    public static final int THRESHOLD_SHAKE_SPEED = 15; //摇一摇加速度阈值
    public static final float THRESHOLD_ROTATION_UPDATE = 5.0f; //旋转角度阈值

    private boolean isProcessingShake = false;

    //传感器事件监听器
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        float[] accValues = null; //加速度传感器的值
        float[] magValues = null; //地磁传感器的值
        long lastShakeTime = 0; //记录上一次摇一摇的时间

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                //加速度传感器的值发生变化
                case Sensor.TYPE_ACCELEROMETER:
                    accValues = event.values.clone();
                    long curTime = System.currentTimeMillis(); //获取当前时间
                    //摇一摇间隔时间大于阈值
                    if ((curTime - lastShakeTime) >= THRESHOLD_SHAKE_INTERVAL) {
                        lastShakeTime = curTime;
                        //摇一摇加速度大于阈值
                        if ((Math.abs(accValues[0]) > THRESHOLD_SHAKE_SPEED
                                || Math.abs(accValues[1]) > THRESHOLD_SHAKE_SPEED
                                || Math.abs(accValues[2]) > THRESHOLD_SHAKE_SPEED)) {
                            if (!isProcessingShake) {
                                isProcessingShake = true;

                                Toast.makeText(MainActivity.this, "SHAKE THE PHONE",
                                        Toast.LENGTH_LONG).show();
                                mShakeCounter++;
                                mShakeTextView.setText("摇一摇计数：" + mShakeCounter);

                                isProcessingShake = false;
                            }
                        }
                    }
                    break;
                //地磁传感器的值发生变化
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magValues = event.values.clone();
                    break;
                default:
                    break;
            }

            //利用地磁传感器和加速度传感器共同计算手机朝向
            if (accValues != null && magValues != null) {
                float[] R = new float[9];
                float[] values = new float[3];

                SensorManager.getRotationMatrix(R, null, accValues, magValues); //得到旋转矩阵
                SensorManager.getOrientation(R, values); //得到手机朝向

                float newRotationDegree = -(float) Math.toDegrees(values[0]); //当前手机朝向

                //手机朝向旋转角度大于阈值
                if (Math.abs(newRotationDegree-mCurrentRotation)>THRESHOLD_ROTATION_UPDATE){
                    //改变箭头角度以及文字提示
                    mArrowView.onUpdateRotation(newRotationDegree);
                    mRotationTextView.setText("旋转角度：" +
                            String.format("%.2f", newRotationDegree)); //保留小数点2位
                    //记录手机朝向
                    mCurrentRotation = newRotationDegree;
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
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

    //位置变化事件监听类
    private LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //得到新的位置
            double weidu = location.getLatitude();
            double jingdu = location.getLongitude();

            mJingduTextView.setText("经度：" + jingdu);
            mWeiduTextView.setText("纬度：" + weidu);

            new GeoDecoder().execute(location); //通过坐标获取具体位置描述
        }
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        public void onProviderEnabled(String provider) {}
        public void onProviderDisabled(String provider) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //findView
        mArrowView = (ArrowView) findViewById(R.id.arrow);
        mRotationTextView = (TextView) findViewById(R.id.arrowText);
        mShakeTextView = (TextView) findViewById(R.id.shake);
        mJingduTextView = (TextView) findViewById(R.id.jingdu);
        mWeiduTextView = (TextView) findViewById(R.id.weidu);
        mLocationTextView = (TextView)findViewById(R.id.addr);

        //获取传感器管理器和位置管理器
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //获取地磁传感器和加速度传感器
        mMagneticSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    //前台运行
    @Override
    protected void onResume() {
        super.onResume();
        //注册地磁传感器和加速度传感器
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

        //指定位置管理器的定位方法、产生位置改变时间的时间间隔、距离、回调函数
        mLocationManager.requestLocationUpdates(provider, 2000, 10, mLocationListener);
        //检查是否获取了用户权限
        if (PackageManager.PERMISSION_GRANTED == getPackageManager().checkPermission(
                "ACCESS_COARSE_LOCATION", "com.example.rainbow.lab10")) {
            //获取一个缓存的位置信息
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

    //离开前台
    @Override
    protected void onPause() {
        super.onPause();
        // 取消注册传感器
        mSensorManager.unregisterListener(mSensorEventListener);

        //取消注册位置管理器
        if (PackageManager.PERMISSION_GRANTED == getPackageManager().checkPermission(
                "ACCESS_COARSE_LOCATION", "com.example.rainbow.lab10")) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    //利用百度LBS开放平台的API实现逆地理编码
    private class GeoDecoder extends AsyncTask<Location, Integer, String> {
        @Override
        protected String doInBackground(Location... params) {
            Location param = params[0];
            try {
                //先把经纬度坐标转换成百度地图的坐标
                String result = sendHttpRequest(String.format(getString(R.string.coor_change),
                        param.getLongitude(), param.getLatitude(), getString(R.string.server_ak)));
                JSONObject jObject = new JSONObject(result);
                JSONArray jsonArray = jObject.getJSONArray("result");
                JSONObject locObject = jsonArray.getJSONObject(0);
                double x = locObject.getDouble("x");
                double y = locObject.getDouble("y");
                Log.d("x", ""+x);
                Log.d("y", ""+y);
                //再调用逆地理编码API获取当前位置的描述
                Log.d("request", String.format(getString(R.string.geo_decode),
                        y, x, getString(R.string.server_ak)));
                String decode = sendHttpRequest(String.format(getString(R.string.geo_decode),
                        y, x, getString(R.string.server_ak)));
                Log.d("decode", decode);
                JSONObject geoObject = new JSONObject(decode);
                String addr_info = geoObject.getJSONObject("result").
                        getString("formatted_address");
                Log.d("addr_info", addr_info);
                return addr_info;
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return "获取错误";
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
