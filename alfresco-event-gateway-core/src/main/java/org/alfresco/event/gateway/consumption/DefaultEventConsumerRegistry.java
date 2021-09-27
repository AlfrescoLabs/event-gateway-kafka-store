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
package org.alfresco.event.gateway.consumption;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link EventConsumerRegistry} that uses a {@link Set} to store the {@link EventConsumer} objects.
 */
public class DefaultEventConsumerRegistry implements EventConsumerRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventConsumerRegistry.class);

    private Set<EventConsumer> registry = ConcurrentHashMap.newKeySet();

    @Override
    public void register(EventConsumer eventConsumer) {
        Objects.requireNonNull(eventConsumer);
        LOGGER.debug("Registering the event consumer {}", eventConsumer);
        if (registry.add(eventConsumer)) {
            LOGGER.debug("Event consumer successfully registered");
        } else {
            LOGGER.debug("Event consumer already registered");
        }
    }

    @Override
    public void deregister(EventConsumer eventConsumer) {
        Objects.requireNonNull(eventConsumer);
        LOGGER.debug("Registering the event consumer {}", eventConsumer);
        if (registry.remove(eventConsumer)) {
            LOGGER.debug("Event consumer successfully de-registered");
        } else {
            LOGGER.debug("Event consumer not existing in the registry");
        }
    }

    @Override
    public Stream<EventConsumer> getAll() {
        LOGGER.debug("Providing all the registered event consumers");
        return registry.stream();
    }
}
