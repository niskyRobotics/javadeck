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

import java.util.*;

/**
 * Created by hexafraction on 4/11/15.
 */
public class Waypoint {
    private final Set<Field.Zone> zones = new HashSet<>();

    private Waypoint(Point2D pos) {
        this.pos = pos;
    }

    private static final HashMap<Point2D, Waypoint> cache = new HashMap<>();
    public static Waypoint fromPos(Point2D pos){
        if(cache.containsKey(pos)) return cache.get(pos);
        else {
            Waypoint wp = new Waypoint(pos);
            cache.put(pos, wp);
            return wp;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Waypoint)) return false;

        Waypoint waypoint = (Waypoint) o;

        if (!pos.equals(waypoint.pos)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    public Tag tag;

    public void disconnect(Waypoint w) {
        this.neighbors.remove(w);
    }

    public class Tag{
        double dist = Double.POSITIVE_INFINITY; Waypoint prev; boolean inQueue = false;

        @Override
        public String toString() {
            return "Tag{" +
                    "dist=" + dist +
                    ", prev=" + prev +
                    ", inQueue=" + inQueue +
                    '}';
        }
    }

    public Set<Waypoint> getNeighbors() {
        return Collections.unmodifiableSet(neighbors);
    }

    public Set<Field.Zone> getZones() {
        return Collections.unmodifiableSet(zones);
    }

    void addZone(Field.Zone z) throws IllegalArgumentException{
        if(z.contains(this.getPos())) zones.add(z);
        else throw new IllegalArgumentException("Waypoint not in zone");
    }


    private final Point2D pos;
    private final Set<Waypoint> neighbors = new HashSet<>();

    public double distanceTo(Waypoint w){
        return GeometryUtils.euclideanDistance(w.pos, this.pos);
    }

    @Override
    public String toString() {
        return "Waypoint{" +
                "zones=" + zones +
                ", tag=" + tag +
                ", pos=" + pos +
                '}';
    }

    void addNeighbor(Waypoint w){
        this.neighbors.add(w);
    }

    public Point2D getPos() {
        return pos;
    }


}
