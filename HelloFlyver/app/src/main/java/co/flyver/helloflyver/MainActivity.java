package co.flyver.helloflyver;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import net.majorkernelpanic.streaming.gl.SurfaceView;

import java.util.Timer;
import java.util.TimerTask;

import co.flyver.flyvercore.MicroControllers.IOIOController;
import co.flyver.androidrc.Server.CameraProvider;
import co.flyver.androidrc.Server.Server;
import co.flyver.androidrc.Server.VideoStreamProvider;
import co.flyver.dataloggerlib.LocalDataActivity;
import co.flyver.dataloggerlib.LoggerTestActivity;
import co.flyver.dataloggerlib.SettingsActivity;
import co.flyver.flyvercore.DroneTypes.QuadCopterX;
import co.flyver.flyvercore.MainControllers.MainController;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIO.VersionType;
import ioio.lib.util.android.IOIOActivity;

public class MainActivity extends IOIOActivity {
    // TODO: Make Flyver abstraction for Activity

    public TextView stateView;
    private IOIOController microController;
    private MainController mainController;
    CameraProvider cameraProvider;
    QuadCopterX drone = new QuadCopterX();
    boolean microControllerReady = false;
    SurfaceView preview;
    static Server server;

    /**
     * It is called by the IOIO board when connection is established
     */
    @Override
    public IOIOController createIOIOLooper() {
        final IOIOController ic;
        ic = new IOIOController(drone).addConnectionHooks(new IOIOController.ConnectionHooks() {
            @Override
            public void onConnect(IOIOController ioioController) {
                microController = ioioController;
                microControllerReady = true;
                if (mainController != null) {
                    mainController.onIoioConnect();
                }
            }
            @Override
            public void onDisconnect() {
                mainController.onIoioDisconnect();
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

        preview = (SurfaceView) findViewById(R.id.campreview);

        cameraProvider = new VideoStreamProvider(getApplicationContext());
        cameraProvider.setView(preview);
        cameraProvider.init();

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
        TextView serverIp = (TextView) findViewById(R.id.serverIpText);
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        serverIp.setText(ipAddress.equals("0.0.0.0") ? "Server IP: 192.168.43.1" : "Server IP: " + ipAddress);


        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                if (microControllerReady) {
                    Looper.prepare();
                    startMainController();
                    timer.cancel();
                    timer.purge();
                    Looper.loop();
                }
            }
        }, 0, 1000);

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
    private void startMainController() {
        mainController = new MainController(this, microController, drone);

        try {
            mainController.start();
        } catch (Exception e) {
            Toast.makeText(this, "The USB transmission could not start.",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
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

        switch(id) {
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

}