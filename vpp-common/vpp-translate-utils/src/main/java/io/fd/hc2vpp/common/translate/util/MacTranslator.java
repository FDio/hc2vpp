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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Splitter;
import io.fd.jvpp.core.types.MacAddress;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.PhysAddress;

/**
 * Trait providing logic for translation of MAC address data
 */
public interface MacTranslator {

    Splitter COLON_SPLITTER = Splitter.on(':');

    /**
     * Parse string represented mac address (using ":" as separator) into a byte array
     *
     * @param macAddress string representation of MAC address (using ":" as separator)
     * @return byte array mac address
     */
    @Nonnull
    default byte[] parseMac(@Nonnull final String macAddress) {
        final List<String> parts = COLON_SPLITTER.splitToList(macAddress);
        checkArgument(parts.size() == 6, "Mac address is expected to have 6 parts but was: %s", macAddress);
        return parseMacLikeString(parts);
    }

    /**
     * Parse string represented mac address (using ":" as separator) into a MacAddress in VPP
     *
     * @param macAddress string representation of MAC address (using ":" as separator)
     * @return VPP MacAddress
     */
    @Nonnull
    default MacAddress parseMacAddress(@Nonnull final String macAddress) {
        MacAddress mac = new MacAddress();
        mac.macaddress = parseMac(macAddress);
        return mac;
    }

    default byte[] parseMacLikeString(final List<String> strings) {
        return strings.stream().limit(6).map(this::parseHexByte).collect(
                () -> new byte[strings.size()],
                new BiConsumer<byte[], Byte>() {

                    private int i = -1;

                    @Override
                    public void accept(final byte[] bytes, final Byte aByte) {
                        bytes[++i] = aByte;
                    }
                },
                (bytes, bytes2) -> {
                    throw new UnsupportedOperationException("Parallel collect not supported");
                });
    }

    /**
     * Converts byte array to address string ,not separated with ":"
     */
    default String byteArrayToMacUnseparated(byte[] address) {
        checkArgument(address.length >= 6, "Illegal array length");
        return Hex.encodeHexString(Arrays.copyOf(address, 6));
    }

    /**
     * Converts byte array to address string ,separated with ":"
     */
    default String byteArrayToMacSeparated(byte[] address) {
        checkArgument(address.length >= 6, "Illegal array length");

        String unseparatedAddress = Hex.encodeHexString(Arrays.copyOf(address, 6));
        String separated = "";

        for (int i = 0; i < unseparatedAddress.length(); i = i + 2) {
            if (i == (unseparatedAddress.length() - 2)) {
                separated = separated + unseparatedAddress.substring(0 + i, 2 + i);
            } else {
                separated = separated + unseparatedAddress.substring(0 + i, 2 + i) + ":";
            }
        }

        return separated;
    }

    default PhysAddress toPhysAddress(final byte[] macAddress) {
        return new PhysAddress(byteArrayToMacSeparated(macAddress));
    }

    /**
     * Converts MAC string to byte array
     */
    default byte[] macToByteArray(String mac) {
        checkNotNull(mac, "MAC cannot be null");

        mac = mac.replace(":", "");

        try {
            return Hex.decodeHex(mac.toCharArray());
        } catch (DecoderException e) {
            throw new IllegalArgumentException("Unable to convert mac", e);
        }
    }

    default byte parseHexByte(final String aByte) {
        return (byte) Integer.parseInt(aByte, 16);
    }
}
