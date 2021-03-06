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

package io.fd.hc2vpp.acl;

import com.google.inject.Inject;
import io.fd.hc2vpp.common.translate.util.JvppReplyConsumer;
import io.fd.honeycomb.binding.init.ProviderTrait;
import io.fd.honeycomb.data.init.ShutdownHandler;
import io.fd.jvpp.JVppRegistry;
import io.fd.jvpp.VppBaseCallException;
import io.fd.jvpp.acl.JVppAclImpl;
import io.fd.jvpp.acl.dto.AclPluginGetVersion;
import io.fd.jvpp.acl.dto.AclPluginGetVersionReply;
import io.fd.jvpp.acl.future.FutureJVppAclFacade;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JVppAclProvider extends ProviderTrait<FutureJVppAclFacade> implements JvppReplyConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(JVppAclProvider.class);

    @Inject
    private JVppRegistry registry;

    @Inject
    private ShutdownHandler shutdownHandler;

    private static JVppAclImpl initAclApi(final ShutdownHandler shutdownHandler) {
        final JVppAclImpl jvppAcl = new JVppAclImpl();
        // Free jvpp-acl plugin's resources on shutdown
        shutdownHandler.register("jvpp-acl", jvppAcl);
        return jvppAcl;
    }

    @Override
    protected FutureJVppAclFacade create() {
        try {
            return reportVersionAndGet(initAclApi(shutdownHandler));
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open VPP management connection", e);
        } catch (TimeoutException | VppBaseCallException e) {
            throw new IllegalStateException("Unable to load ACL plugin version", e);
        }
    }

    private FutureJVppAclFacade reportVersionAndGet(final JVppAclImpl jvppAcl)
            throws IOException, TimeoutException, VppBaseCallException {
        final FutureJVppAclFacade futureFacade = new FutureJVppAclFacade(registry, jvppAcl);
        final AclPluginGetVersionReply pluginVersion =
                getReply(futureFacade.aclPluginGetVersion(new AclPluginGetVersion()).toCompletableFuture());
        LOG.info("Acl plugin successfully loaded[version {}.{}]", pluginVersion.major, pluginVersion.minor);
        return futureFacade;
    }
}
