package co.flyver.flyvercore.statedata;

import co.flyver.flyvercore.maincontrollers.MainController;
import co.flyver.utils.flyvermq.FlyverMQMessage;
import co.flyver.utils.flyvermq.FlyverMQProducer;
import co.flyver.utils.flyvermq.exceptions.ProducerAlreadyRegisteredException;
import co.flyver.utils.flyvermq.interfaces.FlyverMQCallback;
import co.flyver.utils.flyvermq.interfaces.FlyverMQConsumer;

public class DroneState implements  FlyverMQConsumer, FlyverMQCallback {

    public static final float PI = 3.14159265359f;
    public static final float RAD_TO_DEG = 180.0f / PI;
    public static final double DEG_TO_RAD = PI / 180.0;
    public static final float ALTITUDE_SMOOTHING = 0.95f;
    public static final String TAG = "drone_state";
    public static final String TOPIC = "dronestate.raw";

    private DroneStateData droneStateData;
    private float[] yawPitchRollVec = new float[3];
    private float yawZero;
    private float pitchZero;
    private float rollZero;
    private float elevationZero;
    private float latitudeZero;
    private float longitudeZero;
    private float absoluteLongitude;
    private float absoluteLatitude;
    private float absoluteElevation;
    private SensorsWrapper sensors;
    private FlyverMQProducer dronestateProducer;

    static int count = 0;
    @Override
    public void dataReceived(FlyverMQMessage message) {
        // Make the measurements relative to the user-defined zero orientation.
        count++;
        assert message != null;
        String data = (String) message.data;
        String[] nums = data.replace('[', ' ').replace(']', ' ').split(",");
        //Log.d(TAG, String.valueOf(count).concat(" received msg ").concat(message.topic));

        float[] orientationMatrix = new float[nums.length];
        for (int i = 0; i < nums.length ; i++) {
            orientationMatrix[i] = Float.parseFloat(nums[i]);
        }
        System.arraycopy(orientationMatrix, 0, yawPitchRollVec, 0, 3);
        droneStateData.yaw = getMainAngle(-(yawPitchRollVec[0] - yawZero) * RAD_TO_DEG);
        droneStateData.pitch = getMainAngle(-(yawPitchRollVec[1] - pitchZero) * RAD_TO_DEG);

        if (yawPitchRollVec[2] * RAD_TO_DEG < -150f) { // If the roll angle is less than -150 add 180
            droneStateData.roll = getMainAngle((yawPitchRollVec[2] - rollZero) * RAD_TO_DEG + 180f);
        } else if (yawPitchRollVec[2] * RAD_TO_DEG > +150f) {
            droneStateData.roll = getMainAngle((yawPitchRollVec[2] - rollZero) * RAD_TO_DEG - 180f);
        } else {
            droneStateData.roll = getMainAngle((yawPitchRollVec[2] - rollZero) * RAD_TO_DEG);
        }
//        Log.d(TAG, "YAW: " + droneStateData.yaw + " PITCH: " + droneStateData.pitch + " ROLL: " + droneStateData.roll);
        droneStateData.time = System.nanoTime();
        FlyverMQMessage msg = new FlyverMQMessage.MessageBuilder().setCreationTime(1).setMessageId(2).setTtl(4).setPriority((short) 1).setTopic(TOPIC).setData(droneStateData).build();
        dronestateProducer.addMessage(msg);
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

    @Override
    public void producerRegistered(String topic) {

    }

    @Override
    public void producerUnregistered(String topic) {

    }

    public float[] getYawPitchRollVec() {
        return yawPitchRollVec;
    }

    public DroneState() {
        droneStateData = new DroneStateData();
        yawZero = 0.0f;
        pitchZero = 0.0f;
        rollZero = 0.0f;

        dronestateProducer = new FlyverMQProducer(TOPIC) {

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
        };
        MainController.getInstance().getMessageQueue().registerConsumer(this, "sensors.raw");
        try {
            dronestateProducer.register(false);
        } catch (ProducerAlreadyRegisteredException e) {
            e.printStackTrace();
        }
    }

    public class DroneStateData {
        public float yaw, pitch, roll; // [degrees].
        public long time; // [nanoseconds].

        // TODO: Other state data could be added

    }

    public DroneStateData getState() {
        return droneStateData;
    }

    public void setCurrentStateAsZero() {
        yawZero = yawPitchRollVec[0];
//        pitchZero = yawPitchRollVec[1];
//        rollZero = yawPitchRollVec[2];
        elevationZero = absoluteElevation;
        longitudeZero = absoluteLongitude;
        latitudeZero = absoluteLatitude;
    }

    // Return the smallest angle between two segments.
    public static float getMainAngle(float angle) {
        while (angle < -180.0f)
            angle += 360.0f;
        while (angle > 180.0f)
            angle -= 360.0f;

        return angle;
    }

    public float[] getZeroStates() {
        float[] zeroStates = new float[3];

        zeroStates[0] = yawZero;
        zeroStates[1] = pitchZero;
        zeroStates[2] = rollZero;

        return zeroStates;
    }
}