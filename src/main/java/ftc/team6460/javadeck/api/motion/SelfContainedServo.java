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

import ftc.team6460.javadeck.api.SlewedDouble;
import ftc.team6460.javadeck.api.peripheral.EffectorPeripheral;
import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;

/**
 * Represents a servo motor, generally attached to a servo controller.
 */
public abstract class SelfContainedServo implements EffectorPeripheral<SlewedDouble> {
    /**
     * Sets the servo to the new value. This method should be overridden if it is possible to determine the servo's physical location in order to wait for it to reach the goal.
     * The input may specify the position in an implementation-dependent unit, but a given robot platform should implement this so that all servos of the same type will
     * cause the same angular motion after their gearing is considered. This may require subclasses to have fields describing any gearing or other considerations.
     *
     * @param input The position for the servo to reach.
     * @throws InterruptedException                                                  If interrupted waiting for the effector to reach its target value.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException If the motor cannot be communicated with.
     * @throws ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException    If the motor is inoperable.
     */
    @Override
    public void write(SlewedDouble input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        this.writeFast(input);
    }

    /**
     * Sets the servo to the new value. This method should be overridden if it is possible to determine the servo's physical location.
     * The input may specify the position in an implementation-dependent unit, but a given robot platform should implement this so that all servos of the same type will
     * cause the same angular motion after their gearing is considered. This may require subclasses to have fields describing any gearing or other considerations.     *
     *
     * @param input The value to write.
     * @throws InterruptedException             If interrupted waiting for the write to finish.
     * @throws PeripheralCommunicationException If the effector cannot be communicated with.
     * @throws PeripheralInoperableException    If the effector is inoperable.
     */
    @Override
    public abstract void writeFast(SlewedDouble input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException;


}