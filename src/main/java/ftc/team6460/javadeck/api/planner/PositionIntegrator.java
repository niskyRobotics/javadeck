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

    private final double cX, cY;

    private static final double COARSE_SEARCH_STEP = 0.10;

    //value should be odd.
    // F F F F F
    // F F F F F
    // F F C F F
    // F F F F F
    // F F F F F
    private static final int FINE_STEP_SUBDIVISIONS = 5;
    private static final int FINE_STEPS_IN_EACH_DIRECTION = (int) (FINE_STEP_SUBDIVISIONS / 2);

    private final double firstCoarseVal;
    private final double fieldX;
    private final double fieldY;

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
        firstCoarseVal = COARSE_SEARCH_STEP / FINE_STEP_SUBDIVISIONS * (Math.floor(FINE_STEP_SUBDIVISIONS / 2));
        this.fieldX = fieldX;
        int coarseX = (int) ((this.fieldX - firstCoarseVal) / COARSE_SEARCH_STEP);
        this.fieldY = fieldY;
        int coarseY = (int) ((this.fieldY - firstCoarseVal) / COARSE_SEARCH_STEP);
        tiles = new Tile[coarseX][coarseY];
        for (int i = 0; i < coarseX; i++) {
            for (int j = 0; j < coarseY; j++) {
                tiles[i][j] = new Tile(FINE_STEP_SUBDIVISIONS);
            }
        }
        this.cX = coarseX;
        this.cY = coarseY;
    }

    // if too slow, optimize as single-dim array
    // tiles[coarseX][coarseY]
    private final /*mutable*/ Tile[][] tiles;

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

        for (int x = 0; x < cX; x++) {
            for (int y = 0; y < cY; y++) {
                // reference!
                Tile t = tiles[x][y];
                t.hasFine = false;
                t.coarse = calculateWeighted(getDimension(x), getDimension(y));

            }
        }

        for (Sensor s : sensors) {
            for (RobotPosition lc : s.getPossibleHotspots()) {
                fillIn(lc.getX(), lc.getY());
            }
        }


        return null;
    }

    private double getDimension(int idx) {
        return idx * COARSE_SEARCH_STEP + firstCoarseVal;
    }

    private double getFineDimension(int idx, int fine) {
        return idx * COARSE_SEARCH_STEP + firstCoarseVal + (fine * COARSE_SEARCH_STEP / FINE_STEP_SUBDIVISIONS);
    }

    // Unit test the crap out of this
    private int getNearest(double dim) {
        return (int) Math.round((dim - firstCoarseVal) / COARSE_SEARCH_STEP);
    }

    private void fillIn(double x, double y) {
        int xT = getNearest(x);
        int yT = getNearest(y);
        fillIn0(xT, yT, fineSearchMaxDimension);
    }

    // what the heck, recursion?
    private void fillIn0(int xT, int yT, int maxDim) {
        assert xT >= 0 && xT < cX : "out of bound tile x";
        assert yT >= 0 && yT < cY : "out of bound tile y";
        Tile t = tiles[xT][yT];
        if (t.hasFine) return;
        assert t.fine.length == FINE_STEP_SUBDIVISIONS : "mis-sized fine array";
        assert t.fine[0].length == FINE_STEP_SUBDIVISIONS : "mis-sized fine array";
        double max = 0;
        for (int x = -FINE_STEPS_IN_EACH_DIRECTION; x <= FINE_STEPS_IN_EACH_DIRECTION; x++) {
            for (int y = -FINE_STEPS_IN_EACH_DIRECTION; y <= FINE_STEPS_IN_EACH_DIRECTION; y++) {
                double val = calculateWeighted(getFineDimension(xT, x), getFineDimension(yT, y));
                max = Math.max(val, max);
            }
        }
        if (max > islandRemainStrength && maxDim > 0) {

            // most of these return quickly. Optimize if needed.
            fillIn0(xT + 1, yT, maxDim - 1);
            fillIn0(xT + 1, yT + 1, maxDim - 1);
            fillIn0(xT, yT + 1, maxDim - 1);
            fillIn0(xT - 1, yT + 1, maxDim - 1);
            fillIn0(xT - 1, yT, maxDim - 1);
            fillIn0(xT - 1, yT - 1, maxDim - 1);
            fillIn0(xT, yT - 1, maxDim - 1);
            fillIn0(xT + 1, yT - 1, maxDim - 1);
        }

    }

    private double calculateWeighted(double x, double y) {
        assert x < fieldX && x > 0 : "fieldX out of bounds";
        assert y < fieldY && y > 0 : "fieldY out of bounds";
        double sum = 0;
        double weights = 0;
        for (Sensor s : sensors) {
            double weight = s.getWeight(x, y);
            sum += (s.getLikelihood(x, y) * weight);
            weights += weight;
        }
        return sum / weights;
    }

    // instances of this class are re-used often, for performance reasons.
    private class Tile {
        double coarse;
        double[][] fine;
        boolean hasFine = false;

        Tile(int fineSubdivs) {
            fine = new double[fineSubdivs][fineSubdivs];
        }
    }

}
