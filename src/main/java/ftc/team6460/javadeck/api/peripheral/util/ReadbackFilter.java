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

package ftc.team6460.javadeck.api.peripheral.util;

import ftc.team6460.javadeck.api.peripheral.EffectorPeripheral;
import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;
import ftc.team6460.javadeck.api.peripheral.SensorPeripheral;
import ftc.team6460.javadeck.api.safety.SafetyGroup;

/**
 * Converts the write-only EffectorPeripheral into a read-write peripheral that returns the last written value.
 */
public class ReadbackFilter<O, I extends O> implements EffectorPeripheral<I>, SensorPeripheral<O, Void> {
    private final EffectorPeripheral<I> delegate;
    private O val;

    public ReadbackFilter(EffectorPeripheral<I> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void write(I input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        val = input;
        delegate.write(input);
    }

    @Override
    public void writeFast(I input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        delegate.writeFast(input);
    }

    @Override
    public void safetyShutdown(long nanos) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        val = null;
        delegate.safetyShutdown(nanos);
    }

    @Override
    public void addSafetyGroup(SafetyGroup grp) {
        delegate.addSafetyGroup(grp);
    }

    /**
     * Returns the last written value, or <code>null</code> if the value cannot be determined or has been invalidated.
     *
     * @param params Ignored.
     */
    @Override
    public O read(Void params) {
        return val;
    }

    @Override
    public void calibrate(O val, Void params) throws InterruptedException, UnsupportedOperationException, PeripheralInoperableException, PeripheralCommunicationException {
        throw new UnsupportedOperationException("Cannot calibrate a readback filter");
    }

    @Override
    public void loop() {
        // do not delegate
    }

    @Override
    public void setup() {
// do not delegate
    }
}
