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

/**
 * This package contains the classes and code for a full robot motion planner.
 *
 * <h2>Costs and benefits</h2>
 * Costs and benefits should be consistent within each set, however no specific correlation between units of cost and units of benefit needs to be made.
 * A reasonable benefit unit might be the number of competition points scored by the robot for completing this task.
 *
 * <h2>Coordinates</h2>
 *
 * Coordinates should be specified in meters, specifying the center of the robot. Generally, the robot's start position is defined to be on the X-axis, such
 * that (0,0) is located at the leftmost of the field, on the side on which the robot is parked to start.
 * X increases to the right, and Y increases forward (away from this start wall). Theta is defined as the angle between the robot's natural "forward" direction and the positive X-axis,
 * resulting from the conversion of rectangular coordinates (x, y) to polar coordinates (r, theta) in the range of -pi to pi, namely using
 * <code>Math.atan2(double,double)</code>. Implementations should accept values outside this range, and may "normalize" them using
 * <code>Math.IEEEremainder(angle, Math.PI*2)</code>.
 *
 * <h2>Floating-point error</h2>
 *
 * Standard floating point error of a few ulps is not an issue as the hardware and sensors have far more tolerance on their readings.
 * However, catastrophic rounding should be avoided.
 */
package ftc.team6460.javadeck.api.planner;