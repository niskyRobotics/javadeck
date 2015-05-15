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

package ftc.team6460.javadeck.api.peripheral.impl;

import ftc.team6460.javadeck.api.motion.EncoderedMotor;
import ftc.team6460.javadeck.api.peripheral.PeripheralCommunicationException;
import ftc.team6460.javadeck.api.peripheral.PeripheralInoperableException;
import ftc.team6460.javadeck.api.safety.SafetyGroup;

/**
 * Created by hexafraction on 5/15/15.
 */
public class FtcImplEncoderedMotor extends EncoderedMotor {
    private volatile double val;
    private volatile double lastEncoder;

    public FtcImplEncoderedMotor(SafetyGroup safetyGroup, double antiStallThreshold, double antiStallTimeout, double maxStallPower, int encoderDirection) {
        super(safetyGroup, antiStallThreshold, antiStallTimeout, maxStallPower, encoderDirection);
    }

    public FtcImplEncoderedMotor(double antiStallThreshold, double antiStallTimeout, double maxStallPower, int encoderDirection) {
        super(antiStallThreshold, antiStallTimeout, maxStallPower, encoderDirection);
    }

    public /*undocumented*/ double getWrittenVal(){
        return val;
    }


    public /*undocumented*/ void setEncoder(double encVal){
        this.lastEncoder = encVal;
    }

    @Override
    protected void doWrite(double val) {
        this.val = val;
    }

    @Override
    public void addSafetyGroup(SafetyGroup grp) {
        //noop
    }

    @Override
    public Double read(Void params) throws InterruptedException, PeripheralCommunicationException, PeripheralInoperableException {
        return lastEncoder;
    }

    @Override
    public void calibrate(Double val, Void params) throws InterruptedException, UnsupportedOperationException, PeripheralInoperableException, PeripheralCommunicationException {
        // noop, cannot calibrate an encoder

    }
}
