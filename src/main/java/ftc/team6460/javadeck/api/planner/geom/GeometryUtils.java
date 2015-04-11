// Copyright 2001 softSurfer, 2012 Dan Sunday
// This code may be freely used and modified for any purpose
// providing that this copyright notice is included with it.
// SoftSurfer makes no warranty for this code, and cannot be held
// liable for any real or imagined damage resulting from its use.
// Users of this code must verify correctness for their application.

// Java adaptation of these algorithms provided by hexafraction, 2015. Distribution is permitted providing that
// both copyright notices are included with it. No guarantee or liability is assumed.

package ftc.team6460.javadeck.api.planner.geom;

/**
 * Created by hexafraction on 4/11/15.
 */
public class GeometryUtils {

    private static final double SMALL_NUM = 0.00000001;

    /**
     * @param p0 Point defining line
     * @param p1 Point defining line
     * @param p2 Point to test
     * @return >0 for p2 left of the line through p0 and p1, <0 for p2 right of line, 0 for p2 on line
     */
    public static long isLeft(Point2D p0, Point2D p1, Point2D p2) {
        return ((p1.x - p0.x) * (p2.y - p0.y)
                - (p2.x - p0.x) * (p1.y - p0.y));

    }

    private static long perp(Point2D u, Point2D v) {
        return ((u).x * (v).y - (u).y * (v).x);

    }

    private static long dot(Point2D u, Point2D v) {
        return ((u).x * (v).x + (u).y * (v).y);
    }


    public static double euclideanLength(Segment s){
        return euclideanDistance(s.p0, s.p1);
    }

    public static double euclideanDistance(Point2D p0, Point2D p1) {
        return Math.sqrt((p1.x-p0.x)*(p1.x-p0.x) + (p1.y-p0.y)*(p1.y-p0.y));
    }

    /**
     * Checks if 2 segments intersect
     *
     * @param s1 One segment
     * @param s2 Another segment
     * @return 0 if disjoint, 1 if intersect, 2 if overlap.
     */

    public static int
    intersect2D(Segment s1, Segment s2) {
        Point2D u = new Point2D(s1.p1.x - s1.p0.x, s1.p1.y - s1.p0.y);
        Point2D v = new Point2D(s2.p1.x - s2.p0.x, s2.p1.y - s2.p0.y);
        Point2D w = new Point2D(s1.p0.x - s2.p0.x, s1.p0.y - s2.p0.y);
        float D = perp(u, v);

        // test if  they are parallel (includes either being a point)
        if (Math.abs(D) < SMALL_NUM) {           // s1 and s2 are parallel
            if (perp(u, w) != 0 || perp(v, w) != 0) {
                return 0;                    // they are NOT collinear
            }
            // they are collinear or degenerate
            // check if they are degenerate  points
            float du = dot(u, u);
            float dv = dot(v, v);
            if (du == 0 && dv == 0) {            // both segments are points
                if (s1.p0 != s2.p0)         // they are distinct  points
                    return 0;
                //*I0 = s1.p0;                 // they are the same point
                return 1;
            }
            if (du == 0) {                     // s1 is a single point
                if (!inSegment(s1.p0, s2))  // but is not in s2
                    return 0;
                //*I0 = s1.p0;
                return 1;
            }
            if (dv == 0) {                     // s2 a single point
                if (!inSegment(s2.p0, s1))  // but is not in s1
                    return 0;
                //*I0 = s2.p0;
                return 1;
            }
            // they are collinear segments - get  overlap (or not)
            float t0, t1;                    // endpoints of s1 in eqn for s2
            Point2D w2 = new Point2D(s1.p1.x - s2.p0.x, s1.p1.y - s2.p0.y);
            if (v.x != 0) {
                t0 = w.x / v.x;
                t1 = w2.x / v.x;
            } else {
                t0 = w.y / v.y;
                t1 = w2.y / v.y;
            }
            if (t0 > t1) {                   // must have t0 smaller than t1
                float t = t0;
                t0 = t1;
                t1 = t;    // swap if not
            }
            if (t0 > 1 || t1 < 0) {
                return 0;      // NO overlap
            }
            t0 = t0 < 0 ? 0 : t0;               // clip to min 0
            t1 = t1 > 1 ? 1 : t1;               // clip to max 1
            if (t0 == t1) {                  // intersect is a point
                //*I0 = s2.p0 +  t0 * v;
                return 1;
            }

            // they overlap in a valid subsegment
            // *I0 = s2.p0 + t0 * v;
            //*I1 = s2.p0 + t1 * v;
            return 2;
        }

        // the segments are skew and may intersect in a point
        // get the intersect parameter for s1
        float sI = perp(v, w) / D;
        if (sI < 0 || sI > 1)                // no intersect with s1
            return 0;

        // get the intersect parameter for s2
        float tI = perp(u, w) / D;
        if (tI < 0 || tI > 1)                // no intersect with s2
            return 0;

        //*I0 = s1.p0 + sI * u;                // compute s1 intersect point
        return 1;
    }


    /**
     * Check if point on segment
     *
     * @param p Point
     * @param s <i>Collinear</i> segment
     * @return True if point is on segment
     */
    public static boolean inSegment(Point2D p, Segment s) {
        if (s.p0.x != s.p1.x) {    // s is not  vertical
            if (s.p0.x <= p.x && p.x <= s.p1.x)
                return true;
            if (s.p0.x >= p.x && p.x >= s.p1.x)
                return true;
        } else {    // s is vertical, so test y  coordinate
            if (s.p0.y <= p.y && p.y <= s.p1.y)
                return true;
            if (s.p0.y >= p.y && p.y >= s.p1.y)
                return true;
        }
        return false;
    }

    /**
     * Winding number test for a point and a polygon
     *
     * @param p The point
     * @param v The vertices of the poly
     * @return Winding number (0 for outside)
     */

    public static int checkWindingNumber(Point2D p, Point2D[] v) {

        int wn = 0;    // the  winding number counter

        // loop through all edges of the polygon
        for (int i = 0; i < v.length; i++) {   // edge from v[i] to  v[i+1]
            if (v[i].y <= p.y) {          // start y <= p.y
                if (v[i + 1].y > p.y)      // an upward crossing
                    if (isLeft(v[i], v[(i + 1) % v.length], p) > 0)  // p left of  edge
                        ++wn;            // have  a valid up intersect
            } else {                        // start y > p.y (no test needed)
                if (v[i + 1].y <= p.y)     // a downward crossing
                    if (isLeft(v[i], v[(i + 1) % v.length], p) < 0)  // p right of  edge
                        --wn;            // have  a valid down intersect
            }
        }
        return wn;
    }
}
