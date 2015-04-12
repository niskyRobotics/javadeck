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

import ftc.team6460.javadeck.api.planner.ObstacleException;

import java.util.*;

/**
 * Created by hexafraction on 4/11/15.
 */
public class Field {
    private final long maxX, maxY;
    private final Set<Zone> zones = new HashSet<>();
    private final Set<Waypoint> waypoints = new HashSet<>();

    public boolean addZone(Zone zone) {
        return zones.add(zone);
    }

    /**
     * The numeric values are fixed-point decimal, defined
     * such that a value of 1 describes an offset of 1mm.
     */
    public Field(long maxX, long maxY, Zone... zones) {
        this.maxX = maxX;
        this.maxY = maxY;
        for (Zone z : zones) {
            this.zones.add(z);
        }
    }

    public void addWaypoint(Waypoint waypoint) throws DuplicateWaypointException, ObstacleException {
        if (waypoints.contains(waypoint)) {
            throw new DuplicateWaypointException("Duplicate: " + waypoint.toString());
        }

        for (Zone z : zones) {
            if (z.contains(waypoint.getPos())) {
                // no need for check as waypoint would be in Field#waypoints.
                z.addWaypoint(waypoint);
                waypoint.addZone(z);
            }
        }
        waypoints.add(waypoint);


    }

    // O(N) but who cares?
    public Waypoint getNearest(Point2D pos) {
        Waypoint best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (Waypoint w : waypoints) {
            double t = GeometryUtils.euclideanDistance(pos, w.getPos());
            if (t < bestDistance) {
                bestDistance = t;
                best = w;
            }
        }
        return best;
    }

    // Dijkstra's algorithm
    public List<Waypoint> findPath(Waypoint start, Waypoint end) {
        // tradeoff: decrease priority is not well implemented in the Java API, so we'll just do it in O(V) instead, manually.
        // This should only happen rarely.
        for (Waypoint w : waypoints) {
            w.tag = w.new Tag();
        }
        PriorityQueue<Waypoint> queue = new PriorityQueue<Waypoint>(10, new Comparator<Waypoint>() {
            @Override
            public int compare(Waypoint w1, Waypoint w2) {
                return Double.compare(w1.tag.dist, w2.tag.dist);
            }
        });
        // we'll do a backwards search so we can extract the path in forward order relative to parameters start and end.
        Waypoint src = end;
        src.tag.dist = 0;
        src.tag.inQueue = true;
        queue.add(src);

        while (!queue.isEmpty()) {
            Waypoint u = queue.poll();
            //System.out.println("u = " + u);
            if (u == start) break;
            for (Waypoint v : u.getNeighbors()) {
                double alt = u.tag.dist + u.distanceTo(v);
                if (alt < v.tag.dist) {
                    if (v.tag.inQueue) {
                        queue.remove(v);
                    }
                    v.tag.dist = alt;
                    v.tag.prev = u;
                    queue.add(v);
                    v.tag.inQueue = true;
                }

            }
        }

        ArrayList<Waypoint> path = new ArrayList<>();
        Waypoint t = start;
        //System.out.println("t = " + t);
        do {
            path.add(t);
            t = t.tag.prev;
        } while (t != end
                && t != null);
        return path;
    }


    public void addConnection(Waypoint w1, Waypoint w2) throws ObstacleException {
        if (w1.equals(w2)) throw new IllegalArgumentException("connection to self");
        Segment s = new Segment(w1.getPos(), w2.getPos());
        for (Zone z : zones) {
            if (z.mode == ZoneMode.ZONE_OBSTACLE || z.mode == ZoneMode.ZONE_ILLEGAL) {
                if (z.intersects(s)) throw new ObstacleException("Connection crosses an obstacle or illegal zone");
            }
        }
        w1.addNeighbor(w2);
        w2.addNeighbor(w1);
    }

    enum ZoneMode {
        ZONE_COMMON, ZONE_ALLIANCE, ZONE_PERSONAL, ZONE_ILLEGAL, ZONE_OBSTACLE
    }

    /**
     * Describes a polygonal area of the game field, along with metadata describing its role during gameplay.
     */
    public class Zone {


        private final ZoneMode mode;

        private final TreeSet<Point2D> waypoints = new TreeSet<>(new Comparator<Point2D>() {
            @Override
            public int compare(Point2D o1, Point2D o2) {
                int xCompRes = Long.compare(o1.getX(), o2.getX());
                return (xCompRes == 0 ? Long.compare(o1.getY(), o2.getY()) : xCompRes);
            }
        });

        /**
         * Constructs a new zone
         *
         * @param mode     The zone mode to apply
         * @param vertices A set of vertices, describing the polygon. Must be in consistent order (clockwise OR counterclockwise), with all elements unique.
         * @throws ftc.team6460.javadeck.api.planner.geom.DegeneratePolygonException If the polygon is degenerate (duplicate vertices, or zero area). This is not thrown for self-intersecting polygons with a
         *                                                                           well-defined interior (as per odd-even rule). However, undefined behavior may result from use of such polygons.
         */
        public Zone(ZoneMode mode, Point2D[] vertices) throws DegeneratePolygonException {
            this.mode = mode;
            this.vertices = vertices.clone();
            // check unique vertices:
            HashSet<Point2D> vertexSet = new HashSet<>();
            for (Point2D vtx : vertices) {
                if (!vertexSet.add(vtx))
                    throw new DegeneratePolygonException("Vertices are not unique: " + vtx.toString());
            }
        }


        public void addWaypoint(Waypoint waypoint) throws ObstacleException {
            if (this.mode == ZoneMode.ZONE_OBSTACLE) {
                throw new ObstacleException("This waypoint is contained within a zone tagged as an obstacle.");
            }
            waypoints.add(waypoint.getPos());
        }

        public boolean contains(Point2D point) {
            return GeometryUtils.checkWindingNumber(point, vertices) != 0;
        }

        public boolean intersects(Segment s) {
            for (int i = 0; i < vertices.length; i++) {
                if (GeometryUtils.intersect2D(s, new Segment(vertices[i], vertices[(i + 1) % vertices.length])) != 0)
                    return true;
            }
            return false;
        }

        private final Point2D[] vertices;
    }
}
