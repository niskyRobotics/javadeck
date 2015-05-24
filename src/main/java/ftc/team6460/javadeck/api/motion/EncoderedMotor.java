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
import ftc.team6460.javadeck.api.peripheral.SensorPeripheral;
import ftc.team6460.javadeck.api.safety.SafetyGroup;
import ftc.team6460.javadeck.api.safety.SafetyPeripheral;

/**
 *
 */
public interface EncoderedMotor extends UnencoderedMotor, SensorPeripheral<Double, Void>, SafetyPeripheral {




    /**
     * Sets the motor speed to the new value. This method should be overridden if it is possible to determine when the motor has spun up to its target speed.
     * The input may specify the speed in an implementation-dependent unit, but a given robot platform should implement this so that all motors of the same type will
     * cause the same linear motion after their gearing is considered. This may require instances of this to have fields describing any gearing or other considerations.
     *
     * @param input The speed for the motor to reach.
     * @throws InterruptedException                                                  If interrupted waiting for the effector to reach its target value.
     * @throws PeripheralCommunicationException If the motor cannot be communicated with.
     * @throws PeripheralInoperableException    If the motor is inoperable.
     */
    @Override
    public void write(Double input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException;

    /**
     * Sets the speed of the motor to the new value.
     * The input may specify the speed in an implementation-dependent unit, but a given robot platform should implement this so that all motors of the same type will
     * cause the same linear motion after their gearing is considered. This may require instances of this to have fields describing any gearing or other considerations.
     * When overridden, a call to super() is required for proper safety control.
     *
     * @param input The value to write.
     * @throws InterruptedException             If interrupted waiting for the write to finish.
     * @throws PeripheralCommunicationException If the effector cannot be communicated with.
     * @throws PeripheralInoperableException    If the effector is inoperable.
     */
    @Override
    public void writeFast(Double input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException;


    /**
     * Actually write the value to the device.
     *
     * @param val The value to write
     */
    abstract void doWrite(double val)  throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException;







}
