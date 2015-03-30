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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Calculates the robot's most likely position(s) from a set of sensors. This class is partially thread-safe; see javadocs of specific methods.
 */
public class PositionIntegrator {
    private final Set<Sensor> sensors = new CopyOnWriteArraySet<>();

    /*
     * The maximum size for a fine search area, in units of COARSE_SEARCH_STEP.
     */
    private final int fineSearchMaxDimension;

    /*
     * Minimum ratio of point correlation strength to average correlation strength, to start fine searching.
     */
    private final double islandMinSNR;

    /*
     * Minimum correlation strength to start fine searching
     */
    private final double islandMinAbsolute;

    /*
     * Minimum correlation strength to continue expanding a fine search area.
     */
    private final double islandRemainStrength;


    private static final double COARSE_SEARCH_STEP = 0.10;

    //value should be odd.
    // F F F F F
    // F F F F F
    // F F C F F
    // F F F F F
    // F F F F F
    private static final int FINE_STEP_SUBDIVISIONS = 5;

    public PositionIntegrator(Collection<Sensor> sensors, double fieldX, double fieldY) {
        this(sensors, fieldX, fieldY, 2, 2, 0.8, 0.75);
    }


    public PositionIntegrator(Collection<Sensor> sensors, double fieldX, double fieldY, int fineSearchMaxDimension,
                              double islandMinSNR, double islandMinAbsolute, double islandRemainStrength) {
        this.fineSearchMaxDimension = fineSearchMaxDimension;
        this.islandMinSNR = islandMinSNR;
        this.islandMinAbsolute = islandMinAbsolute;
        this.islandRemainStrength = islandRemainStrength;
        this.sensors.addAll(sensors);
        double firstCoarseVal = COARSE_SEARCH_STEP / FINE_STEP_SUBDIVISIONS * (Math.floor(FINE_STEP_SUBDIVISIONS / 2));
        int coarseX = (int) ((fieldX - firstCoarseVal) / COARSE_SEARCH_STEP);
        int coarseY = (int) ((fieldY - firstCoarseVal) / COARSE_SEARCH_STEP);
    }

    /* Algorithm:
     * 1. Fill search array with Double.NaN
     * 2. Evaluate for all coarse steps, store to array, and average value
     * 3. Sub-fill fine values for all hotspots as returned from sensors
     * 4. Sub-fill all other hotspots where coarseVal/avg>islandMinSNR OR coarseval>islandMinAbsolute
     * 5. Return all location candidates (with correlation strength and island size)
     */

    /**
     * Returns a list of positions, in no particular order, that describes likely robot positions.
     */
    public List<LocationCandidate> getCandidates() {
        return null;
    }

}
