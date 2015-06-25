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
import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;
import ftc.team6460.javadeck.api.planner.*;

/**
 * Represents a drivetrain controlled by relative movements. Note: If an instance of HolonomicDrivetrain is passed, then this class will perform optimizations.
 */
public class PositionBasedDrivetrain extends RobotDrive {

    private static final double ASSERTION_ALLOWED_ERROR = 0.001;
    private static final int MSEC_PER_CONTROL_ITER = 25;

    public PositionBasedDrivetrain(RobotPosition currentPosition, VelocityDrivetrain vd, double robotWidth, double accel, double maxSpeed) {
        super(currentPosition);
        this.vd = vd;
        this.holonomicOptimization = vd instanceof HolonomicDrivetrain;
        this.robotWidth = robotWidth;
        this.accel = accel;
        this.maxSpeed = maxSpeed;
    }

    /* *
     * Constructs a drivetrain controller with holonomic optimizations force-disabled
     */
    /*
    public PositionBasedDrivetrain(RobotPosition currentPosition, VelocityDrivetrain vd, double robotWidth, double accel, double maxSpeed) {
        super(currentPosition);
        this.vd = vd;
        holonomicOptimization = false;
        this.robotWidth = robotWidth;
        this.accel = accel;
        this.maxSpeed = maxSpeed;
    }*/

    // need encoders to function reliably. PID known to exist in SDK

    private final boolean holonomicOptimization;
    private double holonomicAngleOffset;
    private final VelocityDrivetrain vd;

    /**
     * Robot width in meters
     */
    private final double robotWidth;

    /**
     * Maximum acceleration, in meters/sec^2, to allow.
     */
    private final double accel;


    /**
     * Max speed allowed in meters/sec
     */
    private final double maxSpeed;


    // TODO handle obstacle collisions
    @Override
    protected void move0(RelativePosition travel, boolean suppressObstacles) throws RobotHardwareException, ObstacleException {
        //if (!holonomicOptimization) {
        this.doStandardMove(travel);
//
        //} else {
        //     doHolonomicOptimizedMove(travel);


        //}
        collectedAngularDrift += travel.getTheta() / 1000;
        collectedDrift += travel.getDistance() / 1000;
    }

    private void doStandardMove(RelativePosition travel) throws RobotHardwareException {
        double thetaNeeded = travel.getTheta();
        if (thetaNeeded != 0) {
            double circumDistance = Math.abs(thetaNeeded * this.robotWidth / 2);
            double distDuringAccel = Math.max(0.5 * this.maxSpeed * this.maxSpeed / this.accel, circumDistance / 2);
            double holdTime = (distDuringAccel * 2 - circumDistance) / this.maxSpeed;
            double accelTime = Math.sqrt(2 * distDuringAccel / this.accel);
            assert holdTime >= 0 : "holding speed for negative time";
            double spdSignum = Math.signum(circumDistance);
            // 1/2 a t^2, but twice due to decel time as well
            assert Math.abs(circumDistance - (this.accel * accelTime * accelTime + holdTime * this.maxSpeed)) < ASSERTION_ALLOWED_ERROR;
            assert accelTime * this.accel <= this.maxSpeed;


            try {
                long tStart = System.currentTimeMillis();
                // accelerate
                long millisNow = 0;
                double spd;
                while (millisNow < accelTime) {
                    spd = millisNow * this.accel;
                    assert spd <= this.maxSpeed;
                    millisNow = System.currentTimeMillis() - tStart;
                    this.vd.spinInPlace(spd * spdSignum);
                    // hardware writes occur every 50msec
                    Thread.sleep(PositionBasedDrivetrain.MSEC_PER_CONTROL_ITER);
                }

                while (millisNow < accelTime + holdTime) {
                    spd = this.maxSpeed;
                    this.vd.spinInPlace(spd * spdSignum);
                    millisNow = System.currentTimeMillis() - tStart;
                    Thread.sleep(PositionBasedDrivetrain.MSEC_PER_CONTROL_ITER);
                }
                while (millisNow < 2 * accelTime + holdTime) {
                    spd = (2 * accelTime + holdTime - millisNow) * this.accel;
                    assert spd <= this.maxSpeed;
                    assert spd >= 0;
                    millisNow = System.currentTimeMillis() - tStart;
                    this.vd.spinInPlace(spd * spdSignum);
                    // hardware writes occur every 50msec
                    Thread.sleep(PositionBasedDrivetrain.MSEC_PER_CONTROL_ITER);
                }
                this.vd.stopAll();


            } catch (InterruptedException | PeripheralInoperableException | PeripheralCommunicationException e) {
                throw new RobotHardwareException(e);
            }
        }

        double distance = travel.getDistance();
        double distDuringAccel = Math.max(0.5 * this.maxSpeed * this.maxSpeed / this.accel, distance / 2);
        double holdTime = (distDuringAccel * 2 - distance) / this.maxSpeed;
        double accelTime = Math.sqrt(2 * distDuringAccel / this.accel);
        assert holdTime >= 0 : "holding speed for negative time";

        // 1/2 a t^2, but twice due to decel time as well
        assert Math.abs(distance - (this.accel * accelTime * accelTime + holdTime * this.maxSpeed)) < ASSERTION_ALLOWED_ERROR;
        assert accelTime * this.accel <= this.maxSpeed;


        try {
            long tStart = System.currentTimeMillis();
            // accelerate
            long millisNow = 0;
            double spd;
            while (millisNow < accelTime) {
                spd = millisNow * this.accel;
                assert spd <= this.maxSpeed;
                this.vd.setVelocity(spd);
                // hardware writes occur every 50msec
                Thread.sleep(PositionBasedDrivetrain.MSEC_PER_CONTROL_ITER);
                millisNow = System.currentTimeMillis() - tStart;
            }

            while (millisNow < accelTime + holdTime) {
                spd = this.maxSpeed;
                this.vd.setVelocity(spd);
                Thread.sleep(PositionBasedDrivetrain.MSEC_PER_CONTROL_ITER);
                millisNow = System.currentTimeMillis() - tStart;
            }
            while (millisNow < 2 * accelTime + holdTime) {
                spd = (2 * accelTime + holdTime - millisNow) * this.accel;
                assert spd <= this.maxSpeed;
                assert spd >= 0;
                this.vd.setVelocity(spd);
                // hardware writes occur every 50msec
                Thread.sleep(PositionBasedDrivetrain.MSEC_PER_CONTROL_ITER);
                millisNow = System.currentTimeMillis() - tStart;
            }
            this.vd.stopAll();


        } catch (InterruptedException | PeripheralInoperableException | PeripheralCommunicationException e) {
            throw new RobotHardwareException(e);
        }
    }

    @SuppressWarnings("CastToConcreteClass")
    private void doHolonomicOptimizedMove(RelativePosition travel) throws RobotHardwareException {
        // holonomic optimization
        // negative due to choice of orientation
        this.holonomicAngleOffset -= travel.getTheta();
        double distance = travel.getDistance();
        double distDuringAccel = Math.max(0.5 * this.maxSpeed * this.maxSpeed / this.accel, distance / 2);
        double holdTime = (distDuringAccel * 2 - distance) / this.maxSpeed;
        double accelTime = Math.sqrt(2 * distDuringAccel / this.accel);
        assert holdTime >= 0 : "holding speed for negative time";

        // 1/2 a t^2, but twice due to decel time as well
        assert Math.abs(distance - (this.accel * accelTime * accelTime + holdTime * this.maxSpeed)) < ASSERTION_ALLOWED_ERROR;
        assert accelTime * this.accel <= this.maxSpeed;


        try {
            long tStart = System.currentTimeMillis();
            // accelerate
            long millisNow = 0;
            double spd;
            while (millisNow < accelTime) {
                spd = millisNow * this.accel;
                assert spd <= this.maxSpeed;
                ((HolonomicDrivetrain) this.vd).set2DVelocity(spd * Math.cos(this.holonomicAngleOffset), spd * Math.sin(this.holonomicAngleOffset));
                // hardware writes occur every 50msec
                Thread.sleep(PositionBasedDrivetrain.MSEC_PER_CONTROL_ITER);
                millisNow = System.currentTimeMillis() - tStart;
            }

            while (millisNow < accelTime + holdTime) {
                spd = this.maxSpeed;
                ((HolonomicDrivetrain) this.vd).set2DVelocity(spd * Math.cos(this.holonomicAngleOffset), spd * Math.sin(this.holonomicAngleOffset));

                Thread.sleep(PositionBasedDrivetrain.MSEC_PER_CONTROL_ITER);
                millisNow = System.currentTimeMillis() - tStart;

            }
            while (millisNow < 2 * accelTime + holdTime) {
                spd = (2 * accelTime + holdTime - millisNow) * this.accel;
                assert spd <= this.maxSpeed;
                assert spd >= 0;
                ((HolonomicDrivetrain) this.vd).set2DVelocity(spd * Math.cos(this.holonomicAngleOffset), spd * Math.sin(this.holonomicAngleOffset));
                // hardware writes occur every 50msec
                Thread.sleep(PositionBasedDrivetrain.MSEC_PER_CONTROL_ITER);
                millisNow = System.currentTimeMillis() - tStart;
            }
            this.vd.stopAll();
        } catch (InterruptedException | PeripheralInoperableException | PeripheralCommunicationException e) {
            throw new RobotHardwareException(e);
        }
    }

    @Override
    protected double calculateDrift(RelativePosition travel) {
        return 0;
    }

    @Override
    protected double calculateAngularDrift(RelativePosition travel) {
        return 0;
    }

    @Override
    public double calculateTime(RelativePosition travel) {
        return 0;
    }
}
