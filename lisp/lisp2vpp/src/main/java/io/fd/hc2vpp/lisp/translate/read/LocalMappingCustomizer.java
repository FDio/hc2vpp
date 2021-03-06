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

package io.fd.hc2vpp.lisp.translate.read;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.fd.hc2vpp.lisp.translate.read.dump.executor.params.MappingsDumpParams.EidType.valueOf;
import static io.fd.hc2vpp.lisp.translate.read.dump.executor.params.MappingsDumpParams.FilterType;
import static io.fd.hc2vpp.lisp.translate.read.dump.executor.params.MappingsDumpParams.MappingsDumpParamsBuilder;
import static io.fd.hc2vpp.lisp.translate.read.dump.executor.params.MappingsDumpParams.QuantityType;

import java.util.Optional;
import io.fd.hc2vpp.common.translate.util.FutureJVppCustomizer;
import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.hc2vpp.lisp.context.util.EidMappingContext;
import io.fd.hc2vpp.lisp.translate.read.dump.executor.params.MappingsDumpParams;
import io.fd.hc2vpp.lisp.translate.read.init.LispInitPathsMapper;
import io.fd.hc2vpp.lisp.translate.read.trait.MappingReader;
import io.fd.hc2vpp.lisp.translate.util.EidTranslator;
import io.fd.honeycomb.translate.read.ReadContext;
import io.fd.honeycomb.translate.read.ReadFailedException;
import io.fd.honeycomb.translate.spi.read.Initialized;
import io.fd.honeycomb.translate.spi.read.InitializingListReaderCustomizer;
import io.fd.honeycomb.translate.util.RWUtils;
import io.fd.honeycomb.translate.util.read.cache.DumpCacheManager;
import io.fd.jvpp.core.dto.OneEidTableDetails;
import io.fd.jvpp.core.dto.OneEidTableDetailsReplyDump;
import io.fd.jvpp.core.future.FutureJVppCore;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.eid.mapping.context.rev160801.contexts.eid.mapping.context.mappings.mapping.Eid;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.HmacKeyType;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.MappingId;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.dp.subtable.grouping.LocalMappings;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.dp.subtable.grouping.LocalMappingsBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.dp.subtable.grouping.local.mappings.LocalMapping;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.dp.subtable.grouping.local.mappings.LocalMappingBuilder;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.dp.subtable.grouping.local.mappings.LocalMappingKey;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.eid.table.grouping.eid.table.VniTable;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.hmac.key.grouping.HmacKeyBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.lisp.address.types.rev151105.lisp.address.Address;
import org.opendaylight.yangtools.concepts.Builder;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Customizer for reading {@code LocalMapping}<br> Currently unsupported by jvpp
 */
public class LocalMappingCustomizer
        extends FutureJVppCustomizer
        implements InitializingListReaderCustomizer<LocalMapping, LocalMappingKey, LocalMappingBuilder>, EidTranslator,
        MappingReader, LispInitPathsMapper {

    private static final Logger LOG = LoggerFactory.getLogger(LocalMappingCustomizer.class);

    private final DumpCacheManager<OneEidTableDetailsReplyDump, MappingsDumpParams> dumpManager;
    private final NamingContext locatorSetContext;
    private final EidMappingContext localMappingContext;

    public LocalMappingCustomizer(@Nonnull FutureJVppCore futureJvpp, @Nonnull NamingContext locatorSetContext,
                                  @Nonnull EidMappingContext localMappingsContext) {
        super(futureJvpp);
        this.locatorSetContext = checkNotNull(locatorSetContext, "Locator Set Mapping Context cannot be null");
        this.localMappingContext = checkNotNull(localMappingsContext, "Local mappings context cannot be null");
        this.dumpManager =
                new DumpCacheManager.DumpCacheManagerBuilder<OneEidTableDetailsReplyDump, MappingsDumpParams>()
                        .withExecutor(createMappingDumpExecutor(futureJvpp))
                        .acceptOnly(OneEidTableDetailsReplyDump.class)
                        .build();
    }

    @Override
    public LocalMappingBuilder getBuilder(InstanceIdentifier<LocalMapping> id) {
        return new LocalMappingBuilder();
    }

    @Override
    public void readCurrentAttributes(InstanceIdentifier<LocalMapping> id, LocalMappingBuilder builder,
                                      ReadContext ctx) throws ReadFailedException {
        checkState(id.firstKeyOf(LocalMapping.class) != null, "No key present for id({})", id);
        checkState(id.firstKeyOf(VniTable.class) != null, "Parent VNI table not specified");

        //checks whether there is an existing mapping
        final MappingId mappingId = id.firstKeyOf(LocalMapping.class).getId();
        checkState(localMappingContext.containsEid(mappingId, ctx.getMappingContext()));

        final long vni = id.firstKeyOf(VniTable.class).getVirtualNetworkIdentifier();

        final Eid eid = localMappingContext.getEid(mappingId, ctx.getMappingContext());

        //Requesting for specific mapping dump,only from local mappings with specified eid/vni/eid type
        final MappingsDumpParams dumpParams = new MappingsDumpParams.MappingsDumpParamsBuilder()
                .setEidSet(QuantityType.SPECIFIC)
                .setVni((int) vni)
                .setEid(getEidAsByteArray(eid))
                .setEidType(getEidType(eid))
                .setPrefixLength(getPrefixLength(eid))
                .build();

        LOG.debug("Dumping data for LocalMappings(id={})", id);
        final Optional<OneEidTableDetailsReplyDump> replyOptional =
                dumpManager.getDump(id, ctx.getModificationCache(), dumpParams);

        if (!replyOptional.isPresent() || replyOptional.get().oneEidTableDetails.isEmpty()) {
            return;
        }

        OneEidTableDetails details = replyOptional.get().oneEidTableDetails.stream()
                .filter(subtableFilterForLocalMappings(id))
                .filter(detail -> compareAddresses(eid.getAddress(), getAddressFromDumpDetail(detail)))
                .collect(RWUtils.singleItemCollector());

        //in case of local mappings,locator_set_index stands for interface index
        checkState(locatorSetContext.containsName(details.locatorSetIndex, ctx.getMappingContext()),
                "No Locator Set name found for index %s", details.locatorSetIndex);
        builder.setLocatorSet(locatorSetContext.getName(details.locatorSetIndex, ctx.getMappingContext()));
        builder.withKey(new LocalMappingKey(new MappingId(id.firstKeyOf(LocalMapping.class).getId())));
        builder.setEid(getArrayAsEidLocal(valueOf(details.eidType), details.eid, details.eidPrefixLen, details.vni));

        if (details.key != null) {
            builder.setHmacKey(
                    new HmacKeyBuilder()
                            .setKey(toString(details.key))
                            .setKeyType(HmacKeyType.forValue(details.keyId))
                            .build());
        } else {
            builder.setHmacKey(new HmacKeyBuilder().setKeyType(HmacKeyType.NoKey).build());
        }
    }

    private Address getAddressFromDumpDetail(final OneEidTableDetails detail) {
        return getArrayAsEidLocal(valueOf(detail.eidType), detail.eid, detail.eidPrefixLen, detail.vni).getAddress();
    }

    @Override
    public List<LocalMappingKey> getAllIds(InstanceIdentifier<LocalMapping> id, ReadContext context)
            throws ReadFailedException {

        checkState(id.firstKeyOf(VniTable.class) != null, "Parent VNI table not specified");
        final long vni = id.firstKeyOf(VniTable.class).getVirtualNetworkIdentifier();

        if (vni == 0) {
            // ignoring default vni mapping
            // its not relevant for us and we also don't store mapping for such eid's
            // such mapping is used to create helper local mappings to process remote ones
            return Collections.emptyList();
        }

        //request for all local mappings
        final MappingsDumpParams dumpParams = new MappingsDumpParamsBuilder()
                .setFilter(FilterType.LOCAL)
                .setEidSet(QuantityType.ALL)
                .build();

        LOG.debug("Dumping data for LocalMappings(id={})", id);
        final Optional<OneEidTableDetailsReplyDump> replyOptional =
                dumpManager.getDump(id, context.getModificationCache(), dumpParams);

        if (!replyOptional.isPresent() || replyOptional.get().oneEidTableDetails.isEmpty()) {
            return Collections.emptyList();
        }


        return replyOptional.get().oneEidTableDetails.stream()
                .filter(a -> a.vni == vni)
                .filter(subtableFilterForLocalMappings(id))
                .map(detail -> getArrayAsEidLocal(valueOf(detail.eidType), detail.eid, detail.eidPrefixLen, detail.vni))
                .map(localEid -> localMappingContext.getId(localEid, context.getMappingContext()))
                .map(MappingId::new)
                .map(LocalMappingKey::new)
                .collect(Collectors.toList());
    }

    @Override
    public void merge(Builder<? extends DataObject> builder, List<LocalMapping> readData) {
        ((LocalMappingsBuilder) builder).setLocalMapping(readData);
    }

    @Nonnull
    @Override
    public Initialized<? extends DataObject> init(@Nonnull InstanceIdentifier<LocalMapping> instanceIdentifier, @Nonnull LocalMapping localMapping, @Nonnull ReadContext readContext) {
        final KeyedInstanceIdentifier identifier = vniSubtablePath(instanceIdentifier)
                .child(LocalMappings.class)
                .child(LocalMapping.class, instanceIdentifier.firstKeyOf(LocalMapping.class));
        return Initialized.create(identifier, localMapping);
    }
}
