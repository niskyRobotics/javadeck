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
import ftc.team6460.javadeck.api.peripheral.EffectorPeripheral;
import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;
import ftc.team6460.javadeck.api.peripheral.SensorPeripheral;
import ftc.team6460.javadeck.api.safety.SafetyGroup;

import java.util.concurrent.SynchronousQueue;

/**
 * A motor that can reach a specific absolute position, and optionally maintain it (compensating for external forces as needed)
 */
public abstract class ServoFeedbackMotor implements EffectorPeripheral<Double>, SensorPeripheral<Double, Void> {
    private final EncoderedMotor inner;

    private final double correctionFactor;

    private boolean isActivelySeeking = true;

    private boolean holdAtPosition = true;

    private double currentGoal;

    private int currentDirectionSignum;

    private final double maxPower;

    private final double maxErrorTolerance;

    @Override
    public void writeFast(Double input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        // simply delegate
        this.doWrite(input);
    }


    SynchronousQueue<Object> servoFinishedMvmtSynchronizer = new SynchronousQueue<>();

    @Override
    public void write(Double input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        this.doWrite(input);
        // wait for a movement to complete.
        servoFinishedMvmtSynchronizer.take();
    }

    @Override
    public abstract void setup();

    @Override
    public void loop() {
        synchronized (this) {
            inner.loop();
            if (isActivelySeeking) {
                try {
                    double currentPos = this.read(null);
                    double power = (currentGoal - currentPos) * correctionFactor;
                    if (Math.abs(currentGoal - currentPos) < maxErrorTolerance) {
                        power = 0;
                        // ignore return value
                        servoFinishedMvmtSynchronizer.offer(new Object());
                    }
                    int newDirectionSignum = (int) Math.signum(power);
                    if (newDirectionSignum == -this.currentDirectionSignum || newDirectionSignum == 0) {
                        if (!holdAtPosition) {
                            this.isActivelySeeking = false;
                            power = 0;
                            servoFinishedMvmtSynchronizer.offer(new Object());
                            return;
                        }
                    }
                    power = Math.signum(power) * Math.max(Math.abs(maxPower), Math.abs(power));
                    currentDirectionSignum = (int) Math.signum(power);
                    inner.writeFast(power);

                } catch (RuntimeException e) {
                    throw e;
                } catch (InterruptedException | PeripheralInoperableException | PeripheralCommunicationException e) {
                    // todo logging mechanism
                }
            } else {
                return;
            }
        }
    }

    /**
     * Constructs a new motor.
     *
     * @param inner             The motor to use
     * @param correctionFactor  A correction factor specifying how fast to correct, and in which direction. Positive means positive power causes an increase in encoder reading.
     *                          Negative means that a positive power causes encoder readings to decrease.
     * @param maxPower          The maximum drive power acceptable for this motor.
     * @param maxErrorTolerance The maximum deviation, in encoder counts between the goal and actual, that is allowed.
     */
    public ServoFeedbackMotor(AntiStallFilter inner, double correctionFactor, double maxPower, double maxErrorTolerance) {
        this.inner = inner;
        this.correctionFactor = correctionFactor;
        this.maxPower = maxPower;
        this.maxErrorTolerance = maxErrorTolerance;
    }

    /**
     * Specifies whether this servo should hold at its final position
     *
     * @param holdAtPosition True if the motor should maintain its goal position after it reaches it, false if it should shut down after reaching its goal.
     */
    public void setHoldAtPosition(boolean holdAtPosition) {
        synchronized (this) {
            this.holdAtPosition = holdAtPosition;
        }
    }

    @Override
    public void safetyShutdown(long nanos) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        synchronized (this) {
            this.isActivelySeeking = false;
        }
        inner.safetyShutdown(nanos);
    }

    protected void doWrite(double val) {
        // doWrite actually calculates servo control. No delegation straight to the physical motor.
        synchronized (this) {
            this.currentGoal = val;
            this.isActivelySeeking = true;
        }
    }

    public void resetEncoder() throws
            InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        synchronized (this) {
            inner.calibrate(0.0, null);

            this.isActivelySeeking = false;
        }
    }

    @Override
    public void addSafetyGroup(SafetyGroup grp) {
        inner.addSafetyGroup(grp);
    }

    @Override
    public Double read(Void params) throws
            InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        return inner.read(params);
    }

    @Override
    public void calibrate(Double val, Void params) throws
            InterruptedException, UnsupportedOperationException, PeripheralInoperableException, PeripheralCommunicationException {
        inner.calibrate(val, params);

    }
}
