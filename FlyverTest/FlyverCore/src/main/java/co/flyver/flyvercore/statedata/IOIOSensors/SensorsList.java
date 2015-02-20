package co.flyver.flyvercore.statedata.IOIOSensors;

import java.util.LinkedList;

import co.flyver.flyvercore.statedata.IOIOSensors.exceptions.SensorsListException;

/**
 * Created by Petar Petrov on 1/20/15.
 */
public class SensorsList {
    static SensorsList instance;

    LinkedList<IOIOSensor> sensors = new LinkedList<>();

    public void addSensor(IOIOSensor ioioSensor) {
        sensors.add(ioioSensor);
    }

    public LinkedList<IOIOSensor> getSensors() {
        return sensors;
    }

    public static SensorsList getInstance() throws SensorsListException {
        if(instance == null) {
            throw new SensorsListException("Sensors list not created. Call SensorsList.create() first");
        }
        return instance;
    }

    public static void create() {
        instance = new SensorsList();
    }
}
