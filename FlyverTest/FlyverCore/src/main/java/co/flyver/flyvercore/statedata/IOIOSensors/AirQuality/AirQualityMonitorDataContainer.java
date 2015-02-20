package co.flyver.flyvercore.statedata.IOIOSensors.AirQuality;

import co.flyver.flyvercore.statedata.DroneLocation;

/**
 * Created by Tihomir Nedev on 15-1-21.
 */
public class AirQualityMonitorDataContainer {

    public AirQualityMonitorDataContainer(int airQuality, DroneLocation droneLocation, long timeStamp) {
        this.airQuality = airQuality;
        this.droneLocation = droneLocation;
        this.timeStamp = timeStamp;
    }

    public int airQuality;
    public DroneLocation droneLocation = new DroneLocation();
    public long timeStamp;

    public String toString(){
        return droneLocation.getLatitude() +
                ", " +
                droneLocation.getLongitude() +
                ", " +
                droneLocation.getGpsAltitude() +
                 ", " +
                airQuality +
                ", " +
                timeStamp +
                " \n";
    }
}
