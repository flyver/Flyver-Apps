package co.flyver.flyverrc;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.zerokol.views.JoystickView;

import co.flyver.Client.Client;
import co.flyver.IPC.IPCKeys;

public class MainControl extends Activity implements SensorEventListener {

    /* CONSTANTS */

    private static final String BUTTONS = "BUTTONS";
    private static final String GENERAL = "GENERAL";
    private static final float STEP = 1;
    private static final String JOYSTICK = "JOYSTICK";
    private static final int RESULT_SETTINGS = 1;
    private static final String _DEFAULT = "0";

    /* END OF CONSTANTS */

    public static void setServerIP() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getMainCtrlContext());
        Log.d(GENERAL, "Server ip is: " + sharedPreferences.getString("serverIp", _DEFAULT ));
        MainControl.sServerIP = sharedPreferences.getString("serverIp", "");
    }

    private static String sServerIP;
    SensorManager mSensorManager;
    private boolean mSendData = false;
    private boolean mEmergencyStarted = false;
    private static Context sMainControlContext;
    public static Toast lastToast = null;
    public static Context getMainCtrlContext() {
        return sMainControlContext;
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
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
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

    public static void applySettings() {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getMainCtrlContext());

        Log.d(GENERAL, "Server ip is: " + sharedPreferences.getString("serverIp", _DEFAULT ));
        sServerIP = sharedPreferences.getString("serverIp", "");
        Client.setServerIP(sServerIP);

        Log.d(GENERAL, "Yaw pid coefficients: Proportional: " + sharedPreferences.getString("proportionalY", _DEFAULT) + " Integral: " + sharedPreferences.getString("integralY", _DEFAULT) + " Derivative: " + sharedPreferences.getString("derivativeY", _DEFAULT));
        Client.serialize(IPCKeys.PIDYAW, Float.parseFloat(sharedPreferences.getString("proportionalY", _DEFAULT)),
                Float.parseFloat(sharedPreferences.getString("integralY", _DEFAULT)),
                Float.parseFloat(sharedPreferences.getString("derivativeY", _DEFAULT)));

        Log.d(GENERAL, "Pitch pid coefficients: Proportional: " + sharedPreferences.getString("proportionalP", _DEFAULT) + " Integral: " + sharedPreferences.getString("integralP", _DEFAULT) + " Derivative: " + sharedPreferences.getString("derivativeP", _DEFAULT));
        Client.serialize(IPCKeys.PIDPITCH, Float.parseFloat(sharedPreferences.getString("proportionalP", _DEFAULT)),
                Float.parseFloat(sharedPreferences.getString("integralP", _DEFAULT)),
                Float.parseFloat(sharedPreferences.getString("derivativeP", _DEFAULT)));

        Log.d(GENERAL, "Roll pid coefficients: Proportional: " + sharedPreferences.getString("proportionalR", _DEFAULT) + " Integral: " + sharedPreferences.getString("integralR", _DEFAULT) + " Derivative: " + sharedPreferences.getString("derivativeR", _DEFAULT));
        Client.serialize(IPCKeys.PIDROLL, Float.parseFloat(sharedPreferences.getString("proportionalR", _DEFAULT)),
                Float.parseFloat(sharedPreferences.getString("integralR", _DEFAULT)),
                Float.parseFloat(sharedPreferences.getString("derivativeR", _DEFAULT)));
    }

    Runnable mClientCallback = new Runnable() {
        @Override
        public void run() {
            applySettings();
        }
    };

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
                    Client.serialize(IPCKeys.COORDINATES, event.values[0], event.values[2], event.values[1]);
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
                        Client.serialize(IPCKeys.COORDINATES, 0, 0, 0);
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
                finalImageButton1.setImageResource(R.drawable.connect_btn_on);
                Intent intent = new Intent(getApplicationContext(), Client.class);
                startService(intent);
                Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if(!Client.isServerStarted()) {
                            finalImageButton1.setImageResource(R.drawable.connect_btn_off);
                        }
                    }
                };
                handler.postDelayed(runnable, 1000);
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
                    Client.serialize(IPCKeys.EMERGENCY, "start");
                    if(lastToast != null) {
                        lastToast.cancel();
                    }
                    lastToast = Toast.makeText(getApplicationContext(), "Emergency state enabled", Toast.LENGTH_SHORT);
                    finalImageButton.setImageResource(R.drawable.emergency_btn_on);
                    lastToast.show();
                    mEmergencyStarted = true;
                } else {
                    Client.serialize(IPCKeys.EMERGENCY, "stop");
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
                Client.serialize(IPCKeys.PICTURE, IPCKeys.PICREADY, 1);
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
                        Client.serialize(IPCKeys.THROTTLE, IPCKeys.INCREASE, STEP * steps);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case JoystickView.BOTTOM : {
                        Log.d(JOYSTICK, "Throttle decreased with " + (STEP * steps) + "steps");
                        Client.serialize(IPCKeys.THROTTLE, IPCKeys.DECREASE, STEP * steps);
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
                        Client.serialize(IPCKeys.YAW, IPCKeys.INCREASE, STEP * steps);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                    case JoystickView.RIGHT: {
                        Log.d(JOYSTICK, "Yaw decreased with " + (STEP * steps) + "steps");
                        Client.serialize(IPCKeys.YAW, IPCKeys.DECREASE, STEP * steps);
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
        Client.setOnInitCallback(mClientCallback);
        Client.setLastToast(lastToast);
        Client.setMainContext(sMainControlContext);
        setServerIP();
        IntentFilter intentFilter = new IntentFilter("co.flyver.UPDATE");
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                byte[] data = intent.getByteArrayExtra("bitmap");
                Log.d(GENERAL, "Received data");
                ImageView imageView = (ImageView) findViewById(R.id.pic);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(data, 0, data.length));
            }
        }, intentFilter);
    }
}
