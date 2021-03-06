/*
 * Copyright (c) 2019 PANTHEON.tech.
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

package io.fd.hc2vpp.policer.write;

import static com.google.common.base.Preconditions.checkNotNull;

import io.fd.hc2vpp.common.translate.util.NamingContext;
import io.fd.hc2vpp.vpp.classifier.context.VppClassifierContextManager;
import io.fd.honeycomb.translate.write.DataValidationFailedException;
import io.fd.honeycomb.translate.write.Validator;
import io.fd.honeycomb.translate.write.WriteContext;
import javax.annotation.Nonnull;
import org.opendaylight.yang.gen.v1.http.fd.io.hc2vpp.yang._interface.policer.rev190527._interface.policer.attributes.Policer;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public class InterfacePolicerValidator implements Validator<Policer> {

    public InterfacePolicerValidator(final NamingContext interfaceContext,
                                     final VppClassifierContextManager classifyTableContext) {
        checkNotNull(interfaceContext, "interfaceContext should not be null");
        checkNotNull(classifyTableContext, "classifyTableContext should not be null");
    }

    @Override
    public void validateWrite(@Nonnull final InstanceIdentifier<Policer> id,
                              @Nonnull final Policer policer,
                              @Nonnull final WriteContext writeContext)
            throws DataValidationFailedException.CreateValidationFailedException {
        //noop for now
    }

    @Override
    public void validateUpdate(@Nonnull final InstanceIdentifier<Policer> id, @Nonnull final Policer dataBefore,
                               @Nonnull final Policer dataAfter, @Nonnull final WriteContext writeContext)
            throws DataValidationFailedException.UpdateValidationFailedException {
        // noop for now
    }

    @Override
    public void validateDelete(@Nonnull final InstanceIdentifier<Policer> id, @Nonnull final Policer dataBefore,
                               @Nonnull final WriteContext writeContext)
            throws DataValidationFailedException.DeleteValidationFailedException {
        //noop for now
    }
}
