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

import ftc.team6460.javadeck.api.planner.ObstacleException;
import ftc.team6460.javadeck.api.planner.geom.DuplicateWaypointException;
import ftc.team6460.javadeck.api.planner.geom.Field;
import ftc.team6460.javadeck.api.planner.geom.Point2D;
import ftc.team6460.javadeck.api.planner.geom.Waypoint;
import org.junit.Test;

import java.util.List;

/**
 * Created by hexafraction on 4/11/15.
 */
public class DijkstraTest {
    @Test
    public void testDijkstras() throws DuplicateWaypointException, ObstacleException {
        Field f = new Field(10_000, 10_000);
        Waypoint[][] test = new Waypoint[20][20];
        String[][] disp = new String[20][20];

        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                test[x][y] = Waypoint.fromPos(new Point2D(x, y));
                if ((x > 3 && x < 18 && (y == 6 || y==13)) || (y < 18 && x == 8)) {
                    test[x][y] = null;
                }
                if (test[x][y] != null)
                    f.addWaypoint(test[x][y]);

                disp[x][y] = (test[x][y] == null) ? "XXX" : "[ ]";

            }
        }
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 19; y++) {
                if (test[x][y] != null && test[x][y + 1] != null)
                    f.addConnection(test[x][y], test[x][y + 1]);
            }
        }
        for (int x = 0; x < 19; x++) {
            for (int y = 0; y < 19; y++) {
                if (test[x][y] != null && test[x + 1][y + 1] != null)
                    f.addConnection(test[x][y], test[x + 1][y + 1]);
            }
        }
        for (int x = 0; x < 19; x++) {
            for (int y = 1; y < 20; y++) {
                if (test[x][y] != null && test[x + 1][y - 1] != null)
                    f.addConnection(test[x][y], test[x + 1][y - 1]);
            }
        }
        for (int x = 0; x < 19; x++) {
            for (int y = 0; y < 20; y++) {
                if (test[x][y] != null && test[x + 1][y] != null)
                    f.addConnection(test[x][y], test[x + 1][y]);
            }
        }

        List<Waypoint> rslt = f.findPath(test[1][3], test[16][11]);
       // System.out.println("rslt = " + rslt);
        for (int i = 0; i < rslt.size(); i++) {
            Waypoint w = rslt.get(i);
            disp[(int) w.getPos().getX()][(int) w.getPos().getY()] = String.format("%03d", i);
        }

        for (int y = 0; y < 20; y++) {
            for (int x = 0; x < 20; x++) {
                System.out.print(disp[x][y]);
                System.out.print(" ");
            }
            System.out.println();
        }
    }
}
