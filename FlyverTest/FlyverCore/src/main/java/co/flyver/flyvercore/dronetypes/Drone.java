package co.flyver.flyvercore.dronetypes;

/**
 * All types of drones shall implement this interface
 * TODO: Drone specific control abstraction here
 */
public interface Drone{

    public void updateSpeeds(float yawForce, float pitchForce, float rollForce, float altitudeForce);
    public void setToZero();
    class MotorPowers {};

}
