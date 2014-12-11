package co.flyver.flyverrc;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;
import com.zerokol.views.JoystickView;

import java.lang.reflect.Type;

import co.flyver.Client.Client;
import co.flyver.IPC.IPCKeys;
import co.flyver.IPC.JSONUtils;

import static co.flyver.IPC.IPCContainers.JSONQuadruple;
import static co.flyver.IPC.IPCContainers.JSONTriple;
import static co.flyver.IPC.IPCContainers.JSONTuple;
import static co.flyver.IPC.IPCKeys.PIDPITCH;
import static co.flyver.IPC.IPCKeys.PIDROLL;
import static co.flyver.IPC.IPCKeys.PIDYAW;

public class MainControl extends Activity implements SensorEventListener, Client.ConnectionHooks {

    /* CONSTANTS */

    private static final String BUTTONS = "BUTTONS";
    private static final String GENERAL = "GENERAL";
    private static final float STEP = 1;
    private static final String JOYSTICK = "JOYSTICK";
    private static final int RESULT_SETTINGS = 1;
    private static final String _DEFAULT = "0";

    /* END OF CONSTANTS */

    private static String sServerIP;
    SensorManager mSensorManager;
    private boolean mSendData = false;
    private boolean mEmergencyStarted = false;
    private static Context sMainControlContext;
    public static Toast lastToast = null;
    ServiceConnection serviceConnection;
    static Client client;

    BroadcastReceiver broadcastReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("bitmap");
            Log.d(GENERAL, "Received data");
            ImageView imageView = (ImageView) findViewById(R.id.pic);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
        }
    };
    IntentFilter intentFilter = new IntentFilter("co.flyver.UPDATE");

    //Generic containers and derived type objects
    private static JSONQuadruple<String, Float, Float, Float> jsonQuadruple = new JSONQuadruple<>();
    private static Type jsonQuadrupleTypes = new TypeToken<JSONQuadruple<String, Float, Float, Float>>() {}.getType();
    private static JSONTriple<String, String, Float> jsonTriple = new JSONTriple<>();
    private static Type jsonTripleTypes = new TypeToken<JSONTriple<String, String, Float>>() {}.getType();
    private static JSONTuple<String, String> jsonTuple = new JSONTuple<>();
    private static Type jsonTupleTypes = new TypeToken<JSONTuple<String, String>>() {}.getType();

    private SharedPreferences sharedPreferences;


    public static Context getMainCtrlContext() {
        return sMainControlContext;
    }

    public static void setServerIP() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getMainCtrlContext());
        Log.d(GENERAL, "Server ip is: " + sharedPreferences.getString("serverIp", _DEFAULT ));
        MainControl.sServerIP = sharedPreferences.getString("serverIp", "");
        Client.setServerIP(sServerIP);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        registerToSensors();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the strKey bar if it is present.
        getMenuInflater().inflate(R.menu.main_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle strKey bar item clicks here. The strKey bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_settings: {
                Intent i = new Intent(this, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
            }
            break;
        }
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_SETTINGS: {
                applySettings();
            }
        }
    }

    public void applySettings() {

        setServerIP();
        float p;
        float i;
        float d;

        String preferenceValue;

        preferenceValue = sharedPreferences.getString("proportionalY", _DEFAULT);
        p = Float.parseFloat(preferenceValue.equals("") ? String.valueOf(0) : preferenceValue);
        preferenceValue = sharedPreferences.getString("integralY", _DEFAULT);
        i = Float.parseFloat(preferenceValue.equals("") ? String.valueOf(0) : preferenceValue);
        preferenceValue = sharedPreferences.getString("derivativeY", _DEFAULT);
        d = Float.parseFloat(preferenceValue.equals("") ? String.valueOf(0) : preferenceValue);

        Log.d(GENERAL, "Yaw pid coefficients: Proportional: " + p + " Integral: " + i + " Derivative: " + d);
        jsonQuadruple = new JSONQuadruple<>(PIDYAW, p, i, d);
        Client.sendMsg(JSONUtils.serialize(jsonQuadruple, jsonQuadrupleTypes));

        preferenceValue = sharedPreferences.getString("proportionalP", _DEFAULT);
        p = Float.parseFloat(preferenceValue.equals("") ? String.valueOf(0) : preferenceValue);
        preferenceValue = sharedPreferences.getString("integralP", _DEFAULT);
        i = Float.parseFloat(preferenceValue.equals("") ? String.valueOf(0) : preferenceValue);
        preferenceValue = sharedPreferences.getString("derivativeP", _DEFAULT);
        d = Float.parseFloat(preferenceValue.equals("") ? String.valueOf(0) : preferenceValue);

        Log.d(GENERAL, "Pitch pid coefficients: Proportional: " + p + " Integral: " + i + " Derivative: " + d);
        jsonQuadruple = new JSONQuadruple<>(PIDPITCH, p, i, d);
        Client.sendMsg(JSONUtils.serialize(jsonQuadruple, jsonQuadrupleTypes));

        preferenceValue = sharedPreferences.getString("proportionalP", _DEFAULT);
        p = Float.parseFloat(preferenceValue.equals("") ? String.valueOf(0) : preferenceValue);
        preferenceValue = sharedPreferences.getString("integralP", _DEFAULT);
        i = Float.parseFloat(preferenceValue.equals("") ? String.valueOf(0) : preferenceValue);
        preferenceValue = sharedPreferences.getString("derivativeP", _DEFAULT);
        d = Float.parseFloat(preferenceValue.equals("") ? String.valueOf(0) : preferenceValue);
        Log.d(GENERAL, "Roll pid coefficients: Proportional: " + p + " Integral: " + i + " Derivative: " + d);
        jsonQuadruple = new JSONQuadruple<>(PIDROLL, p, i, d);
        Client.sendMsg(JSONUtils.serialize(jsonQuadruple, jsonQuadrupleTypes));
    }

    /**
     * Registers to the orientation sensors of the phone
     */
    public void registerToSensors() {
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor orientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //if the Start button is released, do not send data to the server
                if (mSendData) {
                    jsonQuadruple = new JSONQuadruple<>(IPCKeys.COORDINATES, event.values[0], event.values[2], event.values[1]);
                    Client.sendMsg(JSONUtils.serialize(jsonQuadruple, jsonQuadrupleTypes));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * Attaches onClick/onTouch event listeners to the relevant buttons
     */
    public void addListeners() {

        ImageButton imageButton;
        Button button;

        /* Start/Stop hold button */
        imageButton = (ImageButton) findViewById(R.id.holdstart);

        final ImageButton finalImageButton2 = imageButton;
        imageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN : {
                        mSendData = true;
                        finalImageButton2.setImageResource(R.drawable.start_btn_on);
                        Log.i(BUTTONS, "Holdstart pressed");
                    }
                    break;
                    case MotionEvent.ACTION_UP : {
                        mSendData = false;
                        finalImageButton2.setImageResource(R.drawable.start_btn);
                        jsonQuadruple = new JSONQuadruple<>(IPCKeys.COORDINATES, 0.0f, 0.0f, 0.0f);
                        Client.sendMsg(JSONUtils.serialize(jsonQuadruple, jsonQuadrupleTypes));
                        Log.i(BUTTONS, "Holdstart released");
                    }
                    break;
                    default : {
                    }
                }
                return false;
            }
        });

        /* End of Start/Stop */

        /* Connect Button */
        imageButton = (ImageButton) findViewById(R.id.connectButton);
        final ImageButton finalImageButton1 = imageButton;
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sServerIP == null || sServerIP.isEmpty()) {
                    if(lastToast != null) {
                        lastToast.cancel();
                    }
                    lastToast = Toast.makeText(getApplicationContext(), "Enter IP address", Toast.LENGTH_SHORT);
                    lastToast.show();
                    return;
                }
                Log.d(BUTTONS, "Server IP is: " + sServerIP);
                final Intent intent = new Intent(getApplicationContext(), Client.class);
                serviceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        Client.LocalBinder localBinder = (Client.LocalBinder) service;
                        client = localBinder.getInstance();
                        startService(intent);
                        Log.e("cl", "Server has been bound");
                    }
                    @Override
                    public void onServiceDisconnected(ComponentName name) {

                        Log.e("cl", "Server has been bound");
                    }
                };
                bindService(intent, serviceConnection, BIND_AUTO_CREATE);
            }
        });

        /* End of Connect button */

        /* Emergency stop button */

        imageButton= (ImageButton) findViewById(R.id.emergencyStop);
        final ImageButton finalImageButton = imageButton;
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mEmergencyStarted) {
                    jsonTuple = new JSONTuple<>(IPCKeys.EMERGENCY, "start");
                    Client.sendMsg(JSONUtils.serialize(jsonTuple, jsonTupleTypes));
                    if(lastToast != null) {
                        lastToast.cancel();
                    }
                    lastToast = Toast.makeText(getApplicationContext(), "Emergency state enabled", Toast.LENGTH_SHORT);
                    finalImageButton.setImageResource(R.drawable.emergency_btn_on);
                    lastToast.show();
                    mEmergencyStarted = true;
                } else {
                    jsonTuple = new JSONTuple<>(IPCKeys.EMERGENCY, "stop");
                    Client.sendMsg(JSONUtils.serialize(jsonTuple, jsonTupleTypes));
                    if(lastToast != null) {
                       lastToast.cancel();
                    }
                    lastToast = Toast.makeText(getApplicationContext(), "Emergency state disabled", Toast.LENGTH_SHORT);
                    finalImageButton.setImageResource(R.drawable.emergency_btn_off);
                    lastToast.show();
                    mEmergencyStarted = false;
                }
            }
        });

        /* End of emergency stop button */


        /* Take picture button */

        button = (Button) findViewById(R.id.takePicture);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                jsonTriple = new JSONTriple<>(IPCKeys.PICTURE, IPCKeys.PICREADY, 1.0f);
                Client.sendMsg(JSONUtils.serialize(jsonTriple, jsonTripleTypes));
            }
        });

        /* End of take picture button */

        /* Joystick view */

        JoystickView joystickView = (JoystickView) findViewById(R.id.joystickView);
        joystickView.setOnJoystickMoveListener(new JoystickView.OnJoystickMoveListener() {
            @Override
            public void onValueChanged(int angle, int power, int direction) {
                int steps = (int) Math.floor((double) power / 10);
                switch(direction) {
                    case JoystickView.FRONT : {
                        Log.d(JOYSTICK, "Throttle increased with " + (STEP * steps) + "steps");
//                        Client.serialize(IPCKeys.THROTTLE, IPCKeys.INCREASE, STEP * steps);
                        JSONTriple<String, String, Float> jsonTriple = new JSONTriple<>(IPCKeys.THROTTLE, IPCKeys.INCREASE, STEP * steps);
                        Type type = new TypeToken<JSONTriple<String, String, Float>>() {}.getType();
                        Client.sendMsg(JSONUtils.serialize(jsonTriple, type));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case JoystickView.BOTTOM : {
                        Log.d(JOYSTICK, "Throttle decreased with " + (STEP * steps) + "steps");
                        jsonTriple = new JSONTriple<>(IPCKeys.THROTTLE, IPCKeys.DECREASE, STEP * steps);
                        Client.sendMsg(JSONUtils.serialize(jsonTriple, jsonTripleTypes));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case JoystickView.BOTTOM_LEFT : {
                        Log.d(JOYSTICK, "Not implemented yet");
                    }
                    break;
                    case JoystickView.RIGHT_BOTTOM : {

                        Log.d(JOYSTICK, "Not implemented yet");
                    }
                    break;
                    case JoystickView.FRONT_RIGHT : {

                        Log.d(JOYSTICK, "Not implemented yet");
                    }
                    break;
                    case JoystickView.LEFT_FRONT: {

                        Log.d(JOYSTICK, "Not implemented yet");
                    }
                    break;
                    case JoystickView.LEFT: {
                        Log.d(JOYSTICK, "Yaw increased with " + (STEP * steps) + "steps");
                        jsonTriple = new JSONTriple<>(IPCKeys.YAW, IPCKeys.INCREASE, STEP * steps);
                        Client.sendMsg(JSONUtils.serialize(jsonTriple, jsonTripleTypes));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case JoystickView.RIGHT: {
                        Log.d(JOYSTICK, "Yaw decreased with " + (STEP * steps) + "steps");
                        jsonTriple = new JSONTriple<>(IPCKeys.YAW, IPCKeys.DECREASE, STEP * steps);
                        Client.sendMsg(JSONUtils.serialize(jsonTriple, jsonTripleTypes));
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                    default: {
                        Log.d(JOYSTICK, "Default!!");
                    }
                    break;
                }
            }
        }, JoystickView.DEFAULT_LOOP_INTERVAL);

        /* End of joystick view */
    }

    /**
     * Entry point
     * Registers event/broadcast listeners
     * Connects to a server
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainControl.sMainControlContext = getApplicationContext();
        setContentView(R.layout.activity_main_control);
        addListeners();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        setServerIP();
        Client.registerConnectionHooks(this);
        Client.registerCallback("battery", new Client.ClientCallback() {
            @Override
            public void run(String json) {
                TextView batteryStatus = (TextView) findViewById(R.id.batteryStatus);
                Type type = new TypeToken<JSONTuple<String, String>>() {
                }.getType();
                JSONTuple<String, String> status = JSONUtils.deserialize(json, type);
                batteryStatus.setText("Battery: " + status.getValue() + "%");
            }
        });
        Client.registerCallback("pidyaw", new Client.ClientCallback() {
            @Override
            public void run(String json) {
                JSONQuadruple<String, Float, Float, Float> pidyaw;
                Type type = new TypeToken<JSONQuadruple<String, Float, Float, Float>>() {}.getType();
                pidyaw = JSONUtils.deserialize(json, type);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("proportionalY", String.valueOf(pidyaw.getValue1()));
                editor.putString("integralY", String.valueOf(pidyaw.getValue2()));
                editor.putString("derivativeY", String.valueOf(pidyaw.getValue3()));
                editor.apply();
                Log.d("PID", json);
            }
        });
        Client.registerCallback("pidpitch", new Client.ClientCallback() {
            @Override
            public void run(String json) {
                Log.d("PID", json);
                JSONQuadruple<String, Float, Float, Float> pidpitch;
                Type type = new TypeToken<JSONQuadruple<String, Float, Float, Float>>() {}.getType();
                pidpitch = JSONUtils.deserialize(json, type);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("proportionalP", String.valueOf(pidpitch.getValue1()));
                editor.putString("integralP", String.valueOf(pidpitch.getValue2()));
                editor.putString("derivativeP", String.valueOf(pidpitch.getValue3()));
                editor.apply();
            }
        });
        Client.registerCallback("pidroll", new Client.ClientCallback() {
            @Override
            public void run(String json) {
                JSONQuadruple<String, Float, Float, Float> pidroll;
                Type type = new TypeToken<JSONQuadruple<String, Float, Float, Float>>() {}.getType();
                pidroll = JSONUtils.deserialize(json, type);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                editor.putString("proportionalR", String.valueOf(pidroll.getValue1()));
                editor.putString("integralR", String.valueOf(pidroll.getValue2()));
                editor.putString("derivativeR", String.valueOf(pidroll.getValue3()));
                editor.apply();
                Log.d("PID", json);
            }
        });
        Client.setLastToast(lastToast);
        Client.setMainContext(sMainControlContext);

        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(broadcastReceiver , intentFilter);
    }

    private boolean isServiceRunning(Class<?> service) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo serviceInfo : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (service.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onConnect() {
//        applySettings();
        ImageButton imageButton;
        imageButton = (ImageButton) findViewById(R.id.connectButton);
        final ImageButton finalImageButton1 = imageButton;
        finalImageButton1.setImageResource(R.drawable.connect_btn_on);
    }

    @Override
    public void onDisconnect() {
        ImageButton imageButton;
        imageButton = (ImageButton) findViewById(R.id.connectButton);
        final ImageButton finalImageButton1 = imageButton;
        finalImageButton1.setImageResource(R.drawable.connect_btn_off);
    }
}
