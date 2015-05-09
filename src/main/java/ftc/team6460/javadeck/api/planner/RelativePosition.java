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
 * Describes a position relative to the robot's current position and orientation.
 */
public class RelativePosition {
    private final double distance;
    private final double theta;

    @Override
    public String toString() {
        return "RelativePosition{" +
                "distance=" + distance +
                ", theta=" + theta +
                '}';
    }

    /**
     * Returns the distance in meters.
     */
    public double getDistance() {
        return distance;
    }

    /**
     * The heading to this position, as an angle in radians, between -pi and pi, where positive values are right of face direction and negative values are left.
     */
    public double getTheta() {
        return theta;
    }

    /**
     * Constructs a new relative position object
     *
     * @param distance The distance, in meters
     * @param theta    An angle in radians, between -pi and pi, where positive values are right of face direction and negative values are left.
     */
    public RelativePosition(double distance, double theta) {
        this.distance = distance;
        this.theta = theta;
    }

    /**
     * Applies this relative position as a transformation on an existing position.
     *
     * @param init The initial position.
     * @return An applied position, with the same freshness guarantees as the parameter init.
     */
    public RobotPosition apply(final RobotPosition init) {
        return new RobotPosition() {
            @Override
            public double getX() {
                return init.getX() + RelativePosition.this.distance * Math.cos(init.getTheta() - RelativePosition.this.theta);
            }

            @Override
            public double getY() {
                return init.getY() + RelativePosition.this.distance * Math.sin(init.getTheta() - RelativePosition.this.theta);
            }

            @Override
            public double getTheta() {
                return init.getTheta() - RelativePosition.this.theta;
            }

            @Override
            public ImmutableRobotPosition materialize() {
                return new ImmutableRobotPosition(this.getX(), this.getY(), this.getTheta());
            }
        };
    }

    public static RelativePosition between(RobotPosition start, RobotPosition end) {
        double dist = Math.hypot(start.getX() - end.getX(), start.getY() - end.getY());
        double thetaRaw = Math.atan2(end.getY() - start.getY(), end.getX() - start.getX());
        double thetaToTurn = Math.IEEEremainder(start.getTheta()-thetaRaw, 2 * Math.PI);
        return new RelativePosition(dist, thetaToTurn);
    }

}
