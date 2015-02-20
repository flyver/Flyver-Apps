package co.flyver.flyvercore.statedata;

import co.flyver.flyvercore.maincontrollers.MainController;
import co.flyver.utils.flyvermq.FlyverMQMessage;
import co.flyver.utils.flyvermq.interfaces.FlyverMQConsumer;

/**
 * Created by Tihomir Nedev on 15-1-9.
 * This class is for test purposes only
 */
public class LocationServicesSubsciber implements FlyverMQConsumer {

    /* Constants */
    public static String TOPIC = "LocationServices";
    DroneLocation droneLocation;
    /* End of */

    public LocationServicesSubsciber(){
        MainController.getInstance().getMessageQueue().registerConsumer(this, TOPIC);
    }
    @Override
    public void dataReceived(FlyverMQMessage message) {
        droneLocation = (DroneLocation) message.data;
       //Uncomment to log location Log.i("location", droneLocation.toString());
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
}
