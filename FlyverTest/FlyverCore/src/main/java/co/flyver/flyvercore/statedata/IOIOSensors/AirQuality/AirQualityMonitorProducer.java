package co.flyver.flyvercore.statedata.IOIOSensors.AirQuality;

import android.os.Environment;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import co.flyver.flyvercore.maincontrollers.MainController;
import co.flyver.flyvercore.statedata.DroneLocation;
import co.flyver.utils.flyvermq.FlyverMQMessage;
import co.flyver.utils.flyvermq.FlyverMQProducer;
import co.flyver.utils.flyvermq.exceptions.ProducerAlreadyRegisteredException;
import co.flyver.utils.flyvermq.interfaces.FlyverMQConsumer;

/**
 * Created by Tihomir Nedev on 15-1-21.
 */
public class AirQualityMonitorProducer extends FlyverMQProducer implements FlyverMQConsumer {

    /* Constants */
    public static String TOPIC = "airquality.mapped";
    public static String LOCATIONTOPIC = "LocationServices";
    public static String AIRQUALITYTOPIC = "sensors.airquality";
    public static String FILENAME = "airQualityData.csv";
    public static int DATABUFFERSIZE = 20;

    /* End of */

    AirQualityMonitorDataContainer airQualityMonitorDataContainer;
    DroneLocation droneLocation;
    boolean locationMarker;
    int airQualityData;
    ArrayList <String> data = new ArrayList<String>();

    public AirQualityMonitorProducer() {
        super("airquality.mapped");

        MainController.getInstance().getMessageQueue().registerConsumer(this, LOCATIONTOPIC);
        MainController.getInstance().getMessageQueue().registerConsumer(this, AIRQUALITYTOPIC);
        try {
            register(false);
        } catch (ProducerAlreadyRegisteredException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dataReceived(FlyverMQMessage message) {

        if (message.topic.equals(LOCATIONTOPIC)){
            droneLocation = (DroneLocation) message.data;
            locationMarker = true;
        }
        else if (message.topic.equals(AIRQUALITYTOPIC)){
            airQualityData = (Integer) message.data;
            if(droneLocation != null && locationMarker ) {
                buildData();
                locationMarker = false;
            }

        }
    }

    private void buildData(){


        airQualityMonitorDataContainer = new AirQualityMonitorDataContainer(airQualityData, droneLocation, System.currentTimeMillis());
//        airQualityMonitorDataContainer.setAirQuality(airQualityData);
//        airQualityMonitorDataContainer.setDroneLocation(droneLocation);
//        airQualityMonitorDataContainer.timeStamp = System.currentTimeMillis();

        FlyverMQMessage message = new FlyverMQMessage.MessageBuilder().setCreationTime(System.nanoTime()).
                setMessageId(13000).
                setTopic(TOPIC).
                setPriority((short) 2).
                setTtl(12341).
                setData(airQualityMonitorDataContainer).
                build();

        addMessage(message);
        android.util.Log.i("airq",airQualityMonitorDataContainer.toString());
        writeToBuffer(airQualityMonitorDataContainer.toString());
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }


    public String getFilePath(){
        String path = "";
        if (isExternalStorageWritable()) {
//            path = MainController.getInstance().getMainActivityRef().getExternalFilesDir(null).getAbsolutePath();
            path = Environment.getExternalStorageDirectory().toString().concat("/co.flyver/").concat(FILENAME);
//            path = path + "/" + FILENAME;
        }

        return path;
    }

    /**
     * the writeToBuffer takes every string from the data
     * Builds up the DATABUFFERSIZE and then writes to the file
     */
    public void writeToBuffer (String stringData){

        data.add(stringData);

        if(data.size()>=DATABUFFERSIZE){
            try {
                writeToFile(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
            data.clear();
        }


    }

    public void writeToFile(ArrayList<String> data) throws IOException {

        File file = new File (getFilePath());

        PrintWriter pw = new PrintWriter(new FileWriter(file,true));

        try {
            for(String d:data){
                pw.print(d);
            }
        }
        finally {
            pw.close();
        }



    }
    @Override
    public void paused() {

    }

    @Override
    public void resumed() {

    }

    @Override
    public void registered() {

    }

    @Override
    public void unregistered() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }
}
