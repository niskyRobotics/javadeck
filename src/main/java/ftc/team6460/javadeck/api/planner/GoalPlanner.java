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

import ftc.team6460.javadeck.api.planner.geom.Field;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Contains a planner for goals.
 */
public class GoalPlanner<T> {
    private final List<Goal<T>> goals = new ArrayList<>();
    private final T currentState;
    private final RobotDrive drive;
    private final PositionIntegrator integrator;
    private final Field field;

    /**
     * Creates a new goal planner
     *
     * @param initialState The robot's initial state
     * @param drive        The drivetrain to use in driving the robot to goals
     * @param integrator    The position integrator to use.
     * @param f            A description of the field
     */
    public GoalPlanner(T initialState, RobotDrive drive, PositionIntegrator integrator, Field f) {
        this.currentState = initialState;
        this.drive = drive;
        this.integrator = integrator;
        this.field = f;
    }

    /**
     * Adds a goal to the planner, if it does not already exist.
     */
    public synchronized void addGoal(Goal<T> goal) {
        goals.add(goal);
    }

    /**
     * Removes a goal from the planner if not already present.
     */
    public synchronized void removeGoal(Goal<T> goal) {
        goals.remove(goal);
    }

    protected synchronized Goal<T> getBestGoal() {
        Collections.sort(goals, new Comparator<Goal<T>>() {
            @Override
            public int compare(Goal<T> o1, Goal<T> o2) {
                return Double.compare(o1.computeBenefit(GoalPlanner.this.currentState, GoalPlanner.this),
                        o2.computeBenefit(GoalPlanner.this.currentState, GoalPlanner.this));

            }
        });
        return goals.get(goals.size() - 1);

    }

    public synchronized void start() {
        if (run) return;
        run = true;
        new Thread(new PlannerRunnable(), "goalplanner-" + this.hashCode()).start();
    }

    public synchronized void stop() {
        this.run = false;
    }

    private class PlannerRunnable implements Runnable {

        @Override
        public void run() {
            while (run) {
                Goal<T> goal = GoalPlanner.this.getBestGoal();
                List<LocationCandidate> bestMatches = GoalPlanner.this.integrator.getCandidates(0.9);
                Collections.sort(bestMatches, new Comparator<LocationCandidate>() {
                    @Override
                    public int compare(LocationCandidate o1, LocationCandidate o2) {
                        return Double.compare(o1.getCorrelationStrength(), o2.getCorrelationStrength());
                    }
                });
                try {
                    List<RelativePosition> steps = GoalPlanner.this.field.findPath(bestMatches.get(bestMatches.size() - 1).getPosition(),
                            goal.getLocation());
                    for (RelativePosition rp : steps) {
                        drive.move(rp, false);
                    }
                    List<LocationCandidate> bestMatchesAfter = GoalPlanner.this.integrator.getCandidates(0.9);
                    Collections.sort(bestMatchesAfter, new Comparator<LocationCandidate>() {
                        @Override
                        public int compare(LocationCandidate o1, LocationCandidate o2) {
                            return Double.compare(o1.getCorrelationStrength(), o2.getCorrelationStrength());
                        }
                    });
                    goal.act(bestMatchesAfter.get(bestMatchesAfter.size() - 1).getPosition(), GoalPlanner.this.currentState, GoalPlanner.this);
                } catch (ObstacleException e) {
                    GoalPlanner.this.removeGoal(goal);
                } catch (RobotHardwareException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private volatile boolean run = true;


}
