/*
 * Copyright (c) 2015 Cisco and/or its affiliates.
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

package io.fd.honeycomb.lisp.translate.util;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.fd.honeycomb.lisp.translate.read.dump.executor.params.MappingsDumpParams.EidType;
import static io.fd.honeycomb.lisp.translate.read.dump.executor.params.MappingsDumpParams.EidType.IPV4;
import static io.fd.honeycomb.lisp.translate.read.dump.executor.params.MappingsDumpParams.EidType.IPV6;
import static io.fd.honeycomb.lisp.translate.read.dump.executor.params.MappingsDumpParams.EidType.MAC;

import io.fd.honeycomb.translate.v3po.util.TranslateUtils;
import java.util.Arrays;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.LispAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv4Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.Mac;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.address.MacBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.adjacencies.adjacency.LocalEid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.adjacencies.adjacency.RemoteEid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.local.mappings.local.mapping.Eid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.local.mappings.local.mapping.EidBuilder;


/**
 * Helper class that converts {@code SimpleAddress} to eid format for vpp
 */
public final class EidConverter {

    private EidConverter() {
        throw new UnsupportedOperationException("Cannot instantiate utility class " + EidConverter.class.getName());
    }

    public static byte getPrefixLength(LocalEid address) {
        return resolverPrefixLength(address.getAddress());
    }

    public static byte getPrefixLength(RemoteEid address) {
        return resolverPrefixLength(address.getAddress());
    }

    public static byte getPrefixLength(
            org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.eid.mapping.context.rev160801.contexts.eid.mapping.context.mappings.mapping.Eid address) {
        return resolverPrefixLength(address.getAddress());
    }

    public static byte getPrefixLength(
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.local.mappings.local.mapping.Eid address) {
        return resolverPrefixLength(address.getAddress());
    }

    public static byte getPrefixLength(
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.remote.mappings.remote.mapping.Eid address) {
        return resolverPrefixLength(address.getAddress());
    }

    private static byte resolverPrefixLength(Address address) {

        switch (resolveType(address)) {
            case IPV4:
                return 32;
            case IPV6:
                return (byte) 128;
            case MAC:
                return 0;
            default:
                throw new IllegalArgumentException("Illegal type");
        }
    }

    public static Eid getArrayAsEidLocal(EidType type, byte[] address) {

        switch (type) {
            case IPV4: {
                return new EidBuilder().setAddress(
                        new Ipv4Builder().setIpv4(TranslateUtils.arrayToIpv4AddressNoZoneReversed(address)).build())
                        .build();
            }
            case IPV6: {
                return new EidBuilder().setAddress(
                        new Ipv6Builder().setIpv6(TranslateUtils.arrayToIpv6AddressNoZoneReversed(address)).build())
                        .build();
            }
            case MAC: {
                return new EidBuilder().setAddress(
                        new MacBuilder().setMac(new MacAddress(TranslateUtils.byteArrayToMacSeparated(address)))
                                .build()).build();
            }
            default: {
                throw new IllegalStateException("Unknown type detected");
            }
        }
    }

    public static org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.remote.mappings.remote.mapping.Eid getArrayAsEidRemote(
            EidType type, byte[] address) {

        switch (type) {
            case IPV4: {
                return new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.remote.mappings.remote.mapping.EidBuilder()
                        .setAddress(
                                new Ipv4Builder().setIpv4(TranslateUtils.arrayToIpv4AddressNoZoneReversed(address))
                                        .build())
                        .build();
            }
            case IPV6: {
                return new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.remote.mappings.remote.mapping.EidBuilder()
                        .setAddress(
                                new Ipv6Builder().setIpv6(TranslateUtils.arrayToIpv6AddressNoZoneReversed(address))
                                        .build())
                        .build();
            }
            case MAC: {
                return new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.remote.mappings.remote.mapping.EidBuilder()
                        .setAddress(
                                new MacBuilder().setMac(new MacAddress(TranslateUtils.byteArrayToMacSeparated(address)))
                                        .build()).build();
            }
            default: {
                throw new IllegalStateException("Unknown type detected");
            }
        }
    }

    public static String getArrayAsEidString(
            EidType type, byte[] address) {
        switch (type) {
            case IPV4: {
                return TranslateUtils.arrayToIpv4AddressNoZoneReversed(address).getValue();
            }
            case IPV6: {
                return TranslateUtils.arrayToIpv6AddressNoZoneReversed(address).getValue();
            }
            case MAC: {
                //as wrong as it looks ,its right(second param is not end index,but count)
                return TranslateUtils.byteArrayToMacSeparated(Arrays.copyOfRange(address, 0, 6));
            }
            default: {
                throw new IllegalStateException("Unknown type detected");
            }
        }
    }


    public static EidType getEidType(
            org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.eid.mapping.context.rev160801.contexts.eid.mapping.context.mappings.mapping.Eid address) {
        checkNotNull(address, "SimpleAddress cannot be null");

        return resolveType(address.getAddress());
    }

    public static EidType getEidType(
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.local.mappings.local.mapping.Eid address) {
        checkNotNull(address, "SimpleAddress cannot be null");

        return resolveType(address.getAddress());
    }


    public static EidType getEidType(
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.remote.mappings.remote.mapping.Eid address) {
        checkNotNull(address, "Address cannot be null");

        return resolveType(address.getAddress());
    }


    public static EidType getEidType(final LocalEid address) {
        checkNotNull(address, "Address cannot be null");

        return resolveType(address.getAddress());
    }

    public static EidType getEidType(final RemoteEid address) {
        checkNotNull(address, "Address cannot be null");

        return resolveType(address.getAddress());
    }

    private static EidType resolveType(
            Address address) {

        if (address instanceof Ipv4) {
            return IPV4;
        } else if (address instanceof Ipv6) {
            return IPV6;
        } else if (address instanceof Mac) {
            return MAC;
        } else {
            //TODO - other types
            throw new IllegalStateException("Unknown type detected");
        }
    }

    public static byte[] getEidAsByteArray(
            org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.eid.mapping.context.rev160801.contexts.eid.mapping.context.mappings.mapping.Eid address) {
        checkNotNull(address, "Eid cannot be null");

        return resolveByteArray(getEidType(address), address.getAddress());
    }

    public static byte[] getEidAsByteArray(
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.local.mappings.local.mapping.Eid address) {
        checkNotNull(address, "Eid cannot be null");

        return resolveByteArray(getEidType(address), address.getAddress());
    }

    public static byte[] getEidAsByteArray(
            org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.lisp.rev160520.eid.table.grouping.eid.table.vni.table.remote.mappings.remote.mapping.Eid address) {
        checkNotNull(address, "Eid cannot be null");

        return resolveByteArray(getEidType(address), address.getAddress());
    }

    public static byte[] getEidAsByteArray(final LocalEid address) {
        checkNotNull(address, "Eid cannot be null");

        return resolveByteArray(getEidType(address), address.getAddress());
    }


    public static byte[] getEidAsByteArray(final RemoteEid address) {
        checkNotNull(address, "Eid cannot be null");

        return resolveByteArray(getEidType(address), address.getAddress());
    }

    private static byte[] resolveByteArray(EidType type, Address address) {
        switch (type) {
            case IPV4:
                return TranslateUtils
                        .ipv4AddressNoZoneToArray(new Ipv4AddressNoZone(((Ipv4) address).getIpv4()));
            case IPV6:
                return TranslateUtils
                        .ipv6AddressNoZoneToArray(new Ipv6AddressNoZone(((Ipv6) address).getIpv6()));
            case MAC:
                return TranslateUtils.parseMac(((Mac) address).getMac().getValue());
            default:
                //TODO - other types
                throw new IllegalArgumentException("Unsupported type");
        }
    }

    public static boolean compareEids(
            LispAddress first,
            LispAddress second) {

        return compareAddresses(checkNotNull(first, "First eid is null").getAddress(),
                checkNotNull(second, "Second eid is null").getAddress());
    }

    public static boolean compareAddresses(Address firstAddress, Address secondAddress) {

        checkNotNull(firstAddress, "First address is null");
        checkNotNull(secondAddress, "Second address is null");

        if (firstAddress instanceof Ipv4 && secondAddress instanceof Ipv4) {
            return ((Ipv4) firstAddress).getIpv4().getValue().equals(((Ipv4) secondAddress).getIpv4().getValue());
        }

        if (firstAddress instanceof Ipv6 && secondAddress instanceof Ipv6) {
            return ((Ipv6) firstAddress).getIpv6().getValue().equals(((Ipv6) secondAddress).getIpv6().getValue());
        }

        if (firstAddress instanceof Mac && secondAddress instanceof Mac) {
            return ((Mac) firstAddress).getMac().getValue().equals(((Mac) secondAddress).getMac().getValue());
        }

        throw new IllegalArgumentException("Unsupported eid type " + firstAddress.getClass());
    }
}