package co.flyver.flyvercore.maincontrollers;


import android.app.Activity;

import co.flyver.flyvercore.baseapp.BaseApp;
import co.flyver.flyvercore.pidcontrollers.PIDSettings;
import co.flyver.utils.containers.SharedIPCKeys;
import co.flyver.androidrc.server.Server;
import co.flyver.androidrc.server.Status;
import co.flyver.androidrc.server.interfaces.ServerCallback;
import co.flyver.dataloggerlib.LoggerService;
import co.flyver.flyvercore.dronetypes.Drone;
import co.flyver.flyvercore.microcontrollers.MicroController;
import co.flyver.flyvercore.pidcontrollers.PIDAngleController;
import co.flyver.flyvercore.statedata.DroneState;
import co.flyver.utils.flyvermq.FlyverMQ;
import co.flyver.utils.flyvermq.FlyverMQMessage;
import co.flyver.utils.flyvermq.exceptions.FlyverMQException;
import co.flyver.utils.flyvermq.interfaces.FlyverMQConsumer;
import co.flyver.utils.settings.FlyverPreferences;
import ioio.lib.spi.Log;


/**
 * The MainController is the heart of Flyver
 * The MainController initialized all the controllers and control algorithms
 */

public class MainController extends Activity {

    /* CONSTANTS*/
    public static final double MAX_MOTOR_POWER = 1023.0;
    public static final long INT_MAX = Integer.MAX_VALUE;
    public static final float MAX_SAFE_PITCH_ROLL = 45; // [deg].
    public static final float PID_DERIV_SMOOTHING = 0.5f;
    private static final String CONTROLLER = "MainController";
    /* END OF*/

    private static MainController instance;
    private Drone drone;
    private float meanThrust;
    private float yawAngleTarget;
    private float pitchAngleTarget;
    private float rollAngleTarget;
    private float altitudeTarget;
    private float timeWithoutPcRx;
    private float timeWithoutAdkRx;
    private DroneState droneState;
    private boolean regulatorEnabled;
    private boolean altitudeLockEnabled;
    PIDAngleController yawController;
    PIDAngleController pitchController;
    PIDAngleController rollController;
    private DroneState.DroneStateData droneStateData;
    private long previousTime;
    private boolean zeroStateFlag = false; // Flag indicate that droneState should be zeroed
    private FlyverMQ messageQueue;


    private LoggerService logger;

    public static MainController getInstance() {
        return instance;
    }

    public FlyverMQ getMessageQueue() {
        return messageQueue;
    }

    public float getYawAngleTarget() {
        return yawAngleTarget;
    }

    public void setYawAngleTarget(float yawAngleTarget) {
        this.yawAngleTarget = yawAngleTarget;
    }

    public float getPitchAngleTarget() {
        return pitchAngleTarget;
    }

    public void setPitchAngleTarget(float pitchAngleTarget) {
        this.pitchAngleTarget = pitchAngleTarget;
    }

    public float getRollAngleTarget() {
        return rollAngleTarget;
    }

    public void setRollAngleTarget(float rollAngleTarget) {
        this.rollAngleTarget = rollAngleTarget;
    }

    public float getMeanThrust() {
        return meanThrust;
    }

    public void setMeanThrust(float meanThrust) {
        this.meanThrust = meanThrust;
    }


    private void initLogger() {
        logger = new LoggerService(BaseApp.applicationContext, messageQueue);
        logger.Start();
        logger.LogData("EV_DEBUG", "MainController", "MainController initialized the Logger.");
    }

    public MainController(Drone drone) throws MainControllerInstanceExisting {
        if(instance != null) {
            throw new MainControllerInstanceExisting("Only one instance of the MainController can be existing at a time");
        }
        regulatorEnabled = true;
        this.drone = drone;

        try {
            messageQueue = FlyverMQ.getInstance();
        } catch (FlyverMQException e) {
            e.printStackTrace();
        }

        // altitudeRegulator = new PidRegulator(0.0f,  0.0f,  0.0f, PID_DERIV_SMOOTHING, 0.0f);

        yawAngleTarget = 0.0f;
        pitchAngleTarget = 0.0f;
        rollAngleTarget = 0.0f;
        altitudeTarget = 0.0f;
        instance = this;

    }

    public void start() {
        // Initializations.
        regulatorEnabled = true;
        altitudeLockEnabled = false;
        meanThrust = 0.0f;

        // Start the sensors.
        droneState = new DroneState();
        droneStateData = droneState.new DroneStateData();

        //initialize the controller parameters
        PIDSettings.setPIDSettings();
        startPIDControllers();

        try {
            new MainControllerConsumers().start();
        } catch (FlyverMQException e) {
            e.printStackTrace();
        }

        registerCallbacks();
        registerConsumers();

        //initLogger();
    }

    public void stop() {
        // Stop the main controller thread.
        // Stop the sensors.
        droneState.paused();
    }

    public DroneState.DroneStateData getSensorsData() {
        return droneStateData;
    }

    public void setMeanTrust(float trust) {
        meanThrust = trust;
    }


    /**
     * onConnectionEstablished is called when the whole systems starts
     * and int working order
     */
    public void onConnectionEstablished() {
        // Reset the orientation.
        droneState.setCurrentStateAsZero();
        // TODO: droneState.setCurrentStateAsZero works only on initialization, but can't be accessed after
    }

    /**
     * Called when the communication link between the drone and the RC is lost.
     * TODO: Break it into cases and implement algorithms such as Return To Home
     */
    public void onConnectionLost() {
        // Emergency stop of the quadcopter.
        emergencyStop("Connection Lost");
    }

    public void onIoioConnect(MicroController microController) {
        Log.e(CONTROLLER, "IOIO Connection established");

    }

    public void onIoioDisconnect() {
        Log.e(CONTROLLER, "IOIO Connection lost");
    }

    /**
     * Stops the drone.
     *
     * @param reason for logging the cause of the emergency
     */
    public void emergencyStop(String reason) {
        // TODO: Smart Landing
        Log.w("Emergency", reason);
        yawController.resetIntegrator();
        pitchController.resetIntegrator();
        rollController.resetIntegrator();
        setMeanThrust(0);
    }

    private void startPIDControllers() {
        float[][] pidValues = PIDSettings.getPidValues();
        yawController = new PIDAngleController(pidValues[0][0], pidValues[0][1], pidValues[0][2], PID_DERIV_SMOOTHING);
        pitchController = new PIDAngleController(pidValues[1][0], pidValues[1][1], pidValues[1][2], PID_DERIV_SMOOTHING);
        rollController = new PIDAngleController(pidValues[2][0], pidValues[2][1], pidValues[2][2], PID_DERIV_SMOOTHING);
    }

    private void registerCallbacks() {
        ServerCallback onValuesChange = new ServerCallback() {
            @Override
            public void run(String json) {
                setMeanTrust(Server.sCurrentStatus.getThrottle());
                setPitchAngleTarget(Server.sCurrentStatus.getPitch());
                setRollAngleTarget(Server.sCurrentStatus.getRoll());
                setYawAngleTarget(Server.sCurrentStatus.getYaw());
                if (Server.sCurrentStatus.isEmergency()) {
                    Log.e(CONTROLLER, "Emergency sequence initiated");
                    emergencyStop("Server command");
                }
            }
        };

        ServerCallback onPidYawChange = new ServerCallback() {
            @Override
            public void run(String json) {
                Log.e("PID", "PID Yaw coefficients have changed!!!");
                Status.PID pid = Server.sCurrentStatus.getPidYaw();
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_YAW_PROPORTIONAL.getValue(), String.valueOf(pid.getP()));
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_YAW_INTEGRAL.getValue(), String.valueOf(pid.getI()));
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_YAW_DERIVATIVE.getValue(), String.valueOf(pid.getD()));
                FlyverPreferences.commit();
                yawController.setCoefficients(pid.getP(), pid.getI(), pid.getD());
            }
        };

        ServerCallback onPidPitchChange = new ServerCallback() {
            @Override
            public void run(String json) {
                Log.e("PID", "PID Pitch coefficients have changed!!!");
                Status.PID pid = Server.sCurrentStatus.getPidPitch();
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_PITCH_PROPORTIONAL.getValue(), String.valueOf(pid.getP()));
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_PITCH_INTEGRAL.getValue(), String.valueOf(pid.getI()));
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_PITCH_DERIVATIVE.getValue(), String.valueOf(pid.getD()));
                FlyverPreferences.commit();
                pitchController.setCoefficients(pid.getP(), pid.getI(), pid.getD());
            }
        };

        ServerCallback onPidRollChange = new ServerCallback() {
            @Override
            public void run(String json) {
                Log.e("PID", "PID Roll coefficients have changed!!!");
                Status.PID pid = Server.sCurrentStatus.getPidRoll();
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_ROLL_PROPORTIONAL.getValue(), String.valueOf(pid.getP()));
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_ROLL_INTEGRAL.getValue(), String.valueOf(pid.getI()));
                FlyverPreferences.setPreference(PIDSettings.PIDKeys.PID_ROLL_DERIVATIVE.getValue(), String.valueOf(pid.getD()));
                FlyverPreferences.commit();
                rollController.setCoefficients(pid.getP(), pid.getI(), pid.getD());
            }
        };

        //Register callbacks to be called when the appropriate JSON is received
        Server.registerCallback(SharedIPCKeys.THROTTLE, onValuesChange);
        Server.registerCallback(SharedIPCKeys.YAW, onValuesChange);
        Server.registerCallback(SharedIPCKeys.COORDINATES, onValuesChange);
        Server.registerCallback(SharedIPCKeys.EMERGENCY, onValuesChange);
        Server.registerCallback(SharedIPCKeys.PIDPITCH, onPidPitchChange);
        Server.registerCallback(SharedIPCKeys.PIDROLL, onPidRollChange);
        Server.registerCallback(SharedIPCKeys.PIDYAW, onPidYawChange);
    }

    public void registerConsumers() {

        FlyverMQConsumer droneStateListener = new FlyverMQConsumer() {
            @Override
            public void dataReceived(FlyverMQMessage message) {
                float yawForce, pitchForce, rollForce, altitudeForce, currentYaw, currentPitch, currentRoll;

                if (!zeroStateFlag) {
                    droneState.setCurrentStateAsZero();
                    zeroStateFlag = true;
                }

                // Get the sensors data.
                droneStateData = (DroneState.DroneStateData) message.data;

                currentYaw = droneStateData.yaw;
                currentPitch = droneStateData.pitch;
                currentRoll = droneStateData.roll;

                long currentTime = droneStateData.time;
                float dt = ((float) (currentTime - previousTime)) / 1000000000.0f; // [s].
                previousTime = currentTime;

                // Check for dangerous situations.
                if (regulatorEnabled) {
                    // If the quadcopter is too inclined, emergency stop it.
                    if (Math.abs(currentPitch) > MAX_SAFE_PITCH_ROLL ||
                            Math.abs(currentRoll) > MAX_SAFE_PITCH_ROLL) {
                        //TODO: Setup safety max rol/pitch values
                        emergencyStop("Safe pitch or safe roll exceeded");
                    }
                }

                // Compute the motors powers.
                if (regulatorEnabled && meanThrust > 1.0) {
                    // Compute the "forces" needed to move the quadcopter to the
                    // set point.
                    yawForce = yawController.getInput(yawAngleTarget, currentYaw, dt);
                    pitchForce = pitchController.getInput(pitchAngleTarget, currentPitch, dt);
                    rollForce = rollController.getInput(rollAngleTarget, currentRoll, dt);
                /*
                if(altitudeLockEnabled)
                    altitudeForce = altitudeRegulator.getInput(altitudeTarget, currentAltitude, dt);
                else */
                    altitudeForce = meanThrust;
                    drone.updateSpeeds(yawForce, pitchForce, rollForce, altitudeForce);

                } else {
                    drone.setToZero();
                    yawController.resetIntegrator();
                    pitchController.resetIntegrator();
                    rollController.resetIntegrator();
                    yawForce = 0.0f;
                    pitchForce = 0.0f;
                    rollForce = 0.0f;
                    altitudeForce = 0.0f;
                }
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
        try {
            FlyverMQ.getInstance().registerConsumer(droneStateListener, "dronestate.raw");
        } catch (FlyverMQException e) {
            e.printStackTrace();
        }
    }
}