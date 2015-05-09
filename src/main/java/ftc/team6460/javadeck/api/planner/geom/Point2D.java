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

package ftc.team6460.javadeck.api.planner.geom;

import ftc.team6460.javadeck.api.planner.ImmutableRobotPosition;
import ftc.team6460.javadeck.api.planner.RobotPosition;

/**
 * Describes a position without orientation on the field. The numeric values are fixed-point decimal, defined
 * such that a value of 1 describes an offset of 1mm.
 */
public class Point2D {

    private final ImmutableRobotPosition cachedPos;

    public ImmutableRobotPosition getAsRobotPos() {
        return cachedPos;
    }

    public ImmutableRobotPosition getAsRobotPos(double theta) {
        return new ImmutableRobotPosition(x / 1000.0, y / 1000.0, theta);
    }
    @Override
    public String toString() {
        return "Point2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point2D)) return false;

        Point2D that = (Point2D) o;

        if (x != that.x) return false;
        if (y != that.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (x ^ (x >>> 32));
        result = 31 * result + (int) (y ^ (y >>> 32));
        return result;
    }

    public static Point2D fromRobotPosition(RobotPosition rPos) {
        return new Point2D(Math.round(rPos.getX() * 1000), Math.round(rPos.getY() * 1000));
    }

    public long getX() {
        return x;
    }

    public long getY() {
        return y;
    }


    public Point2D(long x, long y) {

        this.x = x;
        this.y = y;
        cachedPos = new ImmutableRobotPosition(x / 1000.0, y / 1000.0, 0);
    }

    final long x;
    final long y;
}
