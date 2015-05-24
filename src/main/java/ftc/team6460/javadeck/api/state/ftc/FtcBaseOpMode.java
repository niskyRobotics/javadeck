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

package ftc.team6460.javadeck.api.state.ftc;

import ftc.team6460.javadeck.api.motion.EncoderedMotor;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all Javadeck-based op modes for FTC.
 */
public abstract class FtcBaseOpMode /*extends OpMode*/ {


    /**
     * Actually does everything needed in the OpMode
     */
    protected abstract void doActions();

    private final Map<String, EncoderedMotor> motors = new HashMap<>();

    public synchronized EncoderedMotor getMotor(String mid) {
        // completely redo
        return null;
    }

    private EncoderedMotor createMotor0(String mid) {
        // do something with hardware map
        return null;
    }

    public void start() {
        new Thread(new Runnable() {
            public void run() {

                doActions();
            }
        }).start();

    }

    public void loop() {
        // update all motors and sensors
    }

}
