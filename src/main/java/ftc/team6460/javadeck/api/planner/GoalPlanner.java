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
import java8.util.stream.StreamSupport;

import java.util.*;
import java.util.concurrent.SynchronousQueue;

/**
 * Contains a planner for goals.
 */
public class GoalPlanner<T> {
    private final List<Goal<T>> goals = new ArrayList<>();
    private final T currentState;
    private final RobotDrive drive;
    private final PositionIntegrator integrator;
    private final Field field;
    private final double minCorr;

    /**
     * Creates a new goal planner
     *
     * @param initialState The robot's initial state
     * @param drive        The drivetrain to use in driving the robot to goals
     * @param integrator   The position integrator to use.
     * @param f            A description of the field
     * @param minCorr      The minimum sensor integrator correlation allowed.
     */
    public GoalPlanner(T initialState, RobotDrive drive, PositionIntegrator integrator, Field f, double minCorr) {
        this.currentState = initialState;
        this.drive = drive;
        this.integrator = integrator;
        this.field = f;
        this.minCorr = minCorr;
    }

    /**
     * Adds a goal to the planner, if it does not already exist.
     */
    public synchronized void addGoal(Goal<T> goal) {
        goals.add(goal);
        newGoalNotifier.offer(new Object());
    }

    /**
     * Removes a goal from the planner if not already present.
     */
    public synchronized void removeGoal(Goal<T> goal) {
        goals.remove(goal);
    }

    protected synchronized Goal<T> getBestGoal() throws NoSuchElementException {
        return StreamSupport.stream(goals).reduce(
                (goal1, goal2) ->
                        goal1.computeBenefit(currentState, this) >= goal2.computeBenefit(currentState, this)
                                ? goal1 : goal2)
                .get();

    }
    private volatile boolean run = true;
    public synchronized void start() {
        if (run) {
            return;
        }
        run = true;
        new Thread(this::runLoop, "goalplanner-" + this.hashCode()).start();
    }

    public void runLoop() {
        while (run) {
            try {
                Goal<T> goal = GoalPlanner.this.getBestGoal();
                LocationCandidate ourPosition = StreamSupport.stream(GoalPlanner.this.integrator.getCandidates(minCorr))
                        .reduce(null, (c1, c2) -> c1.getCorrelationStrength() >= c2.getCorrelationStrength() ? c1 : c2);
                if (ourPosition == null) {
                    // sleep a second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                }

                try {
                    List<RelativePosition> steps = GoalPlanner.this.field.findPath(ourPosition.getPosition(),
                            goal.getLocation());
                    for (RelativePosition rp : steps) {
                        drive.move(rp, false);
                    }
                    LocationCandidate ourPositionAfter = StreamSupport.stream(GoalPlanner.this.integrator.getCandidates(minCorr))
                            .reduce(null, (c1, c2) -> c1.getCorrelationStrength() >= c2.getCorrelationStrength() ? c1 : c2);
                    if (ourPosition == null) {
                        goal.act(drive.getCurrentPosition(), GoalPlanner.this.currentState, GoalPlanner.this);
                    } else {
                        goal.act(ourPositionAfter.getPosition(), GoalPlanner.this.currentState, GoalPlanner.this);
                    }


                } catch (ObstacleException e) {
                    GoalPlanner.this.removeGoal(goal);
                } catch (RobotHardwareException e) {
                    //e.printStackTrace();
                }
            } catch (NoSuchElementException e) {
                try {
                    newGoalNotifier.take();
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }
        }
    }



    private SynchronousQueue<Object> newGoalNotifier = new SynchronousQueue<>();

    public synchronized void stop() {
        this.run = false;
    }






}
