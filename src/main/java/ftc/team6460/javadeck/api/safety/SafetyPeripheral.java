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

import ftc.team6460.javadeck.api.peripheral.RobotPeripheral;
import ftc.team6460.javadeck.api.peripheral.SensorPeripheral;

/**
 * Created by hexafraction on 3/14/15.
 */
public interface SafetyPeripheral extends RobotPeripheral {
    /**
     * Returns true if the peripheral is currently reporting a safe value.
     *
     * @return True or false.
     */
    public boolean checkSafety();


    public interface SafetyChecker<T> {
        /**
         * Returns true if val is a safe value.
         *
         * @return True or false.
         */
        public boolean checkSafety(T val);
    }

    public class Factory {
        public static <T> SafetyPeripheral fromSensor(final SensorPeripheral<T, ?> sensor, final SafetyChecker<T> lambda) {
            return new LambdaSafetyPeripheral<>(lambda, sensor);
        }
    }

    public class LambdaSafetyPeripheral<T> implements SafetyPeripheral {
        private final SafetyChecker<T> lambda;

        private final SensorPeripheral<T, ?> sensor;

        protected LambdaSafetyPeripheral(SafetyChecker<T> lambda, SensorPeripheral<T, ?> sensor) {
            this.lambda = lambda;
            this.sensor = sensor;
        }

        @Override
        public boolean checkSafety() {
            try {
                return lambda.checkSafety(sensor.read(null));
            } catch (Throwable e) {
                return false;
            }
        }

        @Override
        public void loop() {

        }

        @Override
        public void setup() {

        }
    }
}
