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


/**
 * Describes a goal for a motion planner.
 * @param <T> A class describing the robot's state
 *
 */
public abstract class Goal<T> implements Comparable<Goal<T>>{
    private final ImmutableRobotPosition location;

    protected Goal(ImmutableRobotPosition location) {
        this.location = location;
    }

    public ImmutableRobotPosition getLocation() {
        return location;
    }

    /**
     * Computes the benefit from successfully achieving this goal.
     *
     * @param state The robot's current state.
     * @return The expected benefit.
     * @see ftc.team6460.javadeck.api.planner Information on cost and benefit units
     */
    public abstract double computeBenefit(T state, GoalPlanner g);

    /**
     * Acts when the robot is in position at this goal. This should take any actions to be done when at the goal, such
     * as sensing the environment, operating effectors, adding or removing other goals from the planner, etc.
     *
     * @param pos   The current computed robot position.
     * @param state The robot's current state.
     */
    public abstract void act(RobotPosition pos, T state, GoalPlanner g);

}
