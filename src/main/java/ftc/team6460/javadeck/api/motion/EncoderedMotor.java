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

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Represents a motor with an encoder. The encoder is currently assumed to be relative and resettable. If it is not, the implementor must implement appropriate logic.
 * Subclasses must be thread-safe.
 */
public abstract class EncoderedMotor implements EffectorPeripheral<Double>, SensorPeripheral<Double, Void>, SafetyPeripheral {

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
     *
     * @param input The value to write.
     * @throws InterruptedException             If interrupted waiting for the write to finish.
     * @throws PeripheralCommunicationException If the effector cannot be communicated with.
     * @throws PeripheralInoperableException    If the effector is inoperable.
     */
    @Override
    public abstract void writeFast(Double input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException;

    /**
     * Resets the encoder to zero.
     *
     * @throws InterruptedException             If interrupted.
     * @throws PeripheralCommunicationException If communication failed.
     * @throws PeripheralInoperableException    If encoder inoperable.
     */
    public void resetEncoder() throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        this.calibrate(0.0, null);
    }

    private AtomicLong millisLastMovement = new AtomicLong(System.currentTimeMillis());
    private AtomicLong encoderLastMovement = new AtomicLong(Double.doubleToLongBits(0.0));
    private AtomicReference<EncoderControl> controlType = new AtomicReference<>(EncoderControl.CTRL_NONE);

    public void setControlType(EncoderControl type) {
        this.controlType.set(type);
    }

    @Override
    public void setup() {
        millisLastMovement.set(System.currentTimeMillis());
    }

    @Override
    public boolean checkSafety() {
        if (controlType.get() != EncoderControl.CTRL_STALL) return true;
        double oldVal = Double.longBitsToDouble(encoderLastMovement.get());
        try {
            double newVal = this.read(null);
            return false; //todo
        } catch (Throwable e) {
            return false;
        }

    }

    /**
     * Called each event loop.
     */
    @Override
    public void loop() {
        if (!this.checkSafety()) try {
            safetyGroup.safetyShutdown();
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
