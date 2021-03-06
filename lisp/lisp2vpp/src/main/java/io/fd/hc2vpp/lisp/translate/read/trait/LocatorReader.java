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

package io.fd.hc2vpp.lisp.translate.read.trait;

import static com.google.common.base.Preconditions.checkNotNull;

import io.fd.hc2vpp.common.translate.util.JvppReplyConsumer;
import io.fd.hc2vpp.lisp.translate.read.dump.executor.params.LocatorDumpParams;
import io.fd.honeycomb.translate.util.read.cache.EntityDumpExecutor;
import io.fd.jvpp.core.dto.OneLocatorDetailsReplyDump;
import io.fd.jvpp.core.dto.OneLocatorDump;
import io.fd.jvpp.core.future.FutureJVppCore;
import javax.annotation.Nonnull;

/**
 * Provides common logic for reading of locators
 */
public interface LocatorReader extends JvppReplyConsumer {

    default EntityDumpExecutor<OneLocatorDetailsReplyDump, LocatorDumpParams> createLocatorDumpExecutor(
            @Nonnull final FutureJVppCore vppApi) {
        return (identifier, params) -> {
            checkNotNull(params, "Params for dump request not present");
            final OneLocatorDump request = new OneLocatorDump();
            request.lsIndex = params.getLocatorSetIndex();
            //flag that lsIndex is set
            request.isIndexSet = (byte) 1;

            return getReplyForRead(vppApi.oneLocatorDump(request).toCompletableFuture(), identifier);
        };
    }
}
