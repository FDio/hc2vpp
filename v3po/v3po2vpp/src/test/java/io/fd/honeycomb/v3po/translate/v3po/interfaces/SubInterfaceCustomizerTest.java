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

package io.fd.honeycomb.v3po.translate.v3po.interfaces;

import static io.fd.honeycomb.v3po.translate.v3po.test.ContextTestUtils.getMapping;
import static io.fd.honeycomb.v3po.translate.v3po.test.ContextTestUtils.getMappingIid;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.common.base.Optional;
import io.fd.honeycomb.v3po.translate.MappingContext;
import io.fd.honeycomb.v3po.translate.v3po.test.TestHelperUtils;
import io.fd.honeycomb.v3po.translate.v3po.util.NamingContext;
import io.fd.honeycomb.v3po.translate.write.WriteContext;
import io.fd.honeycomb.v3po.translate.write.WriteFailedException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.naming.context.rev160513.contexts.naming.context.mappings.Mapping;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.CVlan;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.Dot1qTagVlanType;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.Dot1qVlanId;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.SVlan;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.dot1q.tag.or.any.Dot1qTag;
import org.opendaylight.yang.gen.v1.urn.ieee.params.xml.ns.yang.dot1q.types.rev150626.dot1q.tag.or.any.Dot1qTagBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.Interfaces;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.Interface;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.interfaces.rev140508.interfaces.InterfaceKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.SubinterfaceAugmentation;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527._802dot1ad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.interfaces._interface.SubInterfaces;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.interfaces._interface.sub.interfaces.SubInterface;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.interfaces._interface.sub.interfaces.SubInterfaceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.interfaces._interface.sub.interfaces.SubInterfaceKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.match.attributes.match.type.vlan.tagged.VlanTaggedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.sub._interface.base.attributes.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.sub._interface.base.attributes.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.sub._interface.base.attributes.TagsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.sub._interface.base.attributes.tags.Tag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.sub._interface.base.attributes.tags.TagBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.sub._interface.base.attributes.tags.TagKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.openvpp.jvpp.VppBaseCallException;
import org.openvpp.jvpp.dto.CreateSubif;
import org.openvpp.jvpp.dto.CreateSubifReply;
import org.openvpp.jvpp.dto.SwInterfaceSetFlags;
import org.openvpp.jvpp.dto.SwInterfaceSetFlagsReply;
import org.openvpp.jvpp.future.FutureJVpp;

public class SubInterfaceCustomizerTest {

    @Mock
    private FutureJVpp api;
    @Mock
    private WriteContext writeContext;
    @Mock
    private MappingContext mappingContext;

    private NamingContext namingContext;
    private SubInterfaceCustomizer customizer;
    public static final String SUPER_IF_NAME = "local0";
    public static final int SUPER_IF_ID = 1;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        namingContext = new NamingContext("generatedSubInterfaceName", "test-instance");
        doReturn(mappingContext).when(writeContext).getMappingContext();
        // TODO create base class for tests using vppApi
        customizer = new SubInterfaceCustomizer(api, namingContext);
        doReturn(getMapping(SUPER_IF_NAME, SUPER_IF_ID)).when(mappingContext)
                .read(getMappingIid(SUPER_IF_NAME, "test-instance"));
    }

    private SubInterface generateSubInterface(final String superIfName, final boolean enabled) {
        SubInterfaceBuilder builder = new SubInterfaceBuilder();
        builder.setVlanType(_802dot1ad.class);
        builder.setIdentifier(11L);
        final TagsBuilder tags = new TagsBuilder();
        final List<Tag> list = new ArrayList<>();
        list.add(generateTag((short) 0, SVlan.class,  new Dot1qTag.VlanId(new Dot1qVlanId(100))));
        list.add(generateTag((short) 1, CVlan.class,  new Dot1qTag.VlanId(new Dot1qVlanId(200))));

        tags.setTag(list);

        builder.setTags(tags.build());

        builder.setMatch(generateMatch());
        builder.setEnabled(enabled);
        return builder.build();
    }

    private static Tag generateTag(final short index, final Class<? extends Dot1qTagVlanType> tagType,
                                   final Dot1qTag.VlanId vlanId) {
        TagBuilder tag = new TagBuilder();
        tag.setIndex(index);
        tag.setKey(new TagKey(index));
        final Dot1qTagBuilder dtag = new Dot1qTagBuilder();
        dtag.setTagType(tagType);
        dtag.setVlanId(vlanId);
        tag.setDot1qTag(dtag.build());
        return tag.build();
    }

    private static Match generateMatch() {
        final MatchBuilder match = new MatchBuilder();
        final VlanTaggedBuilder tagged = new VlanTaggedBuilder();
        tagged.setMatchExactTags(true);
        match.setMatchType(
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.vpp.vlan.rev150527.match.attributes.match.type.VlanTaggedBuilder()
                        .setVlanTagged(tagged.build()).build());
        return match.build();
    }

    private CreateSubif generateSubInterfaceRequest(final int superIfId) {
        CreateSubif request = new CreateSubif();
        request.subId = 11;
        request.swIfIndex = superIfId;
        request.twoTags = 1;
        request.dot1Ad = 1;
        request.outerVlanId = 100;
        request.innerVlanId = 200;
        return request;
    }

    private SwInterfaceSetFlags generateSwInterfaceEnableRequest(final int swIfIndex) {
        SwInterfaceSetFlags request = new SwInterfaceSetFlags();
        request.swIfIndex = swIfIndex;
        request.adminUpDown = 1;
        return request;
    }

    private InstanceIdentifier<SubInterface> getSubInterfaceId(final String name, final long index) {
        return InstanceIdentifier.create(Interfaces.class).child(Interface.class, new InterfaceKey(name)).augmentation(
                SubinterfaceAugmentation.class).child(SubInterfaces.class).child(SubInterface.class, new SubInterfaceKey(index));
    }

    private void whenCreateSubifThenSuccess() throws ExecutionException, InterruptedException, VppBaseCallException {
        final CompletableFuture<CreateSubifReply> replyFuture = new CompletableFuture<>();
        final CreateSubifReply reply = new CreateSubifReply();
        replyFuture.complete(reply);
        doReturn(replyFuture).when(api).createSubif(any(CreateSubif.class));
    }

    /**
     * Failure response send
     */
    private void whenCreateSubifThenFailure() throws ExecutionException, InterruptedException, VppBaseCallException {
        doReturn(TestHelperUtils.<CreateSubifReply>createFutureException()).when(api).createSubif(any(CreateSubif.class));
    }

    private void whenSwInterfaceSetFlagsThenSuccess() throws ExecutionException, InterruptedException, VppBaseCallException {
        final CompletableFuture<SwInterfaceSetFlagsReply> replyFuture = new CompletableFuture<>();
        final SwInterfaceSetFlagsReply reply = new SwInterfaceSetFlagsReply();
        replyFuture.complete(reply);
        doReturn(replyFuture).when(api).swInterfaceSetFlags(any(SwInterfaceSetFlags.class));
    }

    private CreateSubif verifyCreateSubifWasInvoked(final CreateSubif expected) throws VppBaseCallException {
        ArgumentCaptor<CreateSubif> argumentCaptor = ArgumentCaptor.forClass(CreateSubif.class);
        verify(api).createSubif(argumentCaptor.capture());
        final CreateSubif actual = argumentCaptor.getValue();

        assertEquals(expected.swIfIndex, actual.swIfIndex);
        assertEquals(expected.subId, actual.subId);
        assertEquals(expected.noTags, actual.noTags);
        assertEquals(expected.oneTag, actual.oneTag);
        assertEquals(expected.twoTags, actual.twoTags);
        assertEquals(expected.dot1Ad, actual.dot1Ad);
        assertEquals(expected.exactMatch, actual.exactMatch);
        assertEquals(expected.defaultSub, actual.defaultSub);
        assertEquals(expected.outerVlanIdAny, actual.outerVlanIdAny);
        assertEquals(expected.innerVlanIdAny, actual.innerVlanIdAny);
        assertEquals(expected.outerVlanId, actual.outerVlanId);
        assertEquals(expected.innerVlanId, actual.innerVlanId);
        return actual;
    }

    private SwInterfaceSetFlags verifySwInterfaceSetFlagsWasInvoked(final SwInterfaceSetFlags expected) throws VppBaseCallException{
        ArgumentCaptor<SwInterfaceSetFlags> argumentCaptor = ArgumentCaptor.forClass(SwInterfaceSetFlags.class);
        verify(api).swInterfaceSetFlags(argumentCaptor.capture());
        final SwInterfaceSetFlags actual = argumentCaptor.getValue();

        assertEquals(expected.swIfIndex, actual.swIfIndex);
        assertEquals(expected.adminUpDown, actual.adminUpDown);
        return actual;
    }

    @Test
    public void testCreate() throws Exception {
        final SubInterface subInterface = generateSubInterface(SUPER_IF_NAME, false);
        final String superIfName = "local0";
        final String subIfaceName = "local0.11";
        final long subifIndex = 11;
        final InstanceIdentifier<SubInterface> id = getSubInterfaceId(superIfName, subifIndex);

        whenCreateSubifThenSuccess();
        whenSwInterfaceSetFlagsThenSuccess();

        customizer.writeCurrentAttributes(id, subInterface, writeContext);

        verifyCreateSubifWasInvoked(generateSubInterfaceRequest(SUPER_IF_ID));
        verify(mappingContext)
                .put(eq(getMappingIid(subIfaceName, "test-instance")), eq(getMapping(subIfaceName, 0).get()));
    }

    @Test
    public void testCreateFailed() throws Exception {
        final SubInterface subInterface = generateSubInterface(SUPER_IF_NAME, false);
        final String superIfName = "local0";
        final String subIfaceName = "local0.11";
        final long subifIndex = 11;
        final InstanceIdentifier<SubInterface> id = getSubInterfaceId(superIfName, subifIndex);

        whenCreateSubifThenFailure();

        try {
            customizer.writeCurrentAttributes(id, subInterface, writeContext);
        } catch (WriteFailedException.CreateFailedException e) {
            assertTrue(e.getCause() instanceof VppBaseCallException );
            verifyCreateSubifWasInvoked(generateSubInterfaceRequest(SUPER_IF_ID));
            verify(mappingContext, times(0)).put(
                    eq(getMappingIid(subIfaceName, "test-instance")),
                    eq(getMapping(subIfaceName, 0).get()));
            return;
        }
        fail("WriteFailedException.CreateFailedException was expected");
    }

    @Test
    public void testUpdate() throws Exception {
        final SubInterface before = generateSubInterface("eth0", false);
        final SubInterface after = generateSubInterface("eth1", true);
        final String superIfName = "local0";
        final String subIfaceName = "local0.11";
        final int subifIndex = 11;
        final InstanceIdentifier<SubInterface> id = getSubInterfaceId(superIfName, subifIndex);

        whenSwInterfaceSetFlagsThenSuccess();
        final Optional<Mapping> ifcMapping = getMapping(subIfaceName, subifIndex);
        doReturn(ifcMapping).when(mappingContext).read(any());

        customizer.updateCurrentAttributes(id, before, after, writeContext);

        verifySwInterfaceSetFlagsWasInvoked(generateSwInterfaceEnableRequest(subifIndex));
    }

    @Test
    public void testUpdateNoStateChange() throws Exception {
        final SubInterface before = generateSubInterface(SUPER_IF_NAME, false);
        final SubInterface after = generateSubInterface(SUPER_IF_NAME, false);
        customizer.updateCurrentAttributes(null, before, after, writeContext);

        verify(api, never()).swInterfaceSetFlags(any());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testDelete() throws Exception {
        final SubInterface subInterface = generateSubInterface("eth0", false);
        customizer.deleteCurrentAttributes(null, subInterface, writeContext);
    }
}