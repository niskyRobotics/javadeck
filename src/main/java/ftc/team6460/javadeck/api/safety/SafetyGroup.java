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

import ftc.team6460.javadeck.api.Maintainable;
import ftc.team6460.javadeck.api.peripheral.EffectorPeripheral;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Represents a group of devices that should shut down if one of them fails, or if an external trigger is triggered.
 *
 * Safety shutdowns remain in place for as long as the timeout specified.
 */
public class SafetyGroup implements Maintainable {
    private final Callable<Void> onFaultCallable;
    private final SafetyShutdownFailureAction failureAction;
    private final long nanosForShutdown;


    @Override
    public void loop() {
        for (SafetyPeripheral sens : sensors) {
            if (!sens.checkSafety()) {
                safetyShutdown(this.nanosForShutdown);
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

    public SafetyGroup(Callable<Void> onFaultCallable, SafetyShutdownFailureAction failureAction, long timeout) {
        this.onFaultCallable = onFaultCallable;
        this.failureAction = failureAction;
        nanosForShutdown = timeout;
    }

    public SafetyGroup(Callable<Void> onFaultCallable, long timeout) {
        this.onFaultCallable = onFaultCallable;
        this.failureAction = null;

        nanosForShutdown = timeout;
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
        if (eff instanceof SafetyPeripheral) {
            this.addSafetySensor((SafetyPeripheral) eff);
        }
    }

    /**
     * Call to signal a safety fault. All effectors in this group are immediately put to a safe state.
     */
    public void safetyShutdown(long nanos) {

        for (EffectorPeripheral<?> eff : members) {
            try {
                eff.safetyShutdown(nanos);
            } catch (Throwable t) {
                if (this.failureAction != null) {
                    failureAction.onFail(eff, t);
                }
            }
        }
        try {
            onFaultCallable.call();
        } catch (Exception e) {
            // noop for now
        }


    }
}
