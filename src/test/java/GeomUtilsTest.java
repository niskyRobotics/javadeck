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

import ftc.team6460.javadeck.api.planner.geom.GeometryUtils;
import ftc.team6460.javadeck.api.planner.geom.Point2D;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by hexafraction on 4/11/15.
 */
public class GeomUtilsTest {
    @Test
    public void testLeftRight() throws Exception {
        Assert.assertThat("Should be left", GeometryUtils.isLeft(new Point2D(0, 0), new Point2D(1, 1), new Point2D(0, 1)), new GreaterThanZero());
        Assert.assertThat("Should be right", GeometryUtils.isLeft(new Point2D(0, 0), new Point2D(2, 3), new Point2D(2, 0)), new LessThanZero());
        Assert.assertEquals(0, GeometryUtils.isLeft(new Point2D(0, 0), new Point2D(2, 7), new Point2D(400, 1400)));
    }

    private static class LessThanZero extends BaseMatcher<Long> {
        @Override
        public boolean matches(Object item) {
            if (item instanceof Long) {
                return (Long) item < 0;
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {

        }
    }

    private static class GreaterThanZero extends BaseMatcher<Long> {
        @Override
        public boolean matches(Object item) {
            if (item instanceof Long) {
                return (Long) item > 0;
            }
            return false;
        }

        @Override
        public void describeTo(Description description) {

        }
    }
}
