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

package ftc.team6460.javadeck.api;

/**
 * Represents a double value with a slew rate associated with it.
 */
public class SlewedDouble {
    private final double value;
    private final double slewRate;

    public double interpolate(double start, double time) {
        if (Math.abs(time * slewRate) > Math.abs(value - start)) {
            return value;
        } else {
            return start + Math.signum(value - start) * slewRate * time;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SlewedDouble)) return false;

        SlewedDouble that = (SlewedDouble) o;

        if (Double.compare(that.slewRate, slewRate) != 0) return false;
        if (Double.compare(that.value, value) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(value);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(slewRate);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public double getValue() {

        return value;
    }

    public double getSlewRate() {
        return slewRate;
    }

    public SlewedDouble(double value, double slewRate) {
        this.value = value;
        this.slewRate = Math.abs(slewRate);
    }
}
