package co.flyver.Client;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import co.flyver.IPC.JSONUtils;

import static co.flyver.IPC.Tuples.Triple;
import static co.flyver.IPC.SharedIPCKeys.*;


/**
 * Created by flyver on 12/15/14.
 */
public class JoystickListener implements Runnable {
    private static final float STEP = 1;
    boolean active = false;
    float x;
    float y;
    Context context;
    Intent jsonIntent = new Intent("co.flyver.SENDJSON");

    private enum Change {
        INCREASE,
        DECREASE
    }

    private JoystickListener() {
        //no default constructor
    }

    public JoystickListener(Context context) {
        this.context = context;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setX(float x) {
        this.x = x;
    }
    public void setY(float y) {
        this.y = y;
    }

    private void changeThrottle(int steps, Change change) {
        Triple<String, String, Float> jsonTriple;
        Type type = new TypeToken<Triple<String, String, Float>>() {}.getType();
        switch (change) {
            case INCREASE:
                jsonTriple = new Triple<>(THROTTLE, INCREASE, STEP * steps);
                Client.sendMsg(JSONUtils.serialize(jsonTriple, type));
                jsonIntent.putExtra("json", JSONUtils.serialize(jsonTriple, type));
                context.sendBroadcast(jsonIntent);
                break;
            case DECREASE:
                jsonTriple = new Triple<>(THROTTLE, DECREASE, STEP * steps);
                Client.sendMsg(JSONUtils.serialize(jsonTriple, type));
                jsonIntent.putExtra("json", JSONUtils.serialize(jsonTriple, type));
                context.sendBroadcast(jsonIntent);
                break;
        }
    }

    private void changeYaw(int steps, Change change) {
        Triple<String, String, Float> jsonTriple;
        Type type = new TypeToken<Triple<String, String, Float>>() {}.getType();
        switch (change) {
            case INCREASE:
                jsonTriple = new Triple<>(YAW, INCREASE, STEP * steps);
                Client.sendMsg(JSONUtils.serialize(jsonTriple, type));
                jsonIntent.putExtra("json", JSONUtils.serialize(jsonTriple, type));
                context.sendBroadcast(jsonIntent);
                break;
            case DECREASE:
                jsonTriple = new Triple<>(YAW, DECREASE, STEP * steps);
                Client.sendMsg(JSONUtils.serialize(jsonTriple, type));
                jsonIntent.putExtra("json", JSONUtils.serialize(jsonTriple, type));
                context.sendBroadcast(jsonIntent);
                break;
        }
    }
    public boolean sendData() {
        String xStr = Float.toString(x);
        String yStr = Float.toString(y);
        Log.d("asd", String.format("yStr: %s, xStr: %s", yStr, xStr));
        yStr = yStr.substring(yStr.indexOf("."));
        xStr = xStr.substring(xStr.indexOf("."));

        if (x > 0) {
            if (x == 1) {
                changeYaw(10, Change.DECREASE);
                return true;
            }
            changeYaw(Integer.parseInt(String.valueOf(xStr.charAt(2))), Change.DECREASE);
        } else if (x < 0) {
            if (x == -1) {
                changeYaw(10, Change.INCREASE);
                return true;
            }
            changeYaw(Integer.parseInt(String.valueOf(xStr.charAt(3))), Change.INCREASE);
        }

        if (y > 0) {
            if (y == 1) {
                changeThrottle(10, Change.DECREASE);
                return true;
            }
            changeThrottle(Integer.parseInt(String.valueOf(yStr.charAt(2))), Change.DECREASE);
        } else if (y < 0) {
            if (y == -1) {
                changeThrottle(10, Change.INCREASE);
                return true;
            }
            changeThrottle(Integer.parseInt(String.valueOf(yStr.charAt(3))), Change.INCREASE);
        }
        return false;
    }

    @Override
    public void run() {
        while(true) {
            if(active) {
                sendData();
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
