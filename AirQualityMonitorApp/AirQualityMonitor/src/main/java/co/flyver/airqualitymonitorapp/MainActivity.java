package co.flyver.airqualitymonitorapp;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import co.flyver.AirQualityMonitorApp.R;
import co.flyver.androidrc.server.CameraProvider;
import co.flyver.androidrc.server.Server;
import co.flyver.dataloggerlib.LocalDataActivity;
import co.flyver.dataloggerlib.LoggerTestActivity;
import co.flyver.flyvercore.maincontrollers.MainController;
import co.flyver.flyvercore.microcontrollers.IOIOController;
import co.flyver.flyvercore.statedata.IOIOSensors.AirQuality.AirQualityMonitorDataContainer;
import co.flyver.flyvercore.statedata.IOIOSensors.AirQuality.AirQualityMonitorProducer;
import co.flyver.utils.flyvermq.FlyverMQ;
import co.flyver.utils.flyvermq.FlyverMQMessage;
import co.flyver.utils.flyvermq.exceptions.FlyverMQException;
import co.flyver.utils.flyvermq.interfaces.FlyverMQConsumer;
import co.flyver.utils.settings.SettingsActivity;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO.VersionType;
import ioio.lib.util.android.IOIOActivity;

public class MainActivity extends IOIOActivity {
    // TODO: Make Flyver abstraction for Activity

    public TextView stateView;
    CameraProvider cameraProvider;
    static Server server;

    /**
     * It is called by the IOIO board when connection is established
     */
    @Override
    public IOIOController createIOIOLooper() {
        final IOIOController ic;
        ic = new IOIOController().addConnectionHooks(new IOIOController.ConnectionHooks() {
            @Override
            public void onConnect(IOIOController ioioController) {
                    MainController.getInstance().onIoioConnect(ioioController);
            }

            @Override
            public void onDisconnect() {
                MainController.getInstance().onIoioDisconnect();
            }
        });
        return ic;
    }

    /**
     * Called when the activity is first created. Here we normally initialize
     * our GUI.
     */
    @TargetApi(Build.VERSION_CODES.CUPCAKE)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // Keep the screen on

//        final PendingIntent watchdog = PendingIntent.getActivity(getApplication().getBaseContext(), 0, new Intent(getIntent()), PendingIntent.FLAG_UPDATE_CURRENT);
//        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
//            @Override
//            public void uncaughtException(Thread thread, Throwable ex) {
//                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, watchdog);
//                ex.printStackTrace();
//                System.exit(2);
//            }
//        });

        final Intent intent = new Intent(getApplicationContext(), Server.class);
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Server.LocalBinder localBinder = (Server.LocalBinder) service;
                server = localBinder.getServerInstance();
                server.setCameraProvider(cameraProvider);
                startService(intent);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            }
        };
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    public void onStart() {
        super.onStart();
        monitorAirQuality();
    }

    private void showVersions(IOIO ioio, String title) {
        toast(String.format("%s\n" +
                        "IOIOLib: %s\n" +
                        "Application firmware: %s\n" +
                        "Bootloader firmware: %s\n" +
                        "Hardware: %s",
                title,
                ioio.getImplVersion(VersionType.IOIOLIB_VER),
                ioio.getImplVersion(VersionType.APP_FIRMWARE_VER),
                ioio.getImplVersion(VersionType.BOOTLOADER_VER),
                ioio.getImplVersion(VersionType.HARDWARE_VER)));
    }

    private void toast(final String message) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setDebugText(String debugText) {
        stateView.setText(debugText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
        //return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        Intent intent;

        int id = item.getItemId();

        switch (id) {
            case R.id.action_logger:
                intent = new Intent(this, LoggerTestActivity.class);
                this.startActivity(intent);
                break;
            case R.id.action_loggerlocaldata:
                intent = new Intent(this, LocalDataActivity.class);
                this.startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void monitorAirQuality() {
        FlyverMQConsumer consumer = new FlyverMQConsumer() {
            @Override
            public void dataReceived(FlyverMQMessage message) {
                ImageView imageView = (ImageView) findViewById(R.id.scaleView);
                AirQualityMonitorDataContainer container = (AirQualityMonitorDataContainer) message.data;
                int quality = container.airQuality;

                switch (quality / 16) {
                    case 1: {
                        setImage(imageView, R.drawable.scale1);
                    }
                    break;
                    case 2: {
                        setImage(imageView, R.drawable.scale2);
                    }
                    break;
                    case 3: {
                        setImage(imageView, R.drawable.scale3);
                    }
                    break;
                    case 4: {
                        setImage(imageView, R.drawable.scale4);
                    }
                    break;
                    case 5: {
                        setImage(imageView, R.drawable.scale5);
                    }
                    break;
                    case 6: {
                        setImage(imageView, R.drawable.scale6);
                    }
                    break;
                    default: {
                        //empty
                    }
                }
            }

            @Override
            public void unregistered() {

            }

            @Override
            public void paused() {

            }

            @Override
            public void resumed() {

            }
            private void setImage(final ImageView imageView, final int resId) {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageResource(resId);
                    }
                });
            }
        };
        try {
            FlyverMQ.getInstance().registerConsumer(consumer, AirQualityMonitorProducer.TOPIC);
        } catch (FlyverMQException e) {
            e.printStackTrace();
        }
    }
}