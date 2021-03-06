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
import ftc.team6460.javadeck.api.planner.ObstacleException;
import ftc.team6460.javadeck.api.planner.RelativePosition;

import java.io.Serializable;
import java.util.*;

/**
 * Describes a field of waypoints and zones that a robot may navigate.
 * <p>
 * Dimensions are fixed-point decimal, defined
 * such that a value of 1 describes an offset of 1mm.
 */
public class Field {
    private final Set<Zone> zones = new HashSet<>();
    private final Set<Waypoint> waypoints = new HashSet<>();

    public boolean addZone(Zone zone) {
        return zones.add(zone);
    }

    /**
     * Constructs a new field.
     *
     * @param zones The zones to add to this field.
     */

    public Field(Zone... zones) {
        Collections.addAll(this.zones, zones);
    }

    /**
     * Adds a new waypoint to this field.
     *
     * @param waypoint The waypoint to add
     * @throws DuplicateWaypointException If a waypoint at exactly this location already exists.
     * @throws ObstacleException          If the waypoint being added lies within a forbidden zone.
     */
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

    /**
     * Returns the nearest waypoint to the given point, using a euclidean metric.
     *
     * @param pos The point for which to find the closest waypoint.
     * @return The closest waypoint, or <code>null</code> if there are no waypoints on this field.
     */
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

    /**
     * Finds the shortest path (by Euclidean metric) between the two endpoints, using Dijkstra's Algorithm.
     *
     * @param start The waypoint at which to start.
     * @param end   The waypoint at which to end.
     * @return A list containing waypoints in the appropriate order.
     * @throws ObstacleException If no path is found.
     */
    public List<Waypoint> findPath(Waypoint start, Waypoint end) throws ObstacleException {
        // tradeoff: decrease priority is not well implemented in the Java API, so we'll just do it in O(V) instead, manually.
        // This should only happen rarely.
        for (Waypoint w : waypoints) {
            w.tag = new Waypoint.Tag();
        }
        PriorityQueue<Waypoint> queue = new PriorityQueue<Waypoint>(10, new WaypointComparator());
        // we'll do a backwards search so we can extract the path in forward order relative to parameters start and end.
        end.tag.dist = 0;
        end.tag.inQueue = true;
        queue.add(end);

        while (!queue.isEmpty()) {
            Waypoint u = queue.poll();
            //System.out.println("u = " + u);

            for (Waypoint v : u.getNeighbors()) {
                double alt = u.tag.dist + u.distanceTo(v);
                if (alt < v.tag.dist) {
                    if (v.tag.inQueue) {
                        queue.remove(v);
                        //System.out.println("boo");
                    }
                    v.tag.dist = alt;
                    v.tag.prev = u;
                    queue.add(v);
                    v.tag.inQueue = true;
                }

            }
            if (u == start) {
                break;
            }
        }

        AbstractList<Waypoint> path = new ArrayList<>();
        Waypoint t = start;
        //System.out.println("t = " + t);
        do {
            path.add(t);
            t = t.tag.prev;
            if (t == null) {
                throw new ObstacleException("No path found.");
            }
        } while (t != end);
        path.add(end);
        return path;
    }

    /**
     * Removes a waypoint and disconnects it from its neighbors.
     *
     * @param w The waypoint to remove.
     */
    public void removeWaypoint(Waypoint w) {
        waypoints.remove(w);
        Set<Waypoint> neighbors = new HashSet<>();
        neighbors.addAll(w.getNeighbors());
        for (Waypoint n : neighbors) {
            n.disconnect(w);
            w.disconnect(n);
        }
    }

    /**
     * Adds a graph adjacency between two waypoints, so that {@link Field#findPath(Waypoint, Waypoint)} can take a path between these two waypoints.
     *
     * @throws ObstacleException If the connection crosses an obstacle or illegal zone.
     */
    public void addConnection(Waypoint w1, Waypoint w2) throws ObstacleException {
        if (w1.equals(w2)) {
            throw new IllegalArgumentException("connection to self");
        }
        Segment s = new Segment(w1.getPos(), w2.getPos());
        for (Zone z : zones) {
            if (z.mode == ZoneMode.ZONE_OBSTACLE || z.mode == ZoneMode.ZONE_ILLEGAL) {
                if (z.intersects(s)) {
                    throw new ObstacleException("Connection crosses an obstacle or illegal zone");
                }
            }
        }
        w1.addNeighbor(w2);
        w2.addNeighbor(w1);
    }

    /**
     * Gets an iterable set of the waypoints, in no particular order.
     */
    public Iterable<Waypoint> getWaypoints() {
        return Collections.unmodifiableSet(waypoints);
    }

    /**
     * Finds the shortest path (by Euclidean metric) between the two endpoints, using Dijkstra's Algorithm.
     *
     * @param start The location at which to start.
     * @param end   The location at which to end.
     * @return A list containing travel moves in the appropriate order.
     * @throws ObstacleException If no path is found.
     */
    public List<RelativePosition> findPath(ImmutableRobotPosition start, ImmutableRobotPosition end) throws ObstacleException {
        Waypoint st = getNearest(Point2D.fromRobotPosition(start));
        Waypoint fin = getNearest(Point2D.fromRobotPosition(end));
        List<RelativePosition> rV = new ArrayList<>();
        List<Waypoint> waypoints = findPath(st, fin);
        ImmutableRobotPosition current = start;
        for (Waypoint w : waypoints) {
            ImmutableRobotPosition n = w.getPos().getAsRobotPos();
            RelativePosition rp = RelativePosition.between(current, n);
            rV.add(rp);
            current = rp.apply(current).materialize();

        }
        rV.add(RelativePosition.between(current, end));
        // finding will return both endpoints in correct order
        //List<Waypoint>
        return rV;
    }

    enum ZoneMode {
        ZONE_COMMON, ZONE_ALLIANCE, ZONE_PERSONAL, ZONE_ILLEGAL, ZONE_OBSTACLE
    }

    private static class WaypointComparator implements Comparator<Waypoint>, Serializable {
        @Override
        public int compare(Waypoint w1, Waypoint w2) {
            return Double.compare(w1.tag.dist, w2.tag.dist);
        }
    }

    /**
     * Describes a polygonal area of the game field, along with metadata describing its role during gameplay.
     */
    public static class Zone {


        private final ZoneMode mode;

        private final Set<Point2D> waypoints = new TreeSet<>((o1, o2) -> {
            int xCompRes = Long.compare(o1.getX(), o2.getX());
            return (xCompRes == 0 ? Long.compare(o1.getY(), o2.getY()) : xCompRes);
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
            Set<Point2D> vertexSet = new HashSet<>();
            for (Point2D vtx : vertices) {
                if (!vertexSet.add(vtx)) {
                    throw new DegeneratePolygonException("Vertices are not unique: " + vtx.toString());
                }
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
                if (GeometryUtils.intersect2D(s, new Segment(vertices[i], vertices[(i + 1) % vertices.length])) != 0) {
                    return true;
                }
            }
            return false;
        }

        private final Point2D[] vertices;
    }
}
