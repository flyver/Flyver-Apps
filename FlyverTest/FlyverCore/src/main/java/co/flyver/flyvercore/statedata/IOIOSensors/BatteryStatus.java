package co.flyver.flyvercore.statedata.IOIOSensors;

import android.media.AudioManager;
import android.media.ToneGenerator;

import co.flyver.flyvercore.maincontrollers.MainController;
import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Created by Tihomir Nedev on 15-2-13.
 */
public class BatteryStatus extends IOIOSensor{

    private AnalogInput batteryInput;
    private float batteryVoltage;
    private int batteryPin;
    int batteryStatus;
    int currentStatus;
    float maxVoltage;
    float minVoltage;
    BatteryCells batteryCells;
    long timer;



    /* Constants */
    final float cellMaxVoltage = 4.2f;
    final float cellMinVoltage = 3.3f;
    private final  float dividerCoefficient = 6.94f;
    private int CHECKING_TIME = 3*1000; // Check for battery status every 3 sec
    /* End of */

    public BatteryStatus(String TOPIC, int batteryPin, BatteryCells batteryCells) {
        super(TOPIC);
        this.batteryPin = batteryPin;
        this.batteryCells = batteryCells;

         maxVoltage = cellMaxVoltage*batteryCells.getValue()/dividerCoefficient;
         minVoltage = cellMinVoltage*batteryCells.getValue()/dividerCoefficient;
         timer = System.currentTimeMillis();

    }

    @Override
    public void setup(IOIO ioio_) throws ConnectionLostException {
        batteryInput = ioio_.openAnalogInput(batteryPin);
    }

    @Override
    public void process(IOIO ioio_) throws ConnectionLostException, InterruptedException {
        batteryVoltage = batteryInput.getVoltage();
        calculateStatus();
    }

    private void calculateStatus(){
        currentStatus = (int)(((batteryVoltage - minVoltage) / (maxVoltage - minVoltage))*100);

        if (currentStatus!=batteryStatus){
            batteryStatus = currentStatus;

            if (System.currentTimeMillis() - timer > CHECKING_TIME){
                sendData(batteryStatus); // Send to message queue
                timer = System.currentTimeMillis();

                if(batteryStatus<1){
                    // Battery protection. When battery gets dangerously low. Stop the copter.
                    MainController.getInstance().emergencyStop("Drained Battery");
                    ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
                    toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                }
            }

        }
    }
    public enum BatteryCells {
        TWO(2),
        THREE(3),
        FOUR(4),
        FIVE(5),
        SIX(6);

        private int count;
        private BatteryCells(int count) {
            this.count = count;
        }
        public int getValue() {
            return count;
        }
    }
}
