package co.flyver.flyvercore.statedata.IOIOSensors;

import android.util.Log;

import ioio.lib.api.IOIO;
import ioio.lib.api.PulseInput;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Created by Tihomir Nedev on 15-2-13.
 *
 * Supported sensors:
 *   * HC-SR04
 *
 */
public class UsRangeFinder extends IOIOSensor{

    private int triggerPin;
    private int echoPin;
    float distance;
    private PwmOutput trigger; // Ultrasonic range finder trigger
    private PulseInput echo;  // Ultrasoni range finder echo

    public UsRangeFinder(String TOPIC, int triggerPin, int echoPin) {
        super(TOPIC);
        this.triggerPin = triggerPin;
        this.echoPin = echoPin;

    }

    @Override
    public void setup(IOIO ioio_) throws ConnectionLostException {

        echo = ioio_.openPulseInput(echoPin, PulseInput.PulseMode.POSITIVE);
        trigger = ioio_.openPwmOutput(triggerPin,16);

    }

    @Override
    public void process(IOIO ioio_) throws ConnectionLostException, InterruptedException {

        trigger.setPulseWidth(8000);
        distance = (echo.getDuration() * 1000 * 1000 ) / 29f / 2f;
        if (distance>2 && distance < 700){
            sendData(distance);
        }

    }
}
