package co.flyver.flyvercore.statedata.IOIOSensors.AirQuality;

import co.flyver.flyvercore.statedata.IOIOSensors.IOIOSensor;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Created by Tihomir Nedev on 15-1-19.
 */
public class AirQualitySensor extends IOIOSensor {

    private final float SENSITIVITY_COEFFICIENT = 3f; // Increases the sensitivity of the sensor


    private int sensorReading;
    private int sensorReadingMark;
    private int sensorPin;
    private long initTimeStamp;
    private boolean timeMark;
    private float rawSensorReading;
    AnalogInput analogInput;

    /*
         This type of sensors needs about 3 minutes to heat up and produce stable values
         initTimeStamp will store the time since the app started
         timeMark will be used as an initial wait for the sensor to heat up
    */


    public AirQualitySensor(String TOPIC, int sensorPin) {
        super(TOPIC);
        this.sensorPin = sensorPin;
        initTimeStamp = System.currentTimeMillis();
    }

    @Override
    public void setup(IOIO ioio_){
        try {
            analogInput = ioio_.openAnalogInput(sensorPin);

        } catch (ConnectionLostException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void process(IOIO ioio_) {
        try {

            rawSensorReading = analogInput.read();

            if(rawSensorReading > 0.0) { // Check if there is a reading

                if (timeMark) {
                    sensorReading = (int) (rawSensorReading * SENSITIVITY_COEFFICIENT * 100); //Read the input and convert it to 0-100 %
                } else {
                    if (System.currentTimeMillis() - initTimeStamp > 1000 * 60 * 3) { // Wait 3 minutes before reading data //correct it!
                        timeMark = true;
                    }
                }
                if (sensorReading <= 100) { // Saturate for values over 100
                    sendData(sensorReading);   // Send to mq
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ConnectionLostException e) {
            e.printStackTrace();
        }
    }
}
