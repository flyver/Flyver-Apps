package co.flyver.flyvercore.microcontrollers.IOIOOutputs;

import android.util.Log;

import co.flyver.flyvercore.dronetypes.QuadCopterX;
import ioio.lib.api.IOIO;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;

/**
 * Created by Tihomir Nedev on 15-2-18.
 */
public class QuadCopterXIOIO extends QuadCopterX{

    private final int PWMFrequency = 200; // Frequcy of the PWM signal

    private int minPWMSignalPulse = 1000; // The minimum length of the pulse of the PWM signal
    private int maxPWMSignalPulse = 2023; // // The maximum length of the pulse of the PWM signal



    private PwmOutput motorFC; // Front clockwise motor
    private PwmOutput motorFCC; // Front counterclockwise motor
    private PwmOutput motorRC; // Rear clockwise motor
    private PwmOutput motorRCC; // Rear counterclockwise motor

    private int motorFCPin;
    private int motorFCCPin;
    private int motorRCPin;
    private int motorRCCPin;

    public QuadCopterXIOIO(int motorFCPin, int motorFCCPin, int motorRCPin, int motorRCCPin){
        this.motorFCPin = motorFCPin;
        this.motorFCCPin = motorFCCPin;
        this.motorRCPin = motorRCPin;
        this.motorRCCPin = motorRCCPin;

        new QuadCopterXMotors();
    }

private class QuadCopterXMotors extends IOIOOutput{


    @Override
    public void setup(IOIO ioio_) throws ConnectionLostException {


        motorFC = ioio_.openPwmOutput(motorFCPin, PWMFrequency);
        motorFCC = ioio_.openPwmOutput(motorFCCPin, PWMFrequency);
        motorRC = ioio_.openPwmOutput(motorRCPin, PWMFrequency);
        motorRCC = ioio_.openPwmOutput(motorRCCPin, PWMFrequency);

    }

    @Override
    public void process(IOIO ioio_) throws ConnectionLostException, InterruptedException {

        motorFC.setPulseWidth(motorPowers.fc + minPWMSignalPulse);
        motorFCC.setPulseWidth(motorPowers.fcc + minPWMSignalPulse);
        motorRC.setPulseWidth(motorPowers.rc + minPWMSignalPulse);
        motorRCC.setPulseWidth(motorPowers.rcc + minPWMSignalPulse);


    }
}

    public void setMinPWMSignalPulse(int minPWMSignalPulse) {
        this.minPWMSignalPulse = minPWMSignalPulse;
    }

    public void setMaxPWMSignalPulse(int maxPWMSignalPulse) {
        this.maxPWMSignalPulse = maxPWMSignalPulse;
    }

    public int getMinPWMSignalPulse() {
        return minPWMSignalPulse;
    }

    public int getMaxPWMSignalPulse() {
        return maxPWMSignalPulse;
    }
}
