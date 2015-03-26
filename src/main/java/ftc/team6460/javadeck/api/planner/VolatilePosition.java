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
 * A temporary, reusable holder for a position value, designed to limit GC pressure. This is only valid during a single
 * motion planner/sensing integrator iteration, and should not be retained between method calls.
 */
public class VolatilePosition implements RobotPosition {

    private volatile double xPosition;
    private volatile double yPosition;
    private volatile double theta;

    /**
     * Returns the current X position
     */
    @Override
    public double getX() {
        return xPosition;
    }


    /**
     * Returns the current Y position
     */
    @Override
    public double getY() {
        return yPosition;
    }

    /**
     * Returns the current facedir angle
     */
    @Override
    public double getTheta() {
        return theta;
    }

    /**
     * Updates this position
     */
    public void update(double x, double y, double t) {
        this.xPosition = x;
        this.yPosition = y;
        this.theta = t;
    }

    /**
     * Constructs a new robot position container.
     *
     * @param xPosition The position of the robot on the field in the X direction.
     * @param yPosition The position of the robot on the field in the Y direction.
     * @param theta     The angle of the robot.
     */
    public VolatilePosition(double xPosition, double yPosition, double theta) {

        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.theta = theta;
    }

    /**
     * Constructs an immutable copy of this position, that can be saved and retained for any period of time.
     */
    @Override
    public ImmutableRobotPosition materialize() {
        return new ImmutableRobotPosition(this.xPosition, this.yPosition, this.theta);
    }

}
