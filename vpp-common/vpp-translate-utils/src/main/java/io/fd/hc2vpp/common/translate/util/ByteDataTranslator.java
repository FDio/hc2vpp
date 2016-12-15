/*
 * Copyright (c) 2016 Cisco and/or its affiliates.
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Trait providing logic for working with binary-based data
 */
public interface ByteDataTranslator {

    ByteDataTranslator INSTANCE = new ByteDataTranslator() {
    };

    byte BYTE_FALSE = 0;
    byte BYTE_TRUE = 1;

    /**
     * Returns 0 if argument is null or false, 1 otherwise.
     *
     * @param value Boolean value to be converted
     * @return byte value equal to 0 or 1
     */
    default byte booleanToByte(@Nullable final Boolean value) {
        return value != null && value
                ? BYTE_TRUE
                : BYTE_FALSE;
    }

    /**
     * Converts int to byte
     */
    default byte toByte(final int value) {
        return Integer.valueOf(value).byteValue();
    }

    /**
     * Converts short to byte
     */
    default byte toByte(final short value) {
        return Short.valueOf(value).byteValue();
    }

    /**
     * Returns Boolean.TRUE if argument is 0, Boolean.FALSE otherwise.
     *
     * @param value byte value to be converted
     * @return Boolean value
     * @throws IllegalArgumentException if argument is neither 0 nor 1
     */
    @Nonnull
    default Boolean byteToBoolean(final byte value) {
        if (value == BYTE_FALSE) {
            return Boolean.FALSE;
        } else if (value == BYTE_TRUE) {
            return Boolean.TRUE;
        }
        throw new IllegalArgumentException(String.format("0 or 1 was expected but was %d", value));
    }

    /**
     * Reverses bytes in the byte array
     *
     * @param bytes input array
     * @return reversed array
     */
    default byte[] reverseBytes(final byte[] bytes) {
        final byte[] reversed = new byte[bytes.length];
        int i = 1;
        for (byte aByte : bytes) {
            reversed[bytes.length - i++] = aByte;
        }

        return reversed;
    }

    /**
     * Return (interned) string from byte array while removing \u0000. Strings represented as fixed length byte[] from
     * vpp contain \u0000.
     */
    default String toString(final byte[] cString) {
        return new String(cString).replaceAll("\\u0000", "").intern();
    }

    /**
     * Converts signed byte(filled with unsigned value from vpp) to java integer
     *
     * For example unsigned C byte 128 is converted by jvpp to -128, this will return 128
     */
    default int toJavaByte(final byte vppByte) {
        return Byte.toUnsignedInt(vppByte);
    }
}