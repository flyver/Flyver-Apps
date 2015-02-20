package co.flyver.flyvercore.microcontrollers;

import co.flyver.flyvercore.microcontrollers.IOIOOutputs.IOIOOutput;
import co.flyver.flyvercore.microcontrollers.IOIOOutputs.OutputsList;
import co.flyver.flyvercore.microcontrollers.IOIOOutputs.exceptions.OutputsListException;
import co.flyver.flyvercore.statedata.IOIOSensors.IOIOSensor;
import co.flyver.flyvercore.statedata.IOIOSensors.SensorsList;
import co.flyver.flyvercore.statedata.IOIOSensors.exceptions.SensorsListException;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

/**
 * IOIOController is used to setup and loop the IOIO OTG Board.
 * In here all inputs and outputs of the board shall be implemented.
 * This IOIOController is used only with QuadCopterX of Drone type.
 * A new IOIOController or MicroController shall be made for each new Drone type.
 */
public class IOIOController extends BaseIOIOLooper implements MicroController {

    public interface ConnectionHooks {
        /**
         * This method will be called every time the board connects and reconnects
         *
         * @param ioioController
         */
        public void onConnect(IOIOController ioioController);

        public void onDisconnect();
    }

    public boolean ioioref = false;
    private ConnectionHooks connectionHooks;


    public IOIOController addConnectionHooks(ConnectionHooks hooks) {
        this.connectionHooks = hooks;
        return this;
    }

    @Override
    public void setup() throws ConnectionLostException {
        ioioref = true;

        if (connectionHooks != null) {
            connectionHooks.onConnect(this);
        }

        try {
            for (IOIOOutput output : OutputsList.getInstance().getOutputs()){
                output.setup(ioio_);
            }
            for (IOIOSensor sensor : SensorsList.getInstance().getSensors()) {
                sensor.setup(ioio_);
            }
        } catch (SensorsListException e) {
            e.printStackTrace();
        } catch (OutputsListException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called repetitively while the IOIO is connected.
     *
     * @throws ConnectionLostException When IOIO connection is lost.
     * @throws InterruptedException    When the IOIO thread has been interrupted.
     * @see ioio.lib.util.IOIOLooper#loop()
     */
    @Override
    public void loop() throws InterruptedException, ConnectionLostException {
        try {

             for (IOIOOutput output : OutputsList.getInstance().getOutputs()){
               output.process(ioio_);
             }
            for (IOIOSensor sensor : SensorsList.getInstance().getSensors()) {
                sensor.process(ioio_);
            }
            Thread.sleep(1);
        } catch (InterruptedException e) {
            ioio_.disconnect();
        } catch (ConnectionLostException e) {
            throw e;
        } catch (SensorsListException e) {
            e.printStackTrace();
        } catch (OutputsListException e) {
            e.printStackTrace();
        }

    }

    /**
     * Called when the IOIO is disconnected.
     *
     * @see ioio.lib.util.IOIOLooper#disconnected()
     */
    @Override
    public void disconnected() {
        if (connectionHooks != null) {
            connectionHooks.onDisconnect();
        }
    }

    /**
     * Called when the IOIO is connected, but has an incompatible firmware version.
     *
     * @see ioio.lib.util.IOIOLooper#incompatible(ioio.lib.api.IOIO)
     */
    @Override
    public void incompatible() {

    }

}