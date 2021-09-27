/*
 * Copyright 2021-2021 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.alfresco.event.gateway.subscription.jms;

import org.alfresco.event.gateway.BrokerConfig;

/**
 * The broker config resolver component is in charge of providing the proper broker configuration (i.e. url, username, password) from a broker configuration
 * identifier. This way the sensible broker configuration information doesn't need to be publicly exchanged from the client to the event gateway.
 */
@FunctionalInterface
public interface BrokerConfigResolver {

    /**
     * Resolve a {@link BrokerConfig} from its corresponding identifier.
     *
     * @param brokerConfigId the broken configuration identifier
     * @return the resolved broker configuration
     */
    BrokerConfig resolveBrokerConfig(String brokerConfigId);
}
