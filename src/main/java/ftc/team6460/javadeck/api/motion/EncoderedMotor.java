/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 FTC team 6460 et. al.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ftc.team6460.javadeck.api.motion;

import ftc.team6460.javadeck.api.peripheral.EffectorPeripheral;
import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;
import ftc.team6460.javadeck.api.peripheral.SensorPeripheral;
import ftc.team6460.javadeck.api.safety.SafetyGroup;
import ftc.team6460.javadeck.api.safety.SafetyPeripheral;

/**
 * Represents a motor with an encoder. The encoder is currently assumed to be relative and resettable. If it is not, the implementer must implement appropriate logic.
 * Subclasses must be thread-safe.
 *
 */

// TODO refactor heck out of this class
public abstract class EncoderedMotor extends UnencoderedMotor implements EffectorPeripheral<Double>, SensorPeripheral<Double, Void>, SafetyPeripheral {

    protected volatile SafetyGroup safetyGroup;


    /**
     * Sets the motor speed to the new value. This method should be overridden if it is possible to determine when the motor has spun up to its target speed.
     * The input may specify the speed in an implementation-dependent unit, but a given robot platform should implement this so that all motors of the same type will
     * cause the same linear motion after their gearing is considered. This may require instances of this to have fields describing any gearing or other considerations.
     *
     * @param input The speed for the motor to reach.
     * @throws InterruptedException                                                  If interrupted waiting for the effector to reach its target value.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException If the motor cannot be communicated with.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException    If the motor is inoperable.
     */
    @Override
    public void write(Double input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        this.writeFast(input);
    }

    /**
     * Sets the speed of the motor to the new value.
     * The input may specify the speed in an implementation-dependent unit, but a given robot platform should implement this so that all motors of the same type will
     * cause the same linear motion after their gearing is considered. This may require instances of this to have fields describing any gearing or other considerations.
     * When overridden, a call to super() is required for proper safety control.
     *
     * @param input The value to write.
     * @throws InterruptedException             If interrupted waiting for the write to finish.
     * @throws PeripheralCommunicationException If the effector cannot be communicated with.
     * @throws PeripheralInoperableException    If the effector is inoperable.
     */
    @Override
    public void writeFast(Double input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        synchronized (this) {
            if (System.nanoTime() < earliestReactivation) {
                return;
            }
            this.currentVelocity = input;
            this.doWrite(input);
        }
    }

    protected double currentVelocity = 0.0;


    /**
     * Actually write the value to the device.
     *
     * @param val The value to write
     */
    protected abstract void doWrite(double val)  throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException;

    /**
     * Resets the encoder to zero. This method should not be called after a time less
     *
     * @throws InterruptedException             If interrupted.
     * @throws PeripheralCommunicationException If communication failed.
     * @throws PeripheralInoperableException    If encoder inoperable.
     */
    public void resetEncoder() throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        synchronized (this) {
            this.calibrate(0.0, null);
            this.resetSafety();
        }
    }

    public final double antiStallThreshold;
    public final double antiStallTimeout;
    public final double maxStallPower;
    public final int encoderDirection;

    /**
     * Constructs a new encodered motor.
     *
     * @param safetyGroup        The safety group to join.
     * @param antiStallThreshold The minimum encoder distance that is considered a non-stalled motor.
     * @param antiStallTimeout   How long encoder motion can remain under the threshold before a stall is considered, nanoseconds.
     * @param maxStallPower      The maximum power at which stall prevention will not be performed.
     * @param encoderDirection   +1 if a positive power will cause the encoder reading to increase, -1 otherwise.
     */
    public EncoderedMotor(SafetyGroup safetyGroup, double antiStallThreshold, double antiStallTimeout, double maxStallPower, int encoderDirection) {

        this.safetyGroup = safetyGroup;

        this.antiStallThreshold = antiStallThreshold;
        this.antiStallTimeout = antiStallTimeout;
        this.maxStallPower = maxStallPower;
        this.encoderDirection = encoderDirection;
        safetyGroup.registerEffector(this);
    }

    /**
     * Constructs a new encodered motor.
     *
     * @param antiStallThreshold The minimum encoder distance that is considered a non-stalled motor.
     * @param antiStallTimeout   How long encoder motion can remain under the threshold before a stall is considered, nanoseconds.
     * @param maxStallPower      The maximum power at which stall prevention will not be performed.
     * @param encoderDirection   +1 if a positive power will cause the encoder reading to increase, -1 otherwise.
     */
    public EncoderedMotor(double antiStallThreshold, double antiStallTimeout, double maxStallPower, int encoderDirection) {

        this.antiStallThreshold = antiStallThreshold;
        this.antiStallTimeout = antiStallTimeout;
        this.maxStallPower = maxStallPower;
        this.encoderDirection = encoderDirection;
    }

    private double encoderLastTime = System.nanoTime();

    private double encoderLastPosition = 0.0;

    @Override
    public void setup() {
        encoderLastTime = System.nanoTime();
    }

    /*package-private*/ void resetSafety() {
        try {
            encoderLastPosition = this.read(null);
            encoderLastTime = System.nanoTime();
        } catch (Throwable e) {
            // noop
        }
    }

    @Override
    public boolean checkSafety() {
        if(this.antiStallThreshold ==0) return true;
        if (this.earliestReactivation > System.nanoTime()) {
            try {
                encoderLastPosition = this.read(null);
                encoderLastTime = System.nanoTime();
            } catch (Throwable e) {
                // noop
            }
            return true; // no new safety issue
        }

        synchronized (this) {

            if (this.currentVelocity < maxStallPower) return true;
            if (System.nanoTime() - encoderLastTime < antiStallTimeout) {
                return true;
            }
            try {
                double newVal = this.read(null);
                if ((newVal - encoderLastPosition) / Math.signum(currentVelocity / encoderDirection) > antiStallThreshold) {
                    encoderLastPosition = newVal;
                    encoderLastTime = System.nanoTime();
                    return true;
                } else {
                    return false;
                }
            } catch (Throwable e) {
                return false;
            }
        }
    }


    private volatile long earliestReactivation = 0;

    @Override
    public void safetyShutdown(long nanos) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        this.writeFast(0.0);
        this.earliestReactivation = System.nanoTime() + nanos;
    }

    /**
     * Called each event loop.
     */
    @Override
    public void loop() {
        if (!this.checkSafety()) try {
            safetyGroup.safetyShutdown(1_000_000_000); // default
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public enum EncoderControl {
        /**
         * No encoder feedback, only reads supported.
         */
        CTRL_NONE,
        /**
         * PID control (currently contigent on hardware API support)
         */
        CTRL_PID,
        /**
         * Encoder servo feedback hold.
         */
        CTRL_SRVO,
        /**
         * Stall protection--detection of non-movement of motor
         */
        CTRL_STALL

    }

}
