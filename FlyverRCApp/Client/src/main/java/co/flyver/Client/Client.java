package co.flyver.Client;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;

import co.flyver.IPC.IPCContainers;


/**
 * Created by Petar Petrov on 10/1/14.
 */
public class Client extends IntentService {

    private static final String CLIENT = "CLIENT";
    private static final String INTENT = "INTENT";
    private static Gson sGson = new Gson();
    private static PrintWriter sStreamToServer;
    private static BufferedReader sStreamFromServer;
    private static Toast sLastToast;
    private static String sServerIP;
    private static Context sMainContext;
    private static boolean sServerStarted = false;
    private static Runnable sPreInitCallback;
    private static Runnable sPostInitCallback;
    private static IPCContainers.JSONQuadruple<String, Float, Float, Float> sJsonQuadruple = new IPCContainers.JSONQuadruple<>();
    private static IPCContainers.JSONTriple<String, String, Float> sJsonTriple = new IPCContainers.JSONTriple<>();
    private static IPCContainers.JSONTuple<String, String> jsonTuple = new IPCContainers.JSONTuple<>();
    private static IPCContainers.JSONTriple<String, String, String> sJsonPicture = new IPCContainers.JSONTriple<>();

    public static void setOnInitCallback(Runnable sOnInitCallback) {
        Client.sPreInitCallback = sOnInitCallback;
    }
    public static void setServerIP(String sServerIP) {
        Client.sServerIP = sServerIP;
    }
    public static void setLastToast(Toast sLastToast) {
        Client.sLastToast = sLastToast;
    }
    public static void setMainContext(Context sMainContext) {
        Client.sMainContext = sMainContext;
    }
    public static boolean isServerStarted() {
        return sServerStarted;
    }
    public static IPCContainers.JSONTriple<String, String, String> getsJsonPicture() {
        return sJsonPicture;
    }

    public Client() {
        super("Client");
    }

    private static void displayToast(String message) {
        if(sLastToast != null) {
           sLastToast.cancel();
        }
        sLastToast = Toast.makeText(sMainContext, message, Toast.LENGTH_SHORT);
        sLastToast.show();
    }

    /**
     * Opens the sockets and streams to the server
     */
    private void init() {
        try {
            Socket mConnection = new Socket(sServerIP, 51342);
            sStreamToServer = new PrintWriter(mConnection.getOutputStream(), true);
            sStreamFromServer = new BufferedReader(new InputStreamReader(mConnection.getInputStream()));
            Log.d(CLIENT, "Connection initialized");

            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    displayToast("Connected");
                    sServerStarted = true;
                    Looper.loop();
                }
            }.start();

            new Thread() {
                @Override
                public void run() {
                    //noinspection InfiniteLoopStatement
                    while(true) {
                        try {
                            loop();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

        } catch (IOException e) {

            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    displayToast("Server is not started");
                    Looper.loop();
                }
            }.start();

            e.printStackTrace();
        }
    }

    /**
     * Serialize the data into a JSON message with a key and three float values
     * Common usage is for PID coefficients
     * Refer to IPC class for keys
     * @param key - key of the PID that is to be modified
     * @param value1 - Proportional part of the PID
     * @param value2 - Integral part of the PID
     * @param value3 - Derivative part of the PID
     */
    public static void serialize(String key, float value1, float value2, float value3) {
        if(!sServerStarted) {
            displayToast("Not connected to a server");
            return;
        }
        Type type = new TypeToken<IPCContainers.JSONQuadruple<String, Float, Float, Float>>() {}.getType();
        sJsonQuadruple = new IPCContainers.JSONQuadruple<>(key, value1, value2, value3);
        String json = IPCContainers.JSONUtils.serialize(sJsonQuadruple, type);
        sStreamToServer.println(json);
        sStreamToServer.flush();
        Log.i(CLIENT, json);

    }

    /**
     * Serialize the data into a JSON message
     * Commonly used actions are:
     * Yaw increase/decrease
     * Pitch increase/decrease
     * Roll increase/decrease
     * Throttle increase/decrease
     * For more info on the parameters see the IPC class
     * @param key - String that is the key of the action, eq: throttle
     * @param action - String describing the action, eq: increase
     * @param value - Value of the action, eq: 0.1
     */
    public static void serialize(String key, String action, float value) {
        if(!sServerStarted) {
            displayToast("Not connected to a server");
            return;
        }
        Type type = new TypeToken<IPCContainers.JSONTriple<String, String, Float>>() {}.getType();
        sJsonTriple = new IPCContainers.JSONTriple<>(key, action, value);
        String mJson = IPCContainers.JSONUtils.serialize(sJsonTriple, type);
        sStreamToServer.println(mJson);
        sStreamToServer.flush();
        Log.i(CLIENT, mJson);
    }


    /**
     * Serialize the data into JSON message
     * @param key
     * @param value
     */
    public static void serialize(String key, String value) {
        if(!sServerStarted) {
            displayToast("Not connected to a server");
            return;
        }
        Type type = new TypeToken<IPCContainers.JSONTuple<String, String>>() {}.getType();
        jsonTuple = new IPCContainers.JSONTuple<>(key, value);
        String mJson = IPCContainers.JSONUtils.serialize(jsonTuple, type);
        sStreamToServer.println(mJson);
        sStreamToServer.flush();
        Log.i(CLIENT, mJson);
    }

    private void loop() throws IOException {
        Log.d(CLIENT, "Waiting from input from the server");
        String mJson;
        mJson = sStreamFromServer.readLine();
        Type type = new TypeToken<IPCContainers.JSONTriple<String, String, String>>() {}.getType();
        sJsonPicture = IPCContainers.JSONUtils.deserialize(mJson, type);
        byte[] pic = Base64.decode(sJsonPicture.getValue(), Base64.DEFAULT);
        if(mJson.length() > 0) {
            Intent intent = new Intent();
            intent.setAction("co.flyver.UPDATE");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("bitmap", pic);
            sendBroadcast(intent);
        }
    }

    @Override
    /**
     * Entry point for the client
     */
    protected void onHandleIntent(Intent intent) {
        Log.d(INTENT, "Intent received ");
        if (sPreInitCallback != null) {
            sPreInitCallback.run();
        } else {
            throw new IllegalStateException("PreInit callback is missing");
        }
        init();
        if (sPostInitCallback != null) {
            sPostInitCallback.run();
        }
    }
}
