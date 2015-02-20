package co.flyver.flyvercore.maincontrollers;

import co.flyver.flyvercore.pidcontrollers.PIDSettings;
import co.flyver.utils.flyvermq.FlyverMQ;
import co.flyver.utils.flyvermq.FlyverMQMessage;
import co.flyver.utils.flyvermq.exceptions.FlyverMQException;
import co.flyver.utils.flyvermq.interfaces.FlyverMQConsumer;
import co.flyver.utils.settings.FlyverPreferences;

/**
 * Created by Petar Petrov on 2/13/15.
 */
public class MainControllerConsumers {

    FlyverMQConsumer throttleConsumer = null;
    FlyverMQConsumer pitchConsumer = null;
    FlyverMQConsumer rollConsumer = null;
    FlyverMQConsumer yawConsumer = null;
    FlyverMQConsumer pidPitchConsumer = null;
    FlyverMQConsumer pidRollConsumer = null;
    FlyverMQConsumer pidYawConsumer = null;

    final String TOPIC_THROTTLE = "control.throttle";
    final String TOPIC_PITCH = "control.pitch";
    final String TOPIC_ROLL = "control.roll";
    final String TOPIC_YAW = "control.yaw";
    final String TOPIC_PID_PITCH = "control.pidpitch";
    final String TOPIC_PID_YAW = "control.pidyaw";
    final String TOPIC_PID_ROLL = "control.pidroll";

    MainControllerConsumers() {

    }

    private void createThrottleConsumer() throws FlyverMQException {

        throttleConsumer = new FlyverMQConsumer() {
            @Override
            public void dataReceived(FlyverMQMessage message) {
                Number num = (Number) message.data;
                MainController.getInstance().setMeanThrust((1023 / 100) * num.floatValue());
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
        };
        FlyverMQ.getInstance().registerConsumer(throttleConsumer, TOPIC_THROTTLE);
    }
    private void createPitchConsumer() throws FlyverMQException {
        pitchConsumer = new FlyverMQConsumer() {
            @Override
            public void dataReceived(FlyverMQMessage message) {
                Number num = (Number) message.data;
                MainController.getInstance().setPitchAngleTarget(num.floatValue());
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
        };
        FlyverMQ.getInstance().registerConsumer(pitchConsumer, TOPIC_PITCH);
    }
    private void createRollConsumer() throws FlyverMQException {
        rollConsumer = new FlyverMQConsumer() {
            @Override
            public void dataReceived(FlyverMQMessage message) {
                Number num = (Number) message.data;
                MainController.getInstance().setRollAngleTarget(num.floatValue());
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
        };
        FlyverMQ.getInstance().registerConsumer(rollConsumer, TOPIC_ROLL);

    }
    private void createYawConsumer() throws FlyverMQException {
        yawConsumer = new FlyverMQConsumer() {
            @Override
            public void dataReceived(FlyverMQMessage message) {
                Number num = (Number) message.data;
                MainController.getInstance().setYawAngleTarget(num.floatValue());
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
        };
        FlyverMQ.getInstance().registerConsumer(yawConsumer, TOPIC_YAW);
    }
    private void createPidPitchConsumer() throws FlyverMQException {
        pidPitchConsumer = new FlyverMQConsumer() {
            @Override
            public void dataReceived(FlyverMQMessage message) {
                String data = (String) message.data;
                String[] values = data.replaceAll("[\\[\\]]", "").split(",");
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_PITCH_PROPORTIONAL.getValue(), values[0]);
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_PITCH_INTEGRAL.getValue(), values[1]);
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_PITCH_DERIVATIVE.getValue(), values[2]);
                FlyverPreferences.commit();
                MainController.getInstance().pitchController.setCoefficients(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));

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
        };
        FlyverMQ.getInstance().registerConsumer(pidPitchConsumer, TOPIC_PID_PITCH);

    }
    private void createPidRollConsumer() throws FlyverMQException {
        pidRollConsumer = new FlyverMQConsumer() {
            @Override
            public void dataReceived(FlyverMQMessage message) {
                String data = (String) message.data;
                String[] values = data.replaceAll("[\\[\\]]", "").split(",");
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_ROLL_PROPORTIONAL.getValue(), values[0]);
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_ROLL_INTEGRAL.getValue(), values[1]);
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_ROLL_DERIVATIVE.getValue(), values[2]);
                FlyverPreferences.commit();
                MainController.getInstance().rollController.setCoefficients(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
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
        };
        FlyverMQ.getInstance().registerConsumer(pidRollConsumer, TOPIC_PID_ROLL);

    }
    private void createPidYawConsumer() throws FlyverMQException {
        pidYawConsumer = new FlyverMQConsumer() {
            @Override
            public void dataReceived(FlyverMQMessage message) {
                String data = (String) message.data;
                String[] values = data.replaceAll("[\\[\\]]", "").split(",");
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_YAW_PROPORTIONAL.getValue(), values[0]);
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_YAW_INTEGRAL.getValue(), values[1]);
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_YAW_DERIVATIVE.getValue(), values[2]);
                FlyverPreferences.commit();
                MainController.getInstance().yawController.setCoefficients(Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
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
        };
        FlyverMQ.getInstance().registerConsumer(pidYawConsumer, TOPIC_PID_YAW);

    }

    void start() throws FlyverMQException {
        createThrottleConsumer();
        createPitchConsumer();
        createRollConsumer();
        createYawConsumer();
        createPidPitchConsumer();
        createPidRollConsumer();
        createPidYawConsumer();
    }
}
