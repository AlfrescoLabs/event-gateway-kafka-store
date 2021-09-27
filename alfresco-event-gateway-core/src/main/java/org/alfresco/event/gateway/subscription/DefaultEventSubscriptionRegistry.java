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
package org.alfresco.event.gateway.subscription;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link EventSubscriptionRegistry} that uses a {@link java.util.Map} to store the {@link EventSubscription} objects.
 */
public class DefaultEventSubscriptionRegistry implements EventSubscriptionRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventSubscriptionRegistry.class);

    private Map<String, EventSubscription> registry = new HashMap<>();

    @Override
    public void register(String id, EventSubscription eventSubscription) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(eventSubscription);
        LOGGER.debug("Registering the event subscription {} with the id {}", eventSubscription, id);
        registry.put(id, eventSubscription);
        LOGGER.debug("Event subscription successfully registered");
    }

    @Override
    public void deregister(String id) {
        Objects.requireNonNull(id);
        LOGGER.debug("De-registering the event subscription with the id {}", id);
        if (Objects.nonNull(registry.remove(id))) {
            LOGGER.debug("Event subscription successfully de-registered");
        } else {
            LOGGER.debug("Event subscription not existing in the registry");
        }
    }

    @Override
    public EventSubscription getById(String id) {
        Objects.requireNonNull(id);
        LOGGER.debug("Obtaining the event subscription under the id {}", id);
        return registry.get(id);
    }
}
