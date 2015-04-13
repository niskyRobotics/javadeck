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
import org.newdawn.slick.*;
import org.newdawn.slick.geom.Rectangle;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by hexafraction on 4/12/15.
 */
public class DijkstraTest extends BasicGame {

    private static Field f;
    private static Waypoint h1 = null, h2 = null;
    private static List<Waypoint> foundPath = null;

    @Override
    public void mouseClicked(int button, int x, int y, int clickCount) {
        System.out.println("button = [" + button + "], x = [" + x + "], y = [" + y + "], clickCount = [" + clickCount + "]");

        if (button == 0) try {
            f.addWaypoint(Waypoint.fromPos(new Point2D(x, y)));
        } catch (DuplicateWaypointException e) {
            e.printStackTrace();
        } catch (ObstacleException e) {
            e.printStackTrace();
        }
        if (button == 1) {
            h1 = f.getNearest(new Point2D(x, y));
        }
        if (button == 2) {
            h2 = f.getNearest(new Point2D(x, y));
        }
    }

    @Override
    public void keyPressed(int key, char c) {
        try {
            System.out.println("key = [" + key + "], c = [" + c + "]");
            if (c == 'c') {
                if (h1 != null && h2 != null) {
                    try {
                        f.addConnection(h1, h2);
                    } catch (ObstacleException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (c == 'f') {
                foundPath = f.findPath(h1, h2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DijkstraTest(String gamename) {
        super(gamename);
    }

    @Override
    public void init(GameContainer gc) throws SlickException {
    }

    @Override
    public void update(GameContainer gc, int i) throws SlickException {
    }

    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {

        for (Waypoint w : f.getWaypoints()) {
            g.setColor(Color.darkGray);
            for (Waypoint n : w.getNeighbors()) {
                g.drawLine(w.getPos().getX(), w.getPos().getY(), n.getPos().getX(), n.getPos().getY());
            }
            if (w == h1)
                g.setColor(Color.yellow);
            else if (w == h2)
                g.setColor(Color.red);
             else g.setColor(Color.lightGray);
            int x = (int) w.getPos().getX();
            int y = (int) w.getPos().getY();


            g.fill(new Rectangle(x - 4, y - 4, 8, 8));
            if (foundPath != null && foundPath.contains(w)) {
                g.setColor(Color.black);
                g.fill(new Rectangle(x - 2, y - 2, 4, 4));
            }
        }
        g.setColor(Color.blue);
        if (foundPath != null) {
            for (int i = 0; i < foundPath.size() - 1; i++) {
                g.drawLine(foundPath.get(i).getPos().getX(), foundPath.get(i).getPos().getY(), foundPath.get(i + 1).getPos().getX(), foundPath.get(i + 1).getPos().getY());
                g.drawLine(foundPath.get(i).getPos().getX()+1, foundPath.get(i).getPos().getY(), foundPath.get(i + 1).getPos().getX()+1, foundPath.get(i + 1).getPos().getY());
                g.drawLine(foundPath.get(i).getPos().getX()-1, foundPath.get(i).getPos().getY(), foundPath.get(i + 1).getPos().getX()-1, foundPath.get(i + 1).getPos().getY());
                g.drawLine(foundPath.get(i).getPos().getX(), foundPath.get(i).getPos().getY()+1, foundPath.get(i + 1).getPos().getX(), foundPath.get(i + 1).getPos().getY()+1);
                g.drawLine(foundPath.get(i).getPos().getX()+1, foundPath.get(i).getPos().getY()+1, foundPath.get(i + 1).getPos().getX()+1, foundPath.get(i + 1).getPos().getY()+1);
                g.drawLine(foundPath.get(i).getPos().getX()-1, foundPath.get(i).getPos().getY()+1, foundPath.get(i + 1).getPos().getX()-1, foundPath.get(i + 1).getPos().getY()+1);
                g.drawLine(foundPath.get(i).getPos().getX(), foundPath.get(i).getPos().getY()-1, foundPath.get(i + 1).getPos().getX(), foundPath.get(i + 1).getPos().getY()-1);
                g.drawLine(foundPath.get(i).getPos().getX()+1, foundPath.get(i).getPos().getY()-1, foundPath.get(i + 1).getPos().getX()+1, foundPath.get(i + 1).getPos().getY()-1);
                g.drawLine(foundPath.get(i).getPos().getX()-1, foundPath.get(i).getPos().getY()-1, foundPath.get(i + 1).getPos().getX()-1, foundPath.get(i + 1).getPos().getY()-1);
            }
        }
    }

    public static void main(String[] args) throws DuplicateWaypointException, ObstacleException {
        System.setProperty("java.library.path", "mapeditor/target;mapeditor/target/natives");
        System.setProperty("org.lwjgl.librarypath", new File("mapeditor/target/natives").getAbsolutePath());
        f = new Field(1000, 1000);
        Waypoint[][] test = new Waypoint[20][20];
        String[][] disp = new String[20][20];

        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 20; y++) {
                test[x][y] = Waypoint.fromPos(new Point2D(x*40 + 80, y*40+80));
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
        try {
            AppGameContainer appgc = new AppGameContainer(new DijkstraTest("DijkstraTest"));
            appgc.setDisplayMode(1000, 1000, false);
            appgc.setMouseGrabbed(false);
            appgc.setTargetFrameRate(120);
            appgc.start();
        } catch (SlickException ex) {
            Logger.getLogger(DijkstraTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
