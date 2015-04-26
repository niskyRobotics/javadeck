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

import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;
import ftc.team6460.javadeck.api.planner.*;

/**
 * Represents a robot with two independently-controlled sides (pairs of wheels facing same direction,
 * tank treads, etc).
 */
public class MotionDrivetrain extends RobotDrive {

    // need encoders to function reliably. Assuming PID exists in the SDK. If not, I will have a TODO here for implementing it manually.

    private final EncoderedMotor left, right;

    /**
     * Robot width in meters
     */
    private final double robotWidth;

    /**
     * Maximum acceleration, in meters/sec^2, to allow.
     */
    private final double accel;

    /**
     * Raw speed to motor needed to reach 1 meter/sec.
     */
    private final double speedFactor;

    /**
     * Max speed allowed in meters/sec
     */
    private final double maxSpeed;


    public MotionDrivetrain(RobotPosition currentPosition, EncoderedMotor left, EncoderedMotor right, double robotWidth, double accel, double speedFactor, double maxSpeed) {
        super(currentPosition);
        this.left = left;
        this.right = right;
        this.robotWidth = robotWidth;
        this.accel = accel;
        this.speedFactor = speedFactor;
        this.maxSpeed = maxSpeed;
    }

    // TODO handle obstacle collisions
    @Override
    protected void move0(RelativePosition travel, boolean suppressObstacles) throws RobotHardwareException, ObstacleException {
        double thetaNeeded = travel.getTheta();
        if (thetaNeeded != 0) {
            double circumDistance = Math.abs(thetaNeeded * robotWidth / 2);
            double distDuringAccel = Math.max(0.5 * maxSpeed * maxSpeed / accel, circumDistance / 2);
            double holdTime = (distDuringAccel * 2 - circumDistance) / maxSpeed;
            double accelTime = Math.sqrt(2 * distDuringAccel / accel);
            assert (holdTime >= 0) : "holding speed for negative time";
            double lMtrSignum = Math.signum(circumDistance);
            double rMtrSignum = -lMtrSignum;
            // 1/2 a t^2, but twice due to decel time as well
            assert (Math.abs(circumDistance - (accel * accelTime * accelTime + holdTime * maxSpeed)) < 0.001);
            assert (accelTime * accel <= maxSpeed);


            try {
                long tStart = System.currentTimeMillis();
                // accelerate
                long millisNow = 0;
                double spd;
                while (millisNow < accelTime) {
                    spd = millisNow * accel;
                    assert (spd <= maxSpeed);
                    millisNow = System.currentTimeMillis() - tStart;
                    left.writeFast(spd * lMtrSignum);
                    right.writeFast(spd * rMtrSignum);
                    // hardware writes occur every 50msec
                    Thread.sleep(25);
                }

                while (millisNow < (accelTime + holdTime)) {
                    spd = maxSpeed;
                    left.writeFast(spd * lMtrSignum);
                    right.writeFast(spd * rMtrSignum);
                }
                while (millisNow < (2 * accelTime + holdTime)) {
                    spd = (2 * accelTime + holdTime - millisNow) * accel;
                    assert (spd <= maxSpeed);
                    assert (spd >= 0);
                    millisNow = System.currentTimeMillis() - tStart;
                    left.writeFast(spd * lMtrSignum);
                    right.writeFast(spd * rMtrSignum);
                    // hardware writes occur every 50msec
                    Thread.sleep(25);
                }
                left.write(0.0);
                right.write(0.0);


            } catch (InterruptedException | PeripheralInoperableException | PeripheralCommunicationException e) {
                throw new RobotHardwareException(e);
            }
        }

        double distance = travel.getDistance();
        double distDuringAccel = Math.max(0.5 * maxSpeed * maxSpeed / accel, distance / 2);
        double holdTime = (distDuringAccel * 2 - distance) / maxSpeed;
        double accelTime = Math.sqrt(2 * distDuringAccel / accel);
        assert (holdTime >= 0) : "holding speed for negative time";

        // 1/2 a t^2, but twice due to decel time as well
        assert (Math.abs(distance - (accel * accelTime * accelTime + holdTime * maxSpeed)) < 0.001);
        assert (accelTime * accel <= maxSpeed);


        try {
            long tStart = System.currentTimeMillis();
            // accelerate
            long millisNow = 0;
            double spd;
            while (millisNow < accelTime) {
                spd = millisNow * accel;
                assert (spd <= maxSpeed);
                millisNow = System.currentTimeMillis() - tStart;
                left.writeFast(spd);
                right.writeFast(spd);
                // hardware writes occur every 50msec
                Thread.sleep(25);
            }

            while (millisNow < (accelTime + holdTime)) {
                spd = maxSpeed;
                left.writeFast(spd);
                right.writeFast(spd);
            }
            while (millisNow < (2 * accelTime + holdTime)) {
                spd = (2 * accelTime + holdTime - millisNow) * accel;
                assert (spd <= maxSpeed);
                assert (spd >= 0);
                millisNow = System.currentTimeMillis() - tStart;
                left.writeFast(spd);
                right.writeFast(spd);
                // hardware writes occur every 50msec
                Thread.sleep(25);
            }
            left.write(0.0);
            right.write(0.0);


        } catch (InterruptedException | PeripheralInoperableException | PeripheralCommunicationException e) {
            throw new RobotHardwareException(e);
        }
        this.collectedAngularDrift += travel.getTheta() / 1000;
        this.collectedDrift += travel.getDistance() / 1000;
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
