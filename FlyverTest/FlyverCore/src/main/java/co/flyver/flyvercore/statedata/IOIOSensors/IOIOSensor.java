package co.flyver.flyvercore.statedata.IOIOSensors;

import co.flyver.flyvercore.statedata.IOIOSensors.exceptions.SensorsListException;
import co.flyver.utils.flyvermq.FlyverMQMessage;
import co.flyver.utils.flyvermq.FlyverMQProducer;
import co.flyver.utils.flyvermq.exceptions.ProducerAlreadyRegisteredException;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Created by Tihomir Nedev on 15-1-19.
 * Every sensor used with IOIO OTG board should extend IOIOSensor
 * Each IOIOSensor implements @register() where a implementation of the initialization of the sensor should be made in
 * the context of the IOIO. This method is called in the setup() method of IOIOController
 * Each IOIOSensor implements @process() where data collection or other looped commands are made. This method is executed within the loop() method of the IOIOController.
 *
 * @sendData(Object data) should be called so that the sensor produces a message within the FlyverMessageQ
 * The produced message follows the topic defined in @TOPIC
 */
abstract public class IOIOSensor {

    String TOPIC;

    SensorDataProducer sensorDataProducer;

     public IOIOSensor (String TOPIC){
        this.TOPIC = TOPIC;
        sensorDataProducer = new SensorDataProducer(TOPIC);
        register();
    }

    public void register() {
        try {
            SensorsList.getInstance().addSensor(this);
        } catch (SensorsListException e) {
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


    /**
     * Automatically generates messages for the FlyverMessageQ from the sensor data.
     *
     * @param data
     */
    protected void sendData(Object data){
        sensorDataProducer.sendData(data);
    }

    /**
     * @SensorDataProducer is producing sensor data messages with the FlyverMessageQ
     */
    private class SensorDataProducer extends FlyverMQProducer{
        private SensorDataProducer(String topic) {
            super(topic);
            try {
                register(false);
            } catch (ProducerAlreadyRegisteredException e) {
                e.printStackTrace();
            }
        }

        /**
         * @sendData builds and sends a message of data through the FlyverMessageQ
         * @param data is
         */
        private void sendData(Object data){
            FlyverMQMessage message = new FlyverMQMessage.MessageBuilder().setCreationTime(System.nanoTime()).
                    setMessageId(13000).
                    setTopic(TOPIC).
                    setPriority((short) 3).
                    setTtl(12341).
                    setData(data).
                    build();
            addMessage(message);
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
}
