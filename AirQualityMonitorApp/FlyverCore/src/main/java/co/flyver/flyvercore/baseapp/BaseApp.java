package co.flyver.flyvercore.baseapp;

import android.app.Application;
import android.content.Context;

import co.flyver.flyvercore.maincontrollers.MainController;
import co.flyver.flyvercore.maincontrollers.MainControllerInstanceExisting;
import co.flyver.flyvercore.microcontrollers.IOIOOutputs.OutputsList;
import co.flyver.flyvercore.microcontrollers.IOIOOutputs.QuadCopterXIOIO;
import co.flyver.flyvercore.statedata.IOIOSensors.AirQuality.AirQualityMonitorProducer;
import co.flyver.flyvercore.statedata.IOIOSensors.AirQuality.AirQualitySensor;
import co.flyver.flyvercore.statedata.IOIOSensors.BatteryStatus;
import co.flyver.flyvercore.statedata.IOIOSensors.SensorsList;
import co.flyver.flyvercore.statedata.LocationServicesProvider;
import co.flyver.flyvercore.statedata.SensorsWrapper;
import co.flyver.httpserver.NanoHTTPDServer;
import co.flyver.utils.flyvermq.FlyverMQ;
import co.flyver.utils.webpage.WebpageUtils;
import fi.iki.elonen.ServerRunner;

/**
 * This class serves as a single entry point in the system.
 * It's constructor and methods are called only once at the application's startup
 * and are not called again on activity changes.
 * Static initializations that do not depend on the application's context should be
 * carried out in the constructor.
 * Initializations that depend on the context are carried out in the onCreate() method.
 * Also this class exposes the global application context
 *
 * Created by Petar Petrov on 2/5/15.
 */
public class BaseApp extends Application {
    public static Context applicationContext;
    private static QuadCopterXIOIO drone;


    public BaseApp() {
        super();

        FlyverMQ.start();
        SensorsList.create();
        OutputsList.create();
        drone = new QuadCopterXIOIO(34,35,36,37);
        try {
            new MainController(drone).start();
        } catch (MainControllerInstanceExisting mainControllerInstanceExisting) {
            mainControllerInstanceExisting.printStackTrace();
        }
        ServerRunner.run(NanoHTTPDServer.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        new SensorsWrapper(applicationContext).start();
        WebpageUtils.deployWebpage(applicationContext.getResources().getAssets());
        new LocationServicesProvider();
        new AirQualitySensor("sensors.airquality", 45);
        new AirQualityMonitorProducer();
        new BatteryStatus("battery.status",46, BatteryStatus.BatteryCells.THREE);
    }
}
