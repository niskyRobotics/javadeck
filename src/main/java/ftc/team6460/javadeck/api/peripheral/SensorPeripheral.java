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

public interface SensorPeripheral<T, P> extends RobotPeripheral {
    /**
     * Reads the value from the sensor.
     *
     * @param params Parameters, such as sensing type, to be passed to the sensor.
     * @throws InterruptedException             If interrupted waiting for the sensor to read its value.
     * @throws PeripheralCommunicationException If the sensor cannot be communicated with.
     * @throws PeripheralInoperableException    If the sensor is inoperable.
     */
    public T read(P params) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException;

    /**
     * Calibrates or resets the sensor.
     *
     * @param params Parameters, such as sensing type, to be passed to the sensor.
     * @param val    The value to calibrate to.
     * @throws UnsupportedOperationException    If the sensor cannot be calibrated or set to that value.
     * @throws InterruptedException             If interrupted waiting for the sensor to read its value.
     * @throws PeripheralCommunicationException If the sensor cannot be communicated with.
     * @throws PeripheralInoperableException    If the sensor is inoperable.
     */
    public void calibrate(T val, P params) throws InterruptedException, UnsupportedOperationException, PeripheralInoperableException, PeripheralCommunicationException;
}
