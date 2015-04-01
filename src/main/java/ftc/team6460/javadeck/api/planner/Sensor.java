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
 * Describes a peripheral that can determine the probability of the robot to be at any given point on the field.
 * <p/>
 * Likelihoods do not need to form a probability distribution (i.e. sum or integrate to 1) over the field. For example,
 * if the sensor reading makes it certain that the robot is in a given area, the likelihoods for every point in that area should be 1.
 */
public interface Sensor {
    /**
     * Returns the likelihood of the robot to be at that specific location in that orientation. 1 means that according to this
     * sensor, the robot is undoubtedly able to be at this point (for example, if a color sensor pointed at the floor is reading blue, then
     * that means that the return value for any point which is within a blue region should be 1). 0 means that this position is impossible according to this sensor.
     * Advanced implementations may adjust this value to indicate that the sensor may not be perfectly reliable
     * (for example, to describe a probability distribution for the error in a reading).
     *
     * @param x     The robot position, X.
     * @param y     The robot position, Y.
     * @return The likelihood to be at this point.
     */
    public double getLikelihood(double x, double y);

    /**
     * Returns the likelihood of the robot to be at that specific location in that orientation. 1 means that according to this
     * sensor, the robot is undoubtedly able to be at this point (for example, if a color sensor pointed at the floor is reading blue, then
     * that means that the return value for any point which is within a blue region should be 1). 0 means that this position is impossible according to this sensor.
     * Advanced implementations may adjust this value to indicate that the sensor may not be perfectly reliable
     * (for example, to describe a probability distribution for the error in a reading).
     *
     * @param x     The robot position, X.
     * @param y     The robot position, Y.
     * @param theta The robot orientation, in radians;
     * @return The likelihood to be at this point.
     */
    public double getOrientationLikelihood(double x, double y, double theta);

    /**
     * Returns the "weight" of this sensor for a weighted average, at the specific location and orientation specified. For example, a sensor that
     * is known to be unreliable when near a wall, may return a lower than usual weight for locations near walls.
     *
     * @param x     The robot position, X.
     * @param y     The robot position, Y.
     * @return The sensor weight here.
     */
    public double getWeight(double x, double y);

    /**
     * Returns any meaningful areas at which to suggest to the integrator to focus on for a search. This method is used for
     * time optimization purposes only.
     */
    public Iterable<RobotPosition> getPossibleHotspots();

    /**
     * Handles an error in measurement. This will be called after each sensing and positioning iteration.
     *
     * @param x            The measured robot position, X.
     * @param y            The measured robot position, Y.
     * @param theta        The measured robot orientation.
     * @param agreedWeight How strong the consensus was for this point. 1 indicated unanimous agreement for this point,
     *                     while values arbitrarily close to zero describe arbitrarily weak correlations.
     */
    public void notifyError(double x, double y, double theta, double agreedWeight);

}
