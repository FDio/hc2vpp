/*
 * Copyright (c) 2017 Cisco and/or its affiliates.
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
package io.fd.hc2vpp.v3po.interfacesstate.ip.v6;

import io.fd.hc2vpp.common.test.read.ListReaderCustomizerTest;
import io.fd.hc2vpp.v3po.interfacesstate.ip.InterfaceChildNodeTest;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.ReaderCustomizer;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.InterfacesState;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.state.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.Interface2;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.interfaces.state._interface.Ipv6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.interfaces.state._interface.Ipv6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.interfaces.state._interface.ipv6.Neighbor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.interfaces.state._interface.ipv6.NeighborBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev140616.interfaces.state._interface.ipv6.NeighborKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;


public class Ipv6NeighbourCustomizerTest extends ListReaderCustomizerTest<Neighbor, NeighborKey, NeighborBuilder>
        implements InterfaceChildNodeTest {

    private InstanceIdentifier<Neighbor> instanceIdentifier;

    public Ipv6NeighbourCustomizerTest() {
        super(Neighbor.class, Ipv6Builder.class);
    }

    @Override
    protected void setUp() throws Exception {
        instanceIdentifier = InstanceIdentifier.create(InterfacesState.class)
                .child(Interface.class, new InterfaceKey(IFACE_NAME))
                .augmentation(Interface2.class)
                .child(Ipv6.class)
                .child(Neighbor.class, new NeighborKey(IPV6_ONE_ADDRESS_COMPRESSED));
        defineMapping(mappingContext, IFACE_NAME, IFACE_ID, INTERFACE_CONTEXT_NAME);
        mockNeighborDump(api, dumpV6NeighborsIfaceOne(), v6Neighbors());
    }

    @Test
    public void testGetAll() throws ReadFailedException {
        verifyList(Arrays.asList(
                new NeighborKey(IPV6_ONE_ADDRESS_COMPRESSED),
                new NeighborKey(IPV6_TWO_ADDRESS_COMPRESSED)),
                getCustomizer().getAllIds(instanceIdentifier, ctx));
    }

    @Test
    public void readCurrent() throws ReadFailedException {
        final NeighborBuilder builder = new NeighborBuilder();
        getCustomizer().readCurrentAttributes(instanceIdentifier, builder, ctx);

        assertEquals(IPV6_ONE_ADDRESS_COMPRESSED, builder.getIp());
        assertEquals(MAC_THREE_ADDRESS, builder.getLinkLayerAddress());
    }

    @Override
    protected ReaderCustomizer<Neighbor, NeighborBuilder> initCustomizer() {
        return new Ipv6NeighbourCustomizer(api, INTERFACE_CONTEXT);
    }
}