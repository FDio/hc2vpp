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
import static com.google.common.base.Preconditions.checkState;

import com.google.common.net.InetAddresses;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpPrefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;

/**
 * Trait providing logic for translation of ipv6-related data
 */
public interface Ipv6Translator extends ByteDataTranslator {

    default byte[] ipv6AddressNoZoneToArray(@Nonnull final String address) {
        byte[] retval = new byte[16];

        //splits address and add ommited zeros for easier parsing
        List<String> segments = Arrays.asList(address.split(":"))
                .stream()
                .map(segment -> StringUtils.repeat('0', 4 - segment.length()) + segment)
                .collect(Collectors.toList());

        byte index = 0;
        for (String segment : segments) {

            String firstPart = segment.substring(0, 2);
            String secondPart = segment.substring(2);

            //first part should be ommited
            if ("00".equals(firstPart)) {
                index++;
            } else {
                retval[index++] = ((byte) Short.parseShort(firstPart, 16));
            }

            retval[index++] = ((byte) Short.parseShort(secondPart, 16));
        }

        return retval;
    }

    /**
     * Transform Ipv6 address to a byte array acceptable by VPP. VPP expects incoming byte array to be in the same order
     * as the address.
     *
     * @return byte array with address bytes
     */
    default byte[] ipv6AddressNoZoneToArray(@Nonnull final Ipv6AddressNoZone ipv6Addr) {
        return ipv6AddressNoZoneToArray(ipv6Addr.getValue());
    }

    /**
     * Creates address array from address part of {@link Ipv6Prefix}
     */
    default byte[] ipv6AddressPrefixToArray(@Nonnull final Ipv6Prefix ipv6Prefix) {
        checkNotNull(ipv6Prefix, "Cannot convert null prefix");

        return ipv6AddressNoZoneToArray(new Ipv6AddressNoZone(
                new Ipv6Address(ipv6Prefix.getValue().substring(0, ipv6Prefix.getValue().indexOf('/')))));
    }

    /**
     * Extracts {@link Ipv6Prefix} prefix
     */
    default byte extractPrefix(Ipv6Prefix data) {
        checkNotNull(data, "Cannot extract from null");

        return Byte.valueOf(data.getValue().substring(data.getValue().indexOf('/') + 1));
    }

    /**
     * Converts byte array to {@link Ipv6Prefix} with specified prefixLength
     */
    default Ipv6Prefix arrayToIpv6Prefix(final byte[] address, byte prefixLength) {
        Ipv6AddressNoZone addressPart = arrayToIpv6AddressNoZone(address);

        return new Ipv6Prefix(addressPart.getValue().concat("/").concat(String.valueOf(prefixLength)));
    }

    /**
     * Parse byte array returned by VPP representing an Ipv6 address. Expects array in non-reversed order
     *
     * @return Ipv6ddressNoZone containing string representation of IPv6 address constructed from submitted bytes. No
     * change in order.
     */
    @Nonnull
    default Ipv6AddressNoZone arrayToIpv6AddressNoZone(@Nonnull byte[] ip) {
        checkArgument(ip.length == 16, "Illegal array length");

        try {
            return new Ipv6AddressNoZone(InetAddresses.toAddrString(InetAddress.getByAddress(ip)));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Unable to parse ipv6", e);
        }
    }

    /**
     * Detects whether {@code IpAddress} is ipv6
     */
    default boolean isIpv6(IpAddress address) {
        checkNotNull(address, "Address cannot be null");

        checkState(!(address.getIpv4Address() == null && address.getIpv6Address() == null), "Invalid address");
        return address.getIpv6Address() != null;
    }

    /**
     * Detects whether {@code IpPrefix} is ipv6
     */
    default boolean isIpv6(IpPrefix address) {
        checkNotNull(address, "Address cannot be null");
        checkState(!(address.getIpv4Prefix() == null && address.getIpv6Prefix() == null), "Invalid address");
        return address.getIpv6Prefix() != null;
    }

    default Ipv6Prefix toIpv6Prefix(final byte[] address, final int prefix) {
        try {
            return new Ipv6Prefix(
                    String.format("%s/%s", InetAddress.getByAddress(address).getHostAddress(),
                            String.valueOf(prefix)));
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(
                    "Cannot create prefix for address[" + Arrays.toString(address) + "],prefix[" + prefix + "]");
        }
    }
}