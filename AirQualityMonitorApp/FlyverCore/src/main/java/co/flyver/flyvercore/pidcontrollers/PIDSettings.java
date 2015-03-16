package co.flyver.flyvercore.pidcontrollers;

import android.util.Log;

import co.flyver.androidrc.server.Server;
import co.flyver.utils.settings.FlyverPreferences;
import co.flyver.utils.settings.exceptions.FlyverPreferencesException;

/**
 * Created by Petar Petrov on 2/9/15.
 */
public class PIDSettings {

    public enum PIDKeys {
        PID_YAW_PROPORTIONAL("pid_yaw_p"),
        PID_YAW_INTEGRAL("pid_yaw_i"),
        PID_YAW_DERIVATIVE("pid_yaw_d"),
        PID_PITCH_PROPORTIONAL("pid_pitch_p"),
        PID_PITCH_INTEGRAL("pid_pitch_i"),
        PID_PITCH_DERIVATIVE("pid_pitch_d"),
        PID_ROLL_PROPORTIONAL("pid_roll_p"),
        PID_ROLL_INTEGRAL("pid_roll_i"),
        PID_ROLL_DERIVATIVE("pid_roll_d");
        private String key;

        private PIDKeys(String key) {
            this.key = key;
        }

        public String getValue() {
            return key;
        }
    }

    public static void setPIDSettings() {

        /* Yaw PID Controller preferences */
        if(FlyverPreferences.getPreference(PIDKeys.PID_YAW_PROPORTIONAL.getValue()) == null) {
//            changePIDSharedPreference(PIDKeys.PID_YAW_PROPORTIONAL, "2.2", context);
            FlyverPreferences.setPreference(PIDKeys.PID_YAW_PROPORTIONAL.getValue(), "2.2");
        }
        if(FlyverPreferences.getPreference(PIDKeys.PID_YAW_INTEGRAL.getValue()) == null) {
            FlyverPreferences.setPreference(PIDKeys.PID_YAW_INTEGRAL.getValue(), "0");
        }
        if(FlyverPreferences.getPreference(PIDKeys.PID_YAW_DERIVATIVE.getValue()) == null) {
            FlyverPreferences.setPreference(PIDKeys.PID_YAW_DERIVATIVE.getValue(), "0.2");
        }
        /* End of Yaw PID Controller preferences */

        /* Pitch PID Controller preferences */

        if(FlyverPreferences.getPreference(PIDKeys.PID_PITCH_PROPORTIONAL.getValue()) == null) {
            FlyverPreferences.setPreference(PIDKeys.PID_PITCH_PROPORTIONAL.getValue(), "2.4");
        }

        if(FlyverPreferences.getPreference(PIDKeys.PID_PITCH_INTEGRAL.getValue()) == null) {
            FlyverPreferences.setPreference(PIDKeys.PID_PITCH_INTEGRAL.getValue(), "0.2");
        }

        if(FlyverPreferences.getPreference(PIDKeys.PID_PITCH_DERIVATIVE.getValue()) == null) {
            FlyverPreferences.setPreference(PIDKeys.PID_PITCH_DERIVATIVE.getValue(), "0.4");
        }

        /* End of Pitch PID controller preferences */

        /* Roll PID controller preferences */
        if(FlyverPreferences.getPreference(PIDKeys.PID_ROLL_PROPORTIONAL.getValue()) == null) {
            FlyverPreferences.setPreference(PIDKeys.PID_ROLL_PROPORTIONAL.getValue(), "2.4");
        }

        if(FlyverPreferences.getPreference(PIDKeys.PID_ROLL_INTEGRAL.getValue()) == null) {
            FlyverPreferences.setPreference(PIDKeys.PID_ROLL_INTEGRAL.getValue(), "0.2");
        }

        if(FlyverPreferences.getPreference(PIDKeys.PID_ROLL_DERIVATIVE.getValue()) == null) {
            FlyverPreferences.setPreference(PIDKeys.PID_ROLL_DERIVATIVE.getValue(), "0.4");
        }
        FlyverPreferences.commit();

        /* End of Roll PID controller preferences */

        float[][] pidValues = getPidValues();
        Server.sCurrentStatus.getPidYaw().setP(pidValues[0][0]);
        Server.sCurrentStatus.getPidYaw().setI(pidValues[0][1]);
        Server.sCurrentStatus.getPidYaw().setD(pidValues[0][2]);
        Server.sCurrentStatus.getPidPitch().setP(pidValues[1][0]);
        Server.sCurrentStatus.getPidPitch().setI(pidValues[1][1]);
        Server.sCurrentStatus.getPidPitch().setD(pidValues[1][2]);
        Server.sCurrentStatus.getPidRoll().setP(pidValues[2][0]);
        Server.sCurrentStatus.getPidRoll().setI(pidValues[2][1]);
        Server.sCurrentStatus.getPidRoll().setD(pidValues[2][2]);
    }

    /**
     * returns 3x3 array of PID values
     * [0][n] - Yaw values
     * [1][n] - Pitch values
     * [2][n] - Roll values
     * [n][0] - proportional values
     * [n][1] - integral values
     * [n][2] - derivative values
     * @return array of PID values
     */
    public static float[][] getPidValues() {
        String preference;
        float p;
        float i;
        float d;
        float[][] values = new float[3][3];

        preference = FlyverPreferences.getPreference(PIDSettings.PIDKeys.PID_YAW_INTEGRAL.getValue());
        p = Float.parseFloat(preference);
        preference = FlyverPreferences.getPreference(PIDSettings.PIDKeys.PID_YAW_INTEGRAL.getValue());
        i = Float.parseFloat(preference);
        preference = FlyverPreferences.getPreference(PIDSettings.PIDKeys.PID_YAW_DERIVATIVE.getValue());
        d = Float.parseFloat(preference);
        values[0][0] = p;
        values[0][1] = i;
        values[0][2] = d;

        preference = FlyverPreferences.getPreference(PIDSettings.PIDKeys.PID_PITCH_PROPORTIONAL.getValue());
        p = Float.parseFloat(preference);
        preference = FlyverPreferences.getPreference(PIDSettings.PIDKeys.PID_PITCH_INTEGRAL.getValue());
        i = Float.parseFloat(preference);
        preference = FlyverPreferences.getPreference(PIDSettings.PIDKeys.PID_PITCH_DERIVATIVE.getValue());
        d = Float.parseFloat(preference);
        values[1][0] = p;
        values[1][1] = i;
        values[1][2] = d;

        preference = FlyverPreferences.getPreference(PIDSettings.PIDKeys.PID_ROLL_PROPORTIONAL.getValue());
        p = Float.parseFloat(preference);
        preference = FlyverPreferences.getPreference(PIDSettings.PIDKeys.PID_ROLL_INTEGRAL.getValue());
        i = Float.parseFloat(preference);
        preference = FlyverPreferences.getPreference(PIDSettings.PIDKeys.PID_ROLL_DERIVATIVE.getValue());
        d = Float.parseFloat(preference);
        values[2][0] = p;
        values[2][1] = i;
        values[2][2] = d;

        return values;
    }

}
