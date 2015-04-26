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
 * Describes a drivetrain with two sides both making parallel contributions to the robot's motion
 * (e.g. 4 wheels laid out similarly to those on an automobile without steering, or two tank treads mounted on opposite sides)
 * The motor polarity is assumed such that positive power causes that side of robot to move forward
 */
public class ParallelDrivetrain implements VelocityDrivetrain {

    private final EffectorPeripheral<Double> lMotor, rMotor;
    /**
     * Raw speed to motor needed to reach 1 meter/sec.
     */
    private final double speedFactor;

    public ParallelDrivetrain(EffectorPeripheral<Double> lMotor, EffectorPeripheral<Double> rMotor, double speedFactor) {
        this.lMotor = lMotor;
        this.rMotor = rMotor;
        this.speedFactor = speedFactor;
    }

    @Override
    public void setVelocity(double forwardVelocity) throws PeripheralInoperableException, PeripheralCommunicationException, InterruptedException {
        lMotor.writeFast(forwardVelocity * speedFactor);
        rMotor.writeFast(forwardVelocity * speedFactor);
    }

    @Override
    public void spinInPlace(double tangentialVelocity) throws PeripheralInoperableException, PeripheralCommunicationException, InterruptedException {
        lMotor.writeFast(tangentialVelocity * speedFactor);
        rMotor.writeFast(-tangentialVelocity * speedFactor);
    }



    @Override
    public void stopAll() throws PeripheralInoperableException, PeripheralCommunicationException, InterruptedException {
        lMotor.writeFast(0.0);
        rMotor.writeFast(0.0);
    }
}
