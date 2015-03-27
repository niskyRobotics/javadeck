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

package ftc.team6460.javadeck.api.planner;

/**
 * Interface allowing physical robot to be moved to a new position. Thread-safety is not required.
 */
public abstract class RobotDrive implements Sensor {
    @Override
    public double getLikelihood(double x, double y, double theta) {
        return 2 * calculateGaussian(x - currentPosition.getX(),
                y - currentPosition.getY(),
                Math.IEEEremainder((theta - currentPosition.getTheta()), 2 * Math.PI)
        ) - 1;
    }

    // calculates a gaussian in 3d around the center
    private double calculateGaussian(double deltaX, double deltaY, double theta) {
        double twoSigmaSquared = (2 * Math.pow(collectedDrift, 2));
        double xExpComponent = Math.pow(deltaX, 2) / twoSigmaSquared;
        double yExpComponent = Math.pow(deltaY, 2) / twoSigmaSquared;
        double thetaExpComponent = Math.pow(theta, 2) / (2 * Math.pow(collectedAngularDrift, 2));
        // blech
        return Math.exp(-1 * (xExpComponent + yExpComponent + thetaExpComponent));
    }

    @Override
    public double getWeight(double x, double y, double theta) {
        return this.weight;
    }

    @Override
    public void notifyError(double x, double y, double theta, double agreedWeight) {
        try {
            // weighted average
            double newX = (x * agreedWeight + currentPosition.getX() * this.weight) / (this.weight + agreedWeight);
            double newY = (y * agreedWeight + currentPosition.getY() * this.weight) / (this.weight + agreedWeight);
            double newTheta = (theta * agreedWeight + currentPosition.getTheta() * this.weight) / (this.weight + agreedWeight);
            // what the heck?
            this.weight = (this.weight + calculateGaussian(x - currentPosition.getX(), y - currentPosition.getY(), theta - currentPosition.getTheta())) / 2;
            this.currentPosition = new ImmutableRobotPosition(newX, newY, newTheta);
        } catch (ArithmeticException e) {
            this.weight = 1;
            this.currentPosition = new ImmutableRobotPosition(x, y, theta);
        }
    }

    /**
     * Operates motors or other effectors to move robot.
     *
     * @param travel            The relative move to perform.
     * @param suppressObstacles True if the robot should ignore obstacle collisions and complete this move.
     * @throws RobotHardwareException Thrown if hardware is inoperable, or the move cannot be performed on the current hardware.
     * @throws ObstacleException      Thrown if an obstacle is collided with during operation
     */
    protected abstract void move0(RelativePosition travel, boolean suppressObstacles) throws RobotHardwareException, ObstacleException;

    protected RobotPosition currentPosition;

    public RobotDrive(RobotPosition currentPosition) {
        this.currentPosition = currentPosition;
    }

    protected double collectedDrift = 0;
    protected double collectedAngularDrift = 0;

    /**
     * Calculates the drift possible for this move, in meters. The value returned should be the smallest error e such that the move's result has a 1/2 probability
     * of being within e units of the actual movement.
     *
     * @param travel The relative move to perform.
     */
    protected abstract double calculateDrift(RelativePosition travel);

    /**
     * Calculates the angular drift possible for this move, in meters. The value returned should be the smallest error α such that the move's result has a 1/2 probability
     * of being within α radians of the actual movement.
     *
     * @param travel The relative move to perform.
     */
    protected abstract double calculateAngularDrift(RelativePosition travel);

    /**
     * Calculates the estimated time for this move.
     *
     * @param travel The move to perform
     * @return The time taken, in seconds.
     */
    public abstract double calculateTime(RelativePosition travel);

    protected double weight = 1;

    /**
     * Operates motors or other effectors to move robot.
     *
     * @param travel            The relative move to perform.
     * @param suppressObstacles True if the robot should ignore obstacle collisions and complete this move.
     * @throws RobotHardwareException Thrown if hardware is inoperable, or the move cannot be performed on the current hardware.
     * @throws ObstacleException      Thrown if an obstacle is collided with during operation
     */
    public final void move(RelativePosition travel, boolean suppressObstacles) throws RobotHardwareException, ObstacleException {
        try {
            move0(travel, suppressObstacles);
        } catch (RobotHardwareException | ObstacleException e) {
            this.weight = 0;
            throw e;
        }
        collectedAngularDrift += calculateAngularDrift(travel);
        collectedDrift += calculateDrift(travel);
        this.currentPosition = travel.apply(currentPosition).materialize();
    }
}
