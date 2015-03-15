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

package ftc.team6460.javadeck.api.safety;

import ftc.team6460.javadeck.api.peripheral.EffectorPeripheral;
import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Represents a group of devices that should shut down if one of them fails, or if an external trigger is triggered.
 */
public class SafetyGroup implements EffectorPeripheral<Void> {
    private final Callable<Void> onFaultCallable;
    private final SafetyShutdownFailureAction failureAction;

    @Override
    public void write(Void input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        throw new UnsupportedOperationException("Cannot write to a safety group");
    }

    @Override
    public void writeFast(Void input) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        throw new UnsupportedOperationException("Cannot write to a safety group");
    }

    @Override
    public void safetyShutdown() throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        safetyShutdown0();
    }

    @Override
    public void addSafetyGroup(SafetyGroup grp) {
        throw new UnsupportedOperationException("Nested safety groups are not permitted.");
    }

    @Override
    public void loop() {
        for (SafetyPeripheral sens : sensors) {
            if (!sens.checkSafety()) {
                safetyShutdown0();
                return;
            }
        }
    }

    @Override
    public void setup() {
        // noop
    }

    public static interface SafetyShutdownFailureAction {
        public void onFail(EffectorPeripheral<?> effector, Throwable error);
    }

    public SafetyGroup(Callable<Void> onFaultCallable, SafetyShutdownFailureAction failureAction) {
        this.onFaultCallable = onFaultCallable;
        this.failureAction = failureAction;
    }

    public SafetyGroup(Callable<Void> onFaultCallable) {
        this.onFaultCallable = onFaultCallable;
        this.failureAction = null;

    }

    private final Set<EffectorPeripheral<?>> members = new ConcurrentSkipListSet<>();

    private final Set<SafetyPeripheral> sensors = new ConcurrentSkipListSet<>();

    public void addSafetySensor(SafetyPeripheral sens) {
        sensors.add(sens);
    }

    /**
     * Add an effector to this safety group.
     *
     * @param eff The effector to add.
     */
    public void registerEffector(EffectorPeripheral<?> eff) {
        this.members.add(eff);
        if (eff instanceof SafetyPeripheral)
            this.addSafetySensor((SafetyPeripheral) eff);
    }

    /**
     * Call to signal a safety fault. All effectors in this group are immediately put to a safe state.
     */
    protected void safetyShutdown0() {

        for (EffectorPeripheral<?> eff : members) {
            try {
                eff.safetyShutdown();
            } catch (Throwable t) {
                if (this.failureAction != null) {
                    failureAction.onFail(eff, t);
                }
            }
        }


    }
}
