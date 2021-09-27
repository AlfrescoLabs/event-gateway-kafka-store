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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DefaultEventConsumerRegistry}.
 */
public class DefaultEventConsumerRegistryTest {

    private DefaultEventConsumerRegistry defaultEventConsumerRegistry;

    private final EventConsumer mockEventConsumer = RepoEvent::getData;

    @BeforeEach
    public void setup() {
        defaultEventConsumerRegistry = new DefaultEventConsumerRegistry();
    }

    @Test
    public void should_addEventConsumerToRegistry_when_registerMethodInvokedWithNotRegisteredEventConsumer() {
        long numberOfConsumersBefore = defaultEventConsumerRegistry.getAll().count();

        defaultEventConsumerRegistry.register(mockEventConsumer);
        long numberOfConsumersAfter = defaultEventConsumerRegistry.getAll().count();

        assertThat(numberOfConsumersAfter).isEqualTo(numberOfConsumersBefore + 1);
    }

    @Test
    public void should_notAddEventConsumerToRegistry_when_registerMethodInvokedWithAlreadyRegisteredEventConsumer() {
        defaultEventConsumerRegistry.register(mockEventConsumer);
        long numberOfConsumersBefore = defaultEventConsumerRegistry.getAll().count();

        defaultEventConsumerRegistry.register(mockEventConsumer);
        long numberOfConsumersAfter = defaultEventConsumerRegistry.getAll().count();

        assertThat(numberOfConsumersAfter).isEqualTo(numberOfConsumersBefore);
    }

    @Test
    public void should_throwNullPointerException_when_registerMethodInvokedWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> defaultEventConsumerRegistry.register(null));
    }

    @Test
    public void should_removeEventConsumerFromRegistry_when_deregisterMethodInvokedWithRegisteredEventConsumer() {
        defaultEventConsumerRegistry.register(mockEventConsumer);
        long numberOfConsumersBefore = defaultEventConsumerRegistry.getAll().count();

        defaultEventConsumerRegistry.deregister(mockEventConsumer);
        long numberOfConsumersAfter = defaultEventConsumerRegistry.getAll().count();

        assertThat(numberOfConsumersAfter).isEqualTo(numberOfConsumersBefore - 1);
    }

    @Test
    public void should_notRemoveEventConsumerFromRegistry_when_deregisterMethodInvokedWithNotRegisteredEventConsumer() {
        defaultEventConsumerRegistry.register(mockEventConsumer);
        long numberOfConsumersBefore = defaultEventConsumerRegistry.getAll().count();

        defaultEventConsumerRegistry.deregister(RepoEvent::getId);
        long numberOfConsumersAfter = defaultEventConsumerRegistry.getAll().count();

        assertThat(numberOfConsumersAfter).isEqualTo(numberOfConsumersBefore);
    }

    @Test
    public void should_throwNullPointerException_when_deregisterMethodInvokedWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> defaultEventConsumerRegistry.deregister(null));
    }

    @Test
    public void should_returnAStreamWithAllRegisteredEventConsumer_when_getAllMethodIsInvoked() {
        defaultEventConsumerRegistry.register(mockEventConsumer);

        Stream<EventConsumer> allConsumers = defaultEventConsumerRegistry.getAll();

        Set<EventConsumer> allConsumersSet = allConsumers.collect(Collectors.toSet());
        assertThat(allConsumersSet).containsOnly(mockEventConsumer);
    }
}
