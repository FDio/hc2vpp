/*
 * Copyright (c) 2016 Cisco and its affiliates.
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

package io.fd.hc2vpp.vppioam.impl.util;

import com.google.common.base.Preconditions;
import io.fd.jvpp.ioamexport.future.FutureJVppIoamexport;
import javax.annotation.Nonnull;

public abstract class FutureJVppIoamexportCustomizer {

    private final FutureJVppIoamexport futureJVppIoamexport;

    public FutureJVppIoamexportCustomizer (@Nonnull final FutureJVppIoamexport futureJVppIoamexport) {
        this.futureJVppIoamexport = Preconditions.checkNotNull(futureJVppIoamexport,
                "futureJVppIoamexport should not be null");
    }

    /**
     * Get Ioam POT Api reference
     *
     * @return Ioam POT Api reference
     */
    public FutureJVppIoamexport getFutureJVppIoamexport() {
        return futureJVppIoamexport;
    }

}
