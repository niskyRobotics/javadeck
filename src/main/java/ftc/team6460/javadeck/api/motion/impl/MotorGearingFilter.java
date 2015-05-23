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

package ftc.team6460.javadeck.api.motion.impl;

import ftc.team6460.javadeck.api.motion.EncoderedMotor;
import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;
import ftc.team6460.javadeck.api.safety.SafetyGroup;

/**
 * Represents gearing on a drive motor.
 */
public class MotorGearingFilter extends EncoderedMotor {
    private final double driveFactor, encoderFactor;

    /**
     * Creates a motor gearing
     * @param driveFactor The output velocity for 1.0 power, in meters/sec.
     * @param encoderFactor The number of encoder ticks per meter of movement.
     * @param delegate The actual motor to use.
     */
    public MotorGearingFilter(double driveFactor, double encoderFactor, AntiStallFilter delegate) {
        this.driveFactor = driveFactor;
        this.encoderFactor = encoderFactor;
        this.delegate = delegate;
    }

    private final AntiStallFilter delegate;

    @Override
    public void doWrite(double val) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {

        delegate.writeFast(val / driveFactor);

    }

    @Override
    public void safetyShutdown(long nanos) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        this.writeFast(0.0);
    }

    @Override
    public void addSafetyGroup(SafetyGroup grp) {
        delegate.addSafetyGroup(grp);
    }

    @Override
    public Double read(Void params) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        return delegate.read(params) / encoderFactor;
    }

    @Override
    public void calibrate(Double val, Void params) throws InterruptedException, UnsupportedOperationException, PeripheralInoperableException, PeripheralCommunicationException {
        delegate.calibrate(val * encoderFactor, params);
    }

    @Override
    public boolean checkSafety() {
        return delegate.checkSafety();
    }

    @Override
    public void setup() {

    }
}
