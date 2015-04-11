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
    public Waypoint fromPos(Point2D pos){
        if(cache.containsKey(pos)) return cache.get(pos);
        else {
            Waypoint wp = new Waypoint(pos);
            cache.put(pos, wp);
            return wp;
        }
    }

    public Set<WaypointConnection> getConnections() {
        return Collections.unmodifiableSet(connections);
    }

    public Set<Field.Zone> getZones() {
        return Collections.unmodifiableSet(zones);
    }

    void addZone(Field.Zone z) throws IllegalArgumentException{
        if(z.contains(this.getPos())) zones.add(z);
        else throw new IllegalArgumentException("Waypoint not in zone");
    }


    private final Point2D pos;
    private final Set<WaypointConnection> connections = new HashSet<>();

    public Point2D getPos() {
        return pos;
    }

    public static class WaypointConnection {

        private final Waypoint w1, w2;
        private final double cost;

        public Waypoint getW1() {
            return w1;
        }

        public Waypoint getW2() {
            return w2;
        }

        public WaypointConnection(Waypoint w1, Waypoint w2, double cost) {

            this.w1 = w1;
            this.w2 = w2;
            this.cost = cost;
        }

        public double getCost() {
            return cost;
        }


    }
}
