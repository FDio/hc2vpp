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

package io.fd.hc2vpp.l3.write.ipv4;

import io.fd.hc2vpp.common.translate.util.AddressTranslator;
import io.fd.hc2vpp.common.translate.util.ByteDataTranslator;
import io.fd.hc2vpp.common.translate.util.FutureJVppCustomizer;
import io.fd.hc2vpp.common.translate.util.JvppReplyConsumer;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.hc2vpp.l3.utils.ip.write.IpWriter;
import io.fd.honeycomb.translate.spi.write.ListWriterCustomizer;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.jvpp.core.dto.IpNeighborAddDel;
import io.fd.jvpp.core.future.FutureJVppCore;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev180220.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev180222.interfaces._interface.Ipv4;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev180222.interfaces._interface.ipv4.Neighbor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.ip.rev180222.interfaces._interface.ipv4.NeighborKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Customizer for writing {@link Neighbor} for {@link Ipv4}.
 */
public class Ipv4NeighbourCustomizer extends FutureJVppCustomizer
        implements ListWriterCustomizer<Neighbor, NeighborKey>, ByteDataTranslator, AddressTranslator,
        IpWriter, JvppReplyConsumer {


    private static final Logger LOG = LoggerFactory.getLogger(Ipv4NeighbourCustomizer.class);
    private final NamingContext interfaceContext;

    public Ipv4NeighbourCustomizer(final FutureJVppCore futureJVppCore, final NamingContext interfaceContext) {
        super(futureJVppCore);
        this.interfaceContext = interfaceContext;
    }

    @Override
    public void writeCurrentAttributes(@Nonnull InstanceIdentifier<Neighbor> id, @Nonnull Neighbor data,
                                       @Nonnull WriteContext writeContext)
            throws WriteFailedException {

        LOG.debug("Processing request for Neighbour {} write", id);

        addDelNeighbour(id, () -> {
            IpNeighborAddDel request = preBindRequest(true);

            request.neighbor.macAddress = parseMacAddress(data.getLinkLayerAddress().getValue());
            request.neighbor.ipAddress = ipv4AddressNoZoneToAddress(data.getIp());
            request.neighbor.swIfIndex = interfaceContext
                    .getIndex(id.firstKeyOf(Interface.class).getName(), writeContext.getMappingContext());
            return request;
        }, getFutureJVpp());
        LOG.debug("Neighbour {} successfully written", id);
    }

    @Override
    public void deleteCurrentAttributes(@Nonnull InstanceIdentifier<Neighbor> id, @Nonnull Neighbor data,
                                        @Nonnull WriteContext writeContext)
            throws WriteFailedException {

        LOG.debug("Processing request for Neighbour {} delete", id);

        addDelNeighbour(id, () -> {
            IpNeighborAddDel request = preBindRequest(false);

            request.neighbor.macAddress = parseMacAddress(data.getLinkLayerAddress().getValue());
            request.neighbor.ipAddress = ipv4AddressNoZoneToAddress(data.getIp());
            request.neighbor.swIfIndex = interfaceContext
                    .getIndex(id.firstKeyOf(Interface.class).getName(), writeContext.getMappingContext());
            return request;
        }, getFutureJVpp());
        LOG.debug("Neighbour {} successfully deleted", id);
    }
}