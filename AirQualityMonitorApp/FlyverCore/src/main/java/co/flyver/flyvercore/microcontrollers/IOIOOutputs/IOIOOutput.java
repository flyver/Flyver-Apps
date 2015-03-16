package co.flyver.flyvercore.microcontrollers.IOIOOutputs;

import co.flyver.flyvercore.maincontrollers.MainController;
import co.flyver.flyvercore.microcontrollers.IOIOOutputs.exceptions.OutputsListException;
import co.flyver.flyvercore.statedata.IOIOSensors.SensorsList;
import co.flyver.flyvercore.statedata.IOIOSensors.exceptions.SensorsListException;
import co.flyver.utils.flyvermq.FlyverMQMessage;
import co.flyver.utils.flyvermq.interfaces.FlyverMQConsumer;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Created by Tihomir Nedev on 15-2-17.
 */
abstract public class IOIOOutput{

    public IOIOOutput(){
        register();
    }

    public void register() {
        try {
            OutputsList.getInstance().addOutput(this);
        } catch (OutputsListException e) {
            e.printStackTrace();
        }
    }


    /**
     * @setup
     * This method initializes the sensor.
     * It is executed in the setup() method of IOIOController
     */
    abstract public void setup(IOIO ioio_) throws ConnectionLostException;

    /**
     * @process
     * This method loops the reading/writing to the IOIO Board
     * It is executed in loop() method of the IOIOController.
     */

    abstract public void process(IOIO ioio_) throws ConnectionLostException, InterruptedException;

}
