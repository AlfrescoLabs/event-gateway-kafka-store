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
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.entity.Filter;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.subscription.exception.UnsupportedFilterTypeException;
import org.alfresco.event.gateway.subscription.exception.UnsupportedSubscriptionTypeException;
import org.alfresco.event.gateway.subscription.filter.EventFilter;
import org.alfresco.event.gateway.subscription.filter.EventFilterFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link DefaultEventSubscriptionFactory}.
 */
public class DefaultEventSubscriptionFactoryTest extends AbstractUnitTest {

    private static final String TEST_SUBSCRIPTION_TYPE = "test-subscription";
    private static final String TEST_FILTER_TYPE = "test-filter";

    @InjectMocks
    private DefaultEventSubscriptionFactory defaultEventSubscriptionFactory;

    @Mock
    private SubscriptionPublisherFactory mockSubscriptionPublisherFactory;
    @Mock
    private SubscriptionPublisher mockSubscriptionPublisher;
    @Mock
    private EventFilterFactory mockEventFilterFactory;
    @Mock
    private EventFilter mockEventFilter;

    @BeforeEach
    public void setup() {
        Map<String, SubscriptionPublisherFactory> subscriptionPublisherFactoryMap = new HashMap<>();
        subscriptionPublisherFactoryMap.put(TEST_SUBSCRIPTION_TYPE, mockSubscriptionPublisherFactory);
        Map<String, EventFilterFactory> eventFilterFactoryMap = new HashMap<>();
        eventFilterFactoryMap.put(TEST_FILTER_TYPE, mockEventFilterFactory);
        defaultEventSubscriptionFactory = new DefaultEventSubscriptionFactory(subscriptionPublisherFactoryMap, eventFilterFactoryMap, Collections.EMPTY_LIST);
    }

    @Test
    public void should_getDefaultEventSubscription_when_properSubscriptionIsProvided() {
        Subscription subscription = new Subscription();
        subscription.setType(TEST_SUBSCRIPTION_TYPE);
        Filter filter = new Filter();
        filter.setType(TEST_FILTER_TYPE);
        subscription.setFilters(Stream.of(filter).collect(Collectors.toList()));
        given(mockSubscriptionPublisherFactory.getSubscriptionPublisher(subscription)).willReturn(mockSubscriptionPublisher);
        given(mockEventFilterFactory.getEventFilter(subscription, filter)).willReturn(mockEventFilter);

        EventSubscription eventSubscription = defaultEventSubscriptionFactory.getEventSubscription(subscription);

        assertThat(ReflectionTestUtils.getField(eventSubscription, "subscriptionPublisher")).isEqualTo(mockSubscriptionPublisher);
        assertThat(eventSubscription.getEventFilters()).containsSequence(mockEventFilter);
    }

    @Test
    public void should_throwNullPointerException_when_nullSubscriptionIsProvided() {
        Assertions.assertThrows(NullPointerException.class,
            () -> defaultEventSubscriptionFactory.getEventSubscription(null));
    }

    @Test
    public void should_throwUnsupportedSubscriptionTypeException_when_unknownSubscriptionTypeIsProvided() {
        Subscription subscription = new Subscription();
        subscription.setType("unknown");
        Assertions.assertThrows(UnsupportedSubscriptionTypeException.class,
            () -> defaultEventSubscriptionFactory.getEventSubscription(subscription));
    }

    @Test
    public void should_throwUnsupportedFilterTypeException_when_unknownFilterTypeIsProvided() {
        Subscription subscription = new Subscription();
        subscription.setType(TEST_SUBSCRIPTION_TYPE);
        Filter filter = new Filter();
        filter.setType("unknown");
        subscription.setFilters(Stream.of(filter).collect(Collectors.toList()));
        Assertions.assertThrows(UnsupportedFilterTypeException.class,
            () -> defaultEventSubscriptionFactory.getEventSubscription(subscription));
    }
}
