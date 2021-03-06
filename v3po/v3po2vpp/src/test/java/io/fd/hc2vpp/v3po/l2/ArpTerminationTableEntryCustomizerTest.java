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

package io.fd.hc2vpp.v3po.l2;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.fd.hc2vpp.common.test.write.WriterCustomizerTest;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.jvpp.VppBaseCallException;
import io.fd.jvpp.VppInvocationException;
import io.fd.jvpp.core.dto.BdIpMacAddDel;
import io.fd.jvpp.core.dto.BdIpMacAddDelReply;
import io.fd.jvpp.core.types.Address;
import io.fd.jvpp.core.types.AddressFamily;
import io.fd.jvpp.core.types.AddressUnion;
import io.fd.jvpp.core.types.BdIpMac;
import io.fd.jvpp.core.types.Ip4Address;
import io.fd.jvpp.core.types.MacAddress;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.v3po.rev190527.BridgeDomains;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.v3po.rev190527.bridge.domain.attributes.ArpTerminationTable;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.v3po.rev190527.bridge.domain.attributes.arp.termination.table.ArpTerminationTableEntry;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.v3po.rev190527.bridge.domain.attributes.arp.termination.table.ArpTerminationTableEntryBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.v3po.rev190527.bridge.domain.attributes.arp.termination.table.ArpTerminationTableEntryKey;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.v3po.rev190527.bridge.domains.BridgeDomain;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.v3po.rev190527.bridge.domains.BridgeDomainKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.PhysAddress;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class ArpTerminationTableEntryCustomizerTest extends WriterCustomizerTest {
    private static final String BD_CTX_NAME = "bd-test-instance";
    private static final String IFC_CTX_NAME = "ifc-test-instance";

    private static final String BD_NAME = "testBD0";
    private static final int BD_ID = 111;
    private static final String IFACE_NAME = "eth0";
    private static final int IFACE_ID = 123;
    private ArpTerminationTableEntryCustomizer customizer;
    private byte[] ipAddressRaw;
    private byte[] ip6AddressRaw;
    private byte[] physAddressRaw;
    private PhysAddress physAddress;
    private IpAddressNoZone ipAddress;
    private IpAddressNoZone ip6Address;
    private ArpTerminationTableEntry entry;
    private ArpTerminationTableEntry ip6entry;
    private InstanceIdentifier<ArpTerminationTableEntry> id;

    private static InstanceIdentifier<ArpTerminationTableEntry> getArpEntryId(final IpAddressNoZone ipAddress,
                                                                              final PhysAddress physAddress) {
        return InstanceIdentifier.create(BridgeDomains.class).child(BridgeDomain.class, new BridgeDomainKey(BD_NAME))
                .child(ArpTerminationTable.class)
                .child(ArpTerminationTableEntry.class, new ArpTerminationTableEntryKey(ipAddress, physAddress));
    }

    @Override
    public void setUpTest() throws Exception {
        customizer = new ArpTerminationTableEntryCustomizer(api, new NamingContext("generatedBdName", BD_CTX_NAME));

        ipAddressRaw = new byte[]{1, 2, 3, 4};
        ip6AddressRaw = new byte[]{32, 1, 13, -72, 10, 11, 18, -16, 0, 0, 0, 0, 0, 0, 0, 1};
        physAddressRaw = new byte[]{1, 2, 3, 4, 5, 6};
        physAddress = new PhysAddress("01:02:03:04:05:06");

        ipAddress = new IpAddressNoZone(Ipv4AddressNoZone.getDefaultInstance("1.2.3.4"));
        ip6Address =
                new IpAddressNoZone(Ipv6AddressNoZone.getDefaultInstance("2001:0db8:0a0b:12f0:0000:0000:0000:0001"));
        entry = generateArpEntry(ipAddress, physAddress);
        ip6entry = generateArpEntry(ip6Address, physAddress);
        id = getArpEntryId(ipAddress, physAddress);

        defineMapping(mappingContext, BD_NAME, BD_ID, BD_CTX_NAME);
        defineMapping(mappingContext, IFACE_NAME, IFACE_ID, IFC_CTX_NAME);
    }

    private void whenBdIpMacAddDelThenSuccess() {
        doReturn(future(new BdIpMacAddDelReply())).when(api).bdIpMacAddDel(any(BdIpMacAddDel.class));
    }

    private void whenBdIpMacAddDelThenFailure() {
        doReturn(failedFuture()).when(api).bdIpMacAddDel(any(BdIpMacAddDel.class));
    }

    private BdIpMacAddDel generateBdIpMacAddDelRequest(final byte[] ipAddress, final byte[] macAddress,
                                                       final byte isAdd) {
        final BdIpMacAddDel request = new BdIpMacAddDel();
        request.entry = new BdIpMac();
        Address address = new Address();
        address.af = AddressFamily.ADDRESS_IP4;
        Ip4Address ip4Address = new Ip4Address();
        ip4Address.ip4Address = ipAddress;
        address.un = new AddressUnion(ip4Address);
        request.entry.ip = address;
        MacAddress macAddr = new MacAddress();
        macAddr.macaddress = macAddress;
        request.entry.mac = macAddr;
        request.entry.bdId = BD_ID;
        request.isAdd = isAdd;
        return request;
    }

    private ArpTerminationTableEntry generateArpEntry(final IpAddressNoZone ipAddress, final PhysAddress physAddress) {
        final ArpTerminationTableEntryBuilder entry = new ArpTerminationTableEntryBuilder();
        entry.withKey(new ArpTerminationTableEntryKey(ipAddress, physAddress));
        entry.setPhysAddress(physAddress);
        entry.setIpAddress(ipAddress);
        return entry.build();
    }

    private void verifyBdIpMacAddDelWasInvoked(final BdIpMacAddDel expected) throws
            VppInvocationException {
        ArgumentCaptor<BdIpMacAddDel> argumentCaptor = ArgumentCaptor.forClass(BdIpMacAddDel.class);
        verify(api).bdIpMacAddDel(argumentCaptor.capture());
        final BdIpMacAddDel actual = argumentCaptor.getValue();
        assertArrayEquals(expected.entry.mac.macaddress, actual.entry.mac.macaddress);
        assertArrayEquals(expected.entry.ip.un.getIp4().ip4Address, actual.entry.ip.un.getIp4().ip4Address);
        assertEquals(expected.entry.bdId, actual.entry.bdId);
        assertEquals(expected.isAdd, actual.isAdd);
    }

    @Test
    public void testCreate() throws Exception {
        whenBdIpMacAddDelThenSuccess();
        customizer.writeCurrentAttributes(id, entry, writeContext);
        verifyBdIpMacAddDelWasInvoked(generateBdIpMacAddDelRequest(ipAddressRaw, physAddressRaw, (byte) 1));
    }

    @Test
    public void testCreateIpv6() throws Exception {
        whenBdIpMacAddDelThenSuccess();
        customizer.writeCurrentAttributes(id, ip6entry, writeContext);
        verifyBdIpMacAddDelWasInvoked(generateBdIpMacAddDelRequest(ip6AddressRaw, physAddressRaw, (byte) 1));
    }

    @Test
    public void testCreateFailed() throws Exception {
        whenBdIpMacAddDelThenFailure();
        try {
            customizer.writeCurrentAttributes(id, entry, writeContext);
        } catch (WriteFailedException e) {
            assertTrue(e.getCause() instanceof VppBaseCallException);
            verifyBdIpMacAddDelWasInvoked(generateBdIpMacAddDelRequest(ipAddressRaw, physAddressRaw, (byte) 1));
            return;
        }
        fail("WriteFailedException.CreateFailedException was expected");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUpdate() throws Exception {
        customizer.updateCurrentAttributes(InstanceIdentifier.create(ArpTerminationTableEntry.class),
                mock(ArpTerminationTableEntry.class),
                mock(ArpTerminationTableEntry.class), writeContext);
    }

    @Test
    public void testDelete() throws Exception {
        whenBdIpMacAddDelThenSuccess();
        customizer.deleteCurrentAttributes(id, entry, writeContext);
        verifyBdIpMacAddDelWasInvoked(generateBdIpMacAddDelRequest(ipAddressRaw, physAddressRaw, (byte) 0));
    }

    @Test
    public void testDeleteIpv6() throws Exception {
        whenBdIpMacAddDelThenSuccess();
        customizer.deleteCurrentAttributes(id, ip6entry, writeContext);
        verifyBdIpMacAddDelWasInvoked(generateBdIpMacAddDelRequest(ip6AddressRaw, physAddressRaw, (byte) 0));
    }

    @Test
    public void testDeleteFailed() throws Exception {
        whenBdIpMacAddDelThenFailure();
        try {
            customizer.deleteCurrentAttributes(id, entry, writeContext);
        } catch (WriteFailedException e) {
            assertTrue(e.getCause() instanceof VppBaseCallException);
            verifyBdIpMacAddDelWasInvoked(generateBdIpMacAddDelRequest(ipAddressRaw, physAddressRaw, (byte) 0));
            return;
        }
        fail("WriteFailedException.DeleteFailedException was expected");
    }
}
