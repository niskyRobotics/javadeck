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
 * Describes a robot position, that should not change. This may be used for things such as goal locations or logs of old data.
 *
 * @see ftc.team6460.javadeck.api.planner The package-level javadoc describing coordinate systems and conventions.
 */
public class ImmutableRobotPosition implements RobotPosition {
    private final double xPosition;
    private final double yPosition;
    private final double theta;

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

    @Override
    public ImmutableRobotPosition materialize() {
        // immutable anyway
        return this;
    }

    /**
     * Constructs a new robot position container.
     *
     * @param xPosition The position of the robot on the field in the X direction.
     * @param yPosition The position of the robot on the field in the Y direction.
     * @param theta     The angle of the robot.
     */
    public ImmutableRobotPosition(double xPosition, double yPosition, double theta) {

        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.theta = theta;
    }
}
