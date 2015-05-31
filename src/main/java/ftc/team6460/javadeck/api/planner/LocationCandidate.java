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
 * Describes a candidate location
 */
public class LocationCandidate {
    private final ImmutableRobotPosition position;
    private final double correlationStrength;
    public static int compareDescending(LocationCandidate o1, LocationCandidate o2) {
        return -Double.compare(o1.getCorrelationStrength(), o2.getCorrelationStrength());
    }
    public ImmutableRobotPosition getPosition() {
        return position;
    }

    public double getCorrelationStrength() {
        return correlationStrength;
    }

    @Override
    public String toString() {
        return "LocationCandidate{" +
                "position=" + position +
                ", correlationStrength=" + correlationStrength +
                '}';
    }

    public LocationCandidate(ImmutableRobotPosition position, double correlationStrength) {

        this.position = position;
        this.correlationStrength = correlationStrength;
    }
    public static class Comparator implements java.util.Comparator<LocationCandidate> {
        @Override
        public int compare(LocationCandidate o1, LocationCandidate o2) {
            return Double.compare(o1.getCorrelationStrength(), o2.getCorrelationStrength());
        }
    }
}
