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

package ftc.team6460.javadeck.api.motion;

import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;

/**
 * Represents a drivetrain controlled by position.
 */
public interface VelocityDrivetrain {
    /**
     * Sets the movement on this drivetrain to be forward or backward. Must be thread-safe.
     *
     * @param forwardVelocity The velocity with which to move the robot forward, in meters per second.
     * @throws java.lang.IllegalArgumentException                                    If the velocity is beyond the limits or is invalid.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException If a motor or other drivetrain part cannot be communicated with.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException    If a motor or other drivetrain part is inoperable.
     */
    public void setVelocity(double forwardVelocity) throws PeripheralInoperableException, PeripheralCommunicationException, InterruptedException;

    /**
     * Sets the movement on this drivetrain to spin in place. Must be thread-safe.
     *
     * @param tangentialVelocity The velocity with which to spin the robot. Right turns are positive, left turns are negative. Units are meters per second.
     * @throws java.lang.IllegalArgumentException                                    If the velocity is beyond the limits or is invalid.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException If a motor or other drivetrain part cannot be communicated with.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException    If a motor or other drivetrain part is inoperable.
     */
    public void spinInPlace(double tangentialVelocity) throws PeripheralInoperableException, PeripheralCommunicationException, InterruptedException;



    /**
     * Stops all moves.
     *
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException If a motor or other drivetrain part cannot be communicated with.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException    If a motor or other drivetrain part is inoperable.
     */
    public void stopAll() throws PeripheralInoperableException, PeripheralCommunicationException, InterruptedException;
}
