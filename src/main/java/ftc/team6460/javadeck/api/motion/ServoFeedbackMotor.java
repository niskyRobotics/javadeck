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

/**
 * A motor that can reach a specific absolute position, and optionally maintain it (compensating for external forces as needed)
 */
public abstract class ServoFeedbackMotor implements EffectorPeripheral<Double>, SensorPeripheral<Double, Void> {
    private final AntiStallFilter inner;

    private final double correctionFactor;

    private boolean isActivelySeeking = true;

    private boolean holdAtPosition = true;

    private double currentGoal;

    private int currentDirectionSignum;

    private final double maxPower;

    private final double maxErrorTolerance;

    @Override
    public void writeFast(Double input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        this.doWrite(input);
    }

    @Override
    public void write(Double input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        this.doWrite(input);
        while (Math.abs(this.currentGoal - this.read(null)) > this.maxErrorTolerance) {
            Thread.sleep(100);
        }
    }

    @Override
    public void setup() {
        //noop
    }

    @Override
    public void loop() {
        synchronized (this) {
            inner.loop();
            if (!isActivelySeeking) return;
            try {
                double currentPos = this.read(null);
                double power = (currentGoal - currentPos) * correctionFactor;
                if (Math.abs(currentGoal - currentPos) < maxErrorTolerance) {
                    power = 0;
                }
                int newDirectionSignum = (int) Math.signum(power);
                if (newDirectionSignum == -this.currentDirectionSignum || newDirectionSignum == 0) {
                    if (!holdAtPosition) {
                        this.isActivelySeeking = false;
                        power = 0;
                    }
                }
                power = Math.signum(power) * Math.max(Math.abs(maxPower), Math.abs(power));
                currentDirectionSignum = (int) Math.signum(power);
                inner.writeFast(power);

            } catch (Exception e) {
                // pass for now
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
        this.isActivelySeeking = false;
        inner.safetyShutdown(nanos);
    }

    protected void doWrite(double val) {

        synchronized (this) {
            inner.resetSafety();
            this.currentGoal = val;
            this.isActivelySeeking = true;
        }
    }

    public void resetEncoder() throws
            InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        synchronized (this) {
            inner.resetEncoder();

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
