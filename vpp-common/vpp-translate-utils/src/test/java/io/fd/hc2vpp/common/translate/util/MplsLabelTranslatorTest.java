/*
 * Copyright (c) 2018 Cisco and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fd.hc2vpp.common.translate.util;

import static org.junit.Assert.assertEquals;

import io.fd.jvpp.core.types.FibMplsLabel;
import org.junit.Test;

public class MplsLabelTranslatorTest implements MplsLabelTranslator {
    @Test
    public void testTranslateLong() {
        final int expectedLabel = 1048575;
        final FibMplsLabel expected  = new FibMplsLabel();
        expected.label = expectedLabel;
        assertEquals(expected, translate(Long.valueOf(1048575)));
    }
    @Test
    public void testTranslateInt() {
        final int expectedLabel = 11;
        final FibMplsLabel expected  = new FibMplsLabel();
        expected.label = expectedLabel;
        assertEquals(expected, translate(expectedLabel));
    }

}
