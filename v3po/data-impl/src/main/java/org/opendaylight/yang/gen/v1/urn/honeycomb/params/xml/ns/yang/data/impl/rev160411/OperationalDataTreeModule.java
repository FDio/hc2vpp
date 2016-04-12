package org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.data.impl.rev160411;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import io.fd.honeycomb.v3po.data.ReadableDataTree;
import io.fd.honeycomb.v3po.data.impl.OperationalDataTree;
import javax.annotation.Nonnull;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationalDataTreeModule extends
        org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.data.impl.rev160411.AbstractOperationalDataTreeModule {

    private static final Logger LOG = LoggerFactory.getLogger(OperationalDataTreeModule.class);

    public OperationalDataTreeModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
                                     org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public OperationalDataTreeModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier,
                                     org.opendaylight.controller.config.api.DependencyResolver dependencyResolver,
                                     org.opendaylight.yang.gen.v1.urn.honeycomb.params.xml.ns.yang.data.impl.rev160411.OperationalDataTreeModule oldModule,
                                     java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {
        LOG.debug("OperationalDataTreeModule.createInstance()");
        return new CloseableOperationalDataTree(
                new OperationalDataTree(getSerializerDependency(), getSchemaServiceDependency().getGlobalContext(),
                        getReaderRegistryDependency(), getNetconfMonitoringDomDataBrokerDependency()));
    }

    private static final class CloseableOperationalDataTree implements ReadableDataTree, AutoCloseable {

        private final OperationalDataTree delegate;

        CloseableOperationalDataTree(final OperationalDataTree delegate) {
            this.delegate = delegate;
        }

        @Override
        public void close() throws Exception {
            LOG.debug("CloseableOperationalDataTree.close()");
            // NOP
        }

        @Override
        public CheckedFuture<Optional<NormalizedNode<?, ?>>, ReadFailedException> read(
                @Nonnull final YangInstanceIdentifier path) {
            LOG.trace("CloseableOperationalDataTree.read path={}", path);
            return delegate.read(path);
        }
    }
}
