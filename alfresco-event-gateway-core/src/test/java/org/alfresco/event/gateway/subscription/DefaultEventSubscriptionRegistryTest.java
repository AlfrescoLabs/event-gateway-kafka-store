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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DefaultEventSubscriptionRegistry}.
 */
public class DefaultEventSubscriptionRegistryTest {

    private static final String TEST_ID = "id";
    private static final EventSubscription TEST_EVENT_SUBSCRIPTION = new EventSubscription() {
    };
    private static final EventSubscription TEST_ANOTHER_EVENT_SUBSCRIPTION = new EventSubscription() {
    };

    private DefaultEventSubscriptionRegistry defaultEventSubscriptionRegistry;


    @BeforeEach
    public void setup() {
        defaultEventSubscriptionRegistry = new DefaultEventSubscriptionRegistry();
    }

    @Test
    public void should_addEventSubscriptionToRegistry_when_registerMethodInvokedWithProperIdAndEventSubscription() {
        defaultEventSubscriptionRegistry.register(TEST_ID, TEST_EVENT_SUBSCRIPTION);

        assertThat(defaultEventSubscriptionRegistry.getById(TEST_ID)).isEqualTo(TEST_EVENT_SUBSCRIPTION);
    }

    @Test
    public void should_updateEventSubscriptionInRegistry_when_registerMethodInvokedWithExistingId() {
        defaultEventSubscriptionRegistry.register(TEST_ID, TEST_EVENT_SUBSCRIPTION);

        defaultEventSubscriptionRegistry.register(TEST_ID, TEST_ANOTHER_EVENT_SUBSCRIPTION);

        assertThat(defaultEventSubscriptionRegistry.getById(TEST_ID)).isEqualTo(TEST_ANOTHER_EVENT_SUBSCRIPTION);
    }

    @Test
    public void should_throwNullPointerException_when_registerMethodInvokedWithNullId() {
        Assertions.assertThrows(NullPointerException.class, () -> defaultEventSubscriptionRegistry.register(null, TEST_EVENT_SUBSCRIPTION));
    }

    @Test
    public void should_throwNullPointerException_when_registerMethodInvokedWithNullEventSubscription() {
        Assertions.assertThrows(NullPointerException.class, () -> defaultEventSubscriptionRegistry.register(TEST_ID, null));
    }

    @Test
    public void should_removeEventSubscriptionFromRegistry_when_deregisterMethodInvokedWithRegisteredId() {
        defaultEventSubscriptionRegistry.register(TEST_ID, TEST_EVENT_SUBSCRIPTION);

        defaultEventSubscriptionRegistry.deregister(TEST_ID);

        assertThat(defaultEventSubscriptionRegistry.getById(TEST_ID)).isNull();
    }

    @Test
    public void should_throwNullPointerException_when_deregisterMethodInvokedWithNullId() {
        Assertions.assertThrows(NullPointerException.class, () -> defaultEventSubscriptionRegistry.deregister(null));
    }

    @Test
    public void should_returnEventSubscriptionFromRegistry_when_getByIdMethodInvokedWithRegisteredId() {
        defaultEventSubscriptionRegistry.register(TEST_ID, TEST_EVENT_SUBSCRIPTION);

        EventSubscription eventSubscription = defaultEventSubscriptionRegistry.getById(TEST_ID);

        assertThat(eventSubscription).isEqualTo(TEST_EVENT_SUBSCRIPTION);
    }

    @Test
    public void should_returnNull_when_getByIdMethodInvokedWithNotRegisteredId() {
        defaultEventSubscriptionRegistry.register(TEST_ID, TEST_EVENT_SUBSCRIPTION);

        EventSubscription eventSubscription = defaultEventSubscriptionRegistry.getById("another");

        assertThat(eventSubscription).isNull();
    }

    @Test
    public void should_throwNullPointerException_when_getByIdMethodInvokedWithNullId() {
        Assertions.assertThrows(NullPointerException.class, () -> defaultEventSubscriptionRegistry.getById(null));
    }
}
