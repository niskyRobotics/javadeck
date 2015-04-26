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

import ftc.team6460.javadeck.api.motion.VelocityDrivetrain;
import ftc.team6460.javadeck.api.peripheral.EffectorPeripheral;
import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;

/**
 * The motor polarity is assumed such that positive power causes that motor, as seen from outside robot, to spin clockwise.
 */
public class HolonomicDrivetrain implements VelocityDrivetrain {
    private final EffectorPeripheral<Double> flMotor;
    private final EffectorPeripheral<Double> frMotor;
    private final EffectorPeripheral<Double> rlMotor;
    private final EffectorPeripheral<Double> rrMotor;

    public HolonomicDrivetrain(EffectorPeripheral<Double> flMotor, EffectorPeripheral<Double> frMotor, EffectorPeripheral<Double> rlMotor, EffectorPeripheral<Double> rrMotor, double speedFactor) {
        this.flMotor = flMotor;
        this.frMotor = frMotor;
        this.rlMotor = rlMotor;
        this.rrMotor = rrMotor;
        this.speedFactor = speedFactor;
    }

    public static final double SQRT_2 = Math.sqrt(2);

    /**
     * Raw speed to motor needed to reach 1 meter/sec.
     */
    private final double speedFactor;

    @Override
    public void setVelocity(double forwardVelocity) throws PeripheralInoperableException, PeripheralCommunicationException, InterruptedException {
        flMotor.writeFast(-forwardVelocity * speedFactor * SQRT_2);
        frMotor.writeFast(forwardVelocity * speedFactor * SQRT_2);
        rlMotor.writeFast(-forwardVelocity * speedFactor * SQRT_2);
        rrMotor.writeFast(forwardVelocity * speedFactor * SQRT_2);
    }

    /**
     * Sets the velocity in two dimensions without rotating the robot
     * @param forwardVelocity Meters per second forward
     * @param rightwardVelocity Meters per second rightward
     * @throws java.lang.IllegalArgumentException                                    If the velocity is beyond the limits or is invalid.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException If a motor or other drivetrain part cannot be communicated with.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException    If a motor or other drivetrain part is inoperable.
     */
    public void set2DVelocity(double forwardVelocity, double rightwardVelocity) throws InterruptedException, PeripheralInoperableException, PeripheralCommunicationException {
        flMotor.writeFast((-forwardVelocity - rightwardVelocity) * speedFactor * SQRT_2);
        frMotor.writeFast((forwardVelocity - rightwardVelocity) * speedFactor * SQRT_2);
        rlMotor.writeFast((-forwardVelocity + rightwardVelocity) * speedFactor * SQRT_2);
        rrMotor.writeFast((forwardVelocity + rightwardVelocity) * speedFactor * SQRT_2);
    }


    @Override
    public void spinInPlace(double tangentialVelocity) throws PeripheralInoperableException, PeripheralCommunicationException, InterruptedException {
        flMotor.writeFast(-tangentialVelocity * speedFactor * SQRT_2);
        frMotor.writeFast(-tangentialVelocity * speedFactor * SQRT_2);
        rlMotor.writeFast(-tangentialVelocity * speedFactor * SQRT_2);
        rrMotor.writeFast(-tangentialVelocity * speedFactor * SQRT_2);
    }

    @Override
    public void stopAll() throws PeripheralInoperableException, PeripheralCommunicationException, InterruptedException {
        flMotor.writeFast(0.0);
        frMotor.writeFast(0.0);
        rlMotor.writeFast(0.0);
        rrMotor.writeFast(0.0);
    }
}
