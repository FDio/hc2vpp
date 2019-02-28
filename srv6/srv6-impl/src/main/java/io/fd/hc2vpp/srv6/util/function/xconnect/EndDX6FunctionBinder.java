/*
 * Copyright (c) 2018 Bell Canada, Pantheon Technologies and/or its affiliates.
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

package io.fd.hc2vpp.srv6.util.function.xconnect;

import com.google.common.base.Preconditions;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.hc2vpp.srv6.write.sid.request.XConnectLocalSidRequest;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.write.WriteContext;
import io.fd.jvpp.core.dto.SrLocalsidsDetails;
import io.fd.jvpp.core.future.FutureJVppCore;
import java.util.Collections;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6AddressNoZone;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.srv6._static.rev180301.multi.paths.v6.PathsBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.srv6._static.rev180301.multi.paths.v6.paths.Path;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.srv6._static.rev180301.multi.paths.v6.paths.PathBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.srv6._static.rev180301.srv6._static.cfg.Sid;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.srv6._static.rev180301.srv6._static.cfg.SidBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.srv6._static.rev180301.srv6.sid.config.EndDx6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.srv6._static.rev180301.srv6.sid.config.EndDx6Builder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.srv6.base.rev180301.PathAttrsCmn;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.srv6.types.rev180301.EndDX6;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.srv6.types.rev180301.Srv6EndpointType;

public class EndDX6FunctionBinder extends XConnectFunctionBinder {

    private static final int END_DX6_FUNCTION_VALUE = 6;

    public EndDX6FunctionBinder(@Nonnull FutureJVppCore api, @Nonnull NamingContext interfaceContext) {
        super(api, interfaceContext);
    }

    @Nonnull
    @Override
    public XConnectLocalSidRequest createWriteRequestAndBind(@Nonnull Sid data, @Nonnull WriteContext ctx) {
        XConnectLocalSidRequest request = new XConnectLocalSidRequest(getFutureJVpp());
        Preconditions.checkNotNull(data.getEndDx6(), "EndDx6 data cannot be null.");
        Preconditions.checkNotNull(data.getEndDx6().getPaths(), "EndDx6 paths cannot be null.");
        Preconditions.checkNotNull(data.getEndDx6().getPaths().getPath(), "EndDx6 list of paths cannot be null.");
        Optional<Path> firstPathOptional = data.getEndDx6().getPaths().getPath().stream().findFirst();
        Preconditions
                .checkArgument(firstPathOptional.isPresent(), "Failed to map data: {} for request: {}", data, request);
        request.setOutgoingInterfaceIndex(
                getInterfaceIndex(ctx.getMappingContext(), firstPathOptional.get().getInterface()));
        request.setNextHopAddress(new IpAddress(firstPathOptional.get().getNextHop()));
        request.setFunction(getBehaviourFunctionType());
        return request;
    }

    @Override
    public void translateFromDump(@Nonnull SrLocalsidsDetails data, @Nonnull ReadContext ctx,
                                  @Nonnull final SidBuilder builder) {
        Ipv6AddressNoZone ipv6AddressNoZone = arrayToIpv6AddressNoZone(data.xconnectNhAddr6);
        String interfaceName = getInterfaceName(ctx.getMappingContext(), data.xconnectIfaceOrVrfTable);
        EndDx6 endDx6 = new EndDx6Builder().setPaths(new PathsBuilder().setPath(Collections.singletonList(
                // TODO(HC2VPP-335): currently vpp allows to configure only one next hop
                // therefore setting path index, role and weight to constants
                new PathBuilder().setNextHop(ipv6AddressNoZone)
                        .setInterface(interfaceName)
                        .setPathIndex(DEFAULT_PATH_INDEX)
                        .setWeight(DEFAULT_WEIGHT)
                        .setRole(PathAttrsCmn.Role.PRIMARY)
                        .build())).build()).build();
        builder.setEndDx6(endDx6);
    }

    @Nonnull
    @Override
    public Class<? extends Srv6EndpointType> getHandledFunctionType() {
        return EndDX6.class;
    }

    @Override
    public int getBehaviourFunctionType() {
        return END_DX6_FUNCTION_VALUE;
    }
}
