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

import java.util.*;
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
    private final double correlatorMinSNR;

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
        this(sensors, fieldX, fieldY, 2, 5, 0.95, 0.75);
    }


    public PositionIntegrator(Collection<Sensor> sensors, double fieldX, double fieldY, int fineSearchMaxDimension,
                              double correlatorMinSNR, double islandMinAbsolute, double islandRemainStrength) {
        this.fineSearchMaxDimension = fineSearchMaxDimension;
        this.correlatorMinSNR = correlatorMinSNR;
        this.islandMinAbsolute = islandMinAbsolute;
        this.islandRemainStrength = islandRemainStrength;
        this.sensors.addAll(sensors);
        firstCoarseVal = COARSE_SEARCH_STEP / FINE_STEP_SUBDIVISIONS * (Math.floor(FINE_STEP_SUBDIVISIONS / 2));
        this.fieldX = fieldX;
        int coarseX = (int) Math.ceil((this.fieldX - firstCoarseVal) / COARSE_SEARCH_STEP);
        this.fieldY = fieldY;
        int coarseY = (int) Math.ceil((this.fieldY - firstCoarseVal) / COARSE_SEARCH_STEP);
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
     * 4. Sub-fill all other hotspots where coarseVal/avg>correlatorMinSNR OR coarseval>islandMinAbsolute
     * 5. Return all location candidates (with correlation strength and island size)
     */

    /**
     * Returns a list of positions, in no particular order, that describes likely robot positions.
     */
    public List<LocationCandidate> getCandidates(double minCorr) {
        double total = 0;
        for (int x = 0; x < cX; x++) {
            for (int y = 0; y < cY; y++) {
                // reference!
                Tile t = tiles[x][y];
                t.hasFine = false;
                t.zero();
                t.coarse = calculateWeighted(getDimension(x), getDimension(y));
                total += t.coarse;
            }
        }
        double avg = total / (cX * cY);

        for (Sensor s : sensors) {
            for (RobotPosition lc : s.getPossibleHotspots()) {
                //System.out.println("lc = " + lc);
                fillIn(lc.getX(), lc.getY());
            }
        }

        for (int x = 0; x < cX; x++) {
            for (int y = 0; y < cY; y++) {
                if (tiles[x][y].coarse > islandMinAbsolute || tiles[x][y].coarse > (avg * correlatorMinSNR)) {
                    //System.out.println("x = " + x + ", y = " + y);
                    fillInPos(x, y);
                }
            }
        }

        List<LocationCandidate> candidates = new ArrayList<>();

        for (int x = 0; x < cX; x++) {
            for (int y = 0; y < cY; y++) {
                if (checkRelMax(x, y) && tiles[x][y].getMax() > minCorr) {
                    IntPair fine = tiles[x][y].getMaxPos();
                    double xPos = getFineDimension(x, fine.x);
                    double yPos = getFineDimension(y, fine.y);
                    candidates.add(new LocationCandidate(new ImmutableRobotPosition(xPos, yPos, 0), tiles[x][y].getMax()));
                }
            }
        }

        return candidates;
    }

    // actually checks if this max is at least correlatorMinSNR the surrounding average
    private boolean checkRelMax(int x, int y) {
        double max = tiles[x][y].getMax();
        if (max < (correlatorMinSNR * getSafeAvg(x + 1, y - 1)) && max < islandMinAbsolute) return false;
        if (max < (correlatorMinSNR * getSafeAvg(x + 1, y)) && max < islandMinAbsolute) return false;
        if (max < (correlatorMinSNR * getSafeAvg(x + 1, y + 1)) && max < islandMinAbsolute) return false;
        if (max < (correlatorMinSNR * getSafeAvg(x, y - 1)) && max < islandMinAbsolute) return false;
        if (max < (correlatorMinSNR * getSafeAvg(x, y)) && max < islandMinAbsolute) return false;
        if (max < (correlatorMinSNR * getSafeAvg(x, y + 1)) && max < islandMinAbsolute) return false;
        if (max < (correlatorMinSNR * getSafeAvg(x + 1, y - 1)) && max < islandMinAbsolute) return false;
        if (max < (correlatorMinSNR * getSafeAvg(x + 1, y)) && max < islandMinAbsolute) return false;
        if (max < (correlatorMinSNR * getSafeAvg(x + 1, y + 1)) && max < islandMinAbsolute) return false;

        return true;
    }

    private double getSafeMax(int x, int y) {
        if (x > cX || x < 0 || y > cY || y < 0) {
            return 0;
        } else return tiles[x][y].getMax();
    }

    private double getSafeAvg(int x, int y) {
        if (x >= cX || x < 0 || y >= cY || y < 0) {
            return 0;
        } else return tiles[x][y].getAvg();
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

    private void fillInPos(int x, int y) {
        fillIn0(x, y, fineSearchMaxDimension);
    }

    // what the heck, recursion?
    private void fillIn0(int xT, int yT, int maxDim) {
        //System.out.println("xT = [" + xT + "], yT = [" + yT + "], maxDim = [" + maxDim + "], cY = " + cY);

        if (!(xT >= 0 && xT < cX)) return;
        if (!(yT >= 0 && yT < cY)) return;
        Tile t = tiles[xT][yT];
        if (t.hasFine) return;
        t.hasFine = true;
        assert t.fine.length == FINE_STEP_SUBDIVISIONS : "mis-sized fine array";
        assert t.fine[0].length == FINE_STEP_SUBDIVISIONS : "mis-sized fine array";
        double max = 0;
        for (int x = -FINE_STEPS_IN_EACH_DIRECTION; x <= FINE_STEPS_IN_EACH_DIRECTION; x++) {
            for (int y = -FINE_STEPS_IN_EACH_DIRECTION; y <= FINE_STEPS_IN_EACH_DIRECTION; y++) {
                double val = calculateWeighted(getFineDimension(xT, x), getFineDimension(yT, y));
                t.fine[x + FINE_STEPS_IN_EACH_DIRECTION][y + FINE_STEPS_IN_EACH_DIRECTION] = val;
                max = Math.max(val, max);
            }

            if (max > islandRemainStrength && maxDim > 0) {
                //System.out.println("RECURSING!");
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
    }

    private double calculateWeighted(double x, double y) {
        assert x <= fieldX && x >= 0 : "fieldX out of bounds";
        assert y <= fieldY && y >= 0 : "fieldY out of bounds";
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
    private static class Tile {
        private static final IntPair COARSE_POSITION = new IntPair(FINE_STEPS_IN_EACH_DIRECTION, FINE_STEPS_IN_EACH_DIRECTION);

        double coarse;
        double[][] fine;
        boolean hasFine = false;

        Tile(int fineSubdivs) {
            fine = new double[fineSubdivs][fineSubdivs];
        }


        //memoize? Fast!
        public double getAvg() {
            if (!hasFine) return coarse;
            else {
                double sum = 0;
                for (int x = -FINE_STEPS_IN_EACH_DIRECTION; x <= FINE_STEPS_IN_EACH_DIRECTION; x++) {
                    for (int y = -FINE_STEPS_IN_EACH_DIRECTION; y <= FINE_STEPS_IN_EACH_DIRECTION; y++) {
                        sum += fine[x + FINE_STEPS_IN_EACH_DIRECTION][y + FINE_STEPS_IN_EACH_DIRECTION];
                    }
                }
                return sum / (FINE_STEP_SUBDIVISIONS * FINE_STEP_SUBDIVISIONS);
            }
        }

        public void zero() {
            this.hasFine = false;
            this.coarse = 0;
            for (int x = 0; x < fine.length; x++) {
                for (int y = 0; y < fine[x].length; y++) {
                    fine[x][y]=0;
                }
            }
        }


        //memoize? Fast!
        public double getMax() {
            if (!hasFine) return coarse;
            else {
                double max = 0;
                for (int x = -FINE_STEPS_IN_EACH_DIRECTION; x <= FINE_STEPS_IN_EACH_DIRECTION; x++) {
                    for (int y = -FINE_STEPS_IN_EACH_DIRECTION; y <= FINE_STEPS_IN_EACH_DIRECTION; y++) {
                        max = Math.max(max, fine[x + FINE_STEPS_IN_EACH_DIRECTION][y + FINE_STEPS_IN_EACH_DIRECTION]);
                    }
                }
                return max;
            }
        }

        //memoize? Fast!
        public IntPair getMaxPos() {
            if (!hasFine) return COARSE_POSITION;
            else {
                int xPos = 0;
                int yPos = 0;
                double max = 0;
                for (int x = -FINE_STEPS_IN_EACH_DIRECTION; x <= FINE_STEPS_IN_EACH_DIRECTION; x++) {
                    for (int y = -FINE_STEPS_IN_EACH_DIRECTION; y <= FINE_STEPS_IN_EACH_DIRECTION; y++) {
                        double val = fine[x + FINE_STEPS_IN_EACH_DIRECTION][y + FINE_STEPS_IN_EACH_DIRECTION];
                        if (val > max) {
                            xPos = x;
                            yPos = y;
                            max = val;
                        }
                    }
                }
                //System.out.println("Returning "+xPos+","+yPos);
                return new IntPair(xPos, yPos);
            }
        }


    }

    public static class IntPair {
        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public IntPair(int x, int y) {

            this.x = x;
            this.y = y;
        }

        final int x, y;
    }

}
