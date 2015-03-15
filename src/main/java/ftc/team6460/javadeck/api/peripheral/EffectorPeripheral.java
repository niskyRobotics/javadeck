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

package ftc.team6460.javadeck.api.peripheral;

import ftc.team6460.javadeck.api.safety.SafetyGroup;

/**
 * Describes a peripheral that can output an effect.
 *
 * @param <T> The type of output that this peripheral can effect. For example, a peripheral such as a simple transistor, that can only switch on and off,
 *            would be considered a boolean output, while a motor can be considered a real number.
 */
public interface EffectorPeripheral<T> extends RobotPeripheral {
    /**
     * Writes the value to the effector, and waits for the effector to reach its target value.
     *
     * @param input The value to write.
     * @throws InterruptedException             If interrupted waiting for the effector to reach its target value.
     * @throws PeripheralCommunicationException If the effector cannot be communicated with.
     * @throws PeripheralInoperableException    If the effector is inoperable.
     */
    public void write(T input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException;

    /**
     * Writes the value to the effector, and returns immediately after the write is finished.
     *
     * @param input The value to write.
     * @throws InterruptedException             If interrupted waiting for the write to finish.
     * @throws PeripheralCommunicationException If the effector cannot be communicated with.
     * @throws PeripheralInoperableException    If the effector is inoperable.
     */
    public void writeFast(T input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException;

    /**
     * Shut down to a known safe state, immediately.
     *
     * @param nanos The number of nanoseconds for the shutdown to stay in effect.
     *
     * If any exception is thrown, the state of the effector is unknown. Depending on its role, the program may take other actions.
     */
    public void safetyShutdown(long nanos) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException;

    /**
     * Adds this effector to the given safety group. This method may be called multiple times. This should establish a reference to that group, so that if this effector
     * is found to be in an unsafe state, the groups it belongs to can be notified.
     *
     * @param grp The safety group that this effector is being added to.
     */
    public void addSafetyGroup(SafetyGroup grp);
}
