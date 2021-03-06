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

package io.fd.hc2vpp.lisp.translate.write;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import io.fd.hc2vpp.lisp.translate.write.trait.SubtableWriterTestCase;
import io.fd.honeycomb.translate.write.WriteFailedException;
import io.fd.jvpp.VppCallbackException;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.eid.table.grouping.EidTable;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.eid.table.grouping.eid.table.VniTable;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.eid.table.grouping.eid.table.VniTableKey;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.eid.table.grouping.eid.table.vni.table.VrfSubtable;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang.lisp.rev171013.eid.table.grouping.eid.table.vni.table.VrfSubtableBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class VrfSubtableCustomizerTest extends SubtableWriterTestCase {

    private VrfSubtableCustomizer customizer;
    private InstanceIdentifier<VrfSubtable> validId;
    private VrfSubtable validData;

    @Before
    public void init() {
        customizer = new VrfSubtableCustomizer(api);
        validId = InstanceIdentifier.create(EidTable.class).child(VniTable.class, new VniTableKey(12L))
                .child(VrfSubtable.class);
        validData = new VrfSubtableBuilder().setTableId(10L).build();
    }

    @Test
    public void testWriteSuccessfull() throws WriteFailedException {
        whenAddDelEidTableAddDelMapSuccess();

        customizer.writeCurrentAttributes(validId, validData, writeContext);
        verifyAddDelEidTableAddDelMapInvokedCorrectly(1, 12, 10, 0);
    }

    @Test
    public void testWriteFailed() {
        whenAddDelEidTableAddDelMapFail();

        try {
            customizer.writeCurrentAttributes(validId, validData, writeContext);
        } catch (Exception e) {
            assertTrue(e instanceof WriteFailedException);

            final WriteFailedException realException = ((WriteFailedException) e);
            assertEquals(validId, realException.getFailedId());
            assertTrue(e.getCause() instanceof VppCallbackException);
            return;
        }

        fail("Test should throw exception");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUpdate() throws WriteFailedException {
        customizer.updateCurrentAttributes(validId, validData, validData, writeContext);
    }

    @Test
    public void testDeleteSuccessfull() throws WriteFailedException {
        whenAddDelEidTableAddDelMapSuccess();

        customizer.deleteCurrentAttributes(validId, validData, writeContext);
        verifyAddDelEidTableAddDelMapInvokedCorrectly(0, 12, 10, 0);
    }

    @Test
    public void testDeleteFailed() {
        whenAddDelEidTableAddDelMapFail();

        try {
            customizer.deleteCurrentAttributes(validId, validData, writeContext);
        } catch (Exception e) {
            assertTrue(e instanceof WriteFailedException);

            final WriteFailedException realException = ((WriteFailedException) e);
            assertEquals(validId, realException.getFailedId());
            assertTrue(e.getCause() instanceof VppCallbackException);
            return;
        }

        fail("Test should throw exception");
    }
}
