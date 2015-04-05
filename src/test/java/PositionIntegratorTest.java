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

import ftc.team6460.javadeck.api.planner.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by hexafraction on 3/31/15.
 */
public class PositionIntegratorTest {

    public static void main(String[] args) throws Exception {
        new PositionIntegratorTest().testTry1();
    }

    @Test
    public void testTry1() throws Exception {
        List<Sensor> sens = new ArrayList<>();
        FakeXHill xH = new FakeXHill(Math.PI);
        FakeYHill yH = new FakeYHill(Math.E);
        sens.add(xH);
        sens.add(yH);
        PositionIntegrator integ = new PositionIntegrator(sens, 10, 10);
        List<LocationCandidate> cands = integ.getCandidates(0.95);
        Collections.sort(cands, new Comparator<LocationCandidate>() {
            @Override
            public int compare(LocationCandidate o1, LocationCandidate o2) {
                return -Double.compare(o1.getCorrelationStrength(), o2.getCorrelationStrength());
            }
        });
        System.out.println(cands.get(0));
        long t = System.currentTimeMillis();
        int iters = 0;
        Random r = new Random();
        for(int i = 0; i < 10000; i++){
            iters++;
            double x = r.nextDouble()*8 + 1;
            double y = r.nextDouble()*8 +1;
            xH.setPos(x);
            yH.setPos(y);
            Assert.assertEquals(1, xH.getLikelihood(x, 5), 0.1);
            List<LocationCandidate> res = integ.getCandidates(0.99);
            Collections.sort(res, new Comparator<LocationCandidate>() {
                @Override
                public int compare(LocationCandidate o1, LocationCandidate o2) {
                    return -Double.compare(o1.getCorrelationStrength(), o2.getCorrelationStrength());
                }
            });
            try {
                ImmutableRobotPosition p = res.get(0).getPosition();
                System.out.println(res.get(0));
                System.out.println("ERR: " + Math.hypot(p.getX() - x, p.getY() - y) + "; MILLIS/ITER = " + (System.currentTimeMillis() - t) / iters);
            } catch (Exception e){
                e.printStackTrace();
                System.out.println("x = " + x);
                System.out.println("y = " + y);
                System.out.println("i = " + i);
            }
        }
    }


    private class FakeXHill implements Sensor {
        private double pos;

        public FakeXHill(double pos) {
            this.pos = pos;
        }

        @Override
        public double getLikelihood(double x, double y) {
            return Math.exp(-Math.abs(Math.log(x / pos)));
        }

        @Override
        public double getOrientationLikelihood(double x, double y, double theta) {
            return Math.exp(-Math.abs(Math.log(x / pos)));
        }

        public void setPos(double pos) {
            this.pos = pos;
        }

        @Override
        public double getWeight(double x, double y) {
            return 1;

        }

        @Override
        public Iterable<RobotPosition> getPossibleHotspots() {
            return Collections.emptyList();
            /*

            ArrayList<RobotPosition> l = new ArrayList<>();
            for(int i = 0; i <= 5; i++){
                l.add(new ImmutableRobotPosition(pos,i*2, 0));
            }
            return l;*/
        }

        @Override
        public void notifyError(double x, double y, double theta, double agreedWeight) {

        }
    }

    private class FakeYHill implements Sensor {
        public void setPos(double pos) {
            this.pos = pos;
        }

        private double pos;

        public FakeYHill(double pos) {
            this.pos = pos;
        }

        @Override
        public double getLikelihood(double x, double y) {
            return Math.exp(-Math.abs(Math.log(y / pos)));
        }

        @Override
        public double getOrientationLikelihood(double x, double y, double theta) {
            return Math.exp(-Math.abs(Math.log(y / pos)));
        }

        @Override
        public double getWeight(double x, double y) {
            return 1;
        }

        @Override
        public Iterable<RobotPosition> getPossibleHotspots() {
            //return Collections.emptyList();

            ArrayList<RobotPosition> l = new ArrayList<>();
            for (int i = 0; i <= 5; i++) {
                l.add(new ImmutableRobotPosition(i*2, pos, 0));
            }
            return l;
        }

        @Override
        public void notifyError(double x, double y, double theta, double agreedWeight) {

        }
    }
}
