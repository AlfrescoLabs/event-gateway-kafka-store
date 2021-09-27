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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.alfresco.event.gateway.entity.Filter;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.subscription.exception.UnsupportedFilterTypeException;
import org.alfresco.event.gateway.subscription.exception.UnsupportedSubscriptionTypeException;
import org.alfresco.event.gateway.subscription.filter.EventFilter;
import org.alfresco.event.gateway.subscription.filter.EventFilterFactory;
import org.alfresco.event.gateway.subscription.transformation.EventTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link EventSubscriptionFactory} that produces {@link DefaultEventSubscription} objects.
 */
public class DefaultEventSubscriptionFactory implements EventSubscriptionFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventSubscriptionFactory.class);

    private final Map<String, SubscriptionPublisherFactory> subscriptionPublisherFactoryMap;
    private final Map<String, EventFilterFactory> eventFilterFactoryMap;
    private final List<EventTransformation> eventTransformations;

    /**
     * Constructor.
     *
     * @param subscriptionPublisherFactoryMap given {@link Map} of subscription types and subscription publisher factories
     * @param eventFilterFactoryMap           given {@link Map} of subscription types and event filter factories
     * @param eventTransformations            given {@link List} of {@link EventTransformation}
     */
    public DefaultEventSubscriptionFactory(final Map<String, SubscriptionPublisherFactory> subscriptionPublisherFactoryMap,
        final Map<String, EventFilterFactory> eventFilterFactoryMap, final List<EventTransformation> eventTransformations) {
        this.subscriptionPublisherFactoryMap = subscriptionPublisherFactoryMap;
        this.eventFilterFactoryMap = eventFilterFactoryMap;
        this.eventTransformations = eventTransformations;
    }

    @Override
    public EventSubscription getEventSubscription(final Subscription subscription) {
        Objects.requireNonNull(subscription);
        LOGGER.debug("Building event subscription from subscription with id {}", subscription.getId());
        return new DefaultEventSubscription(getSubscriptionPublisher(subscription), getEventFilters(subscription), eventTransformations);
    }

    private SubscriptionPublisher getSubscriptionPublisher(final Subscription subscription) {
        String subscriptionType = subscription.getType();
        LOGGER.debug("Building subscription publisher from subscription type {}", subscriptionType);

        SubscriptionPublisherFactory subscriptionPublisherFactory = subscriptionPublisherFactoryMap.get(subscriptionType);
        if (Objects.isNull(subscriptionPublisherFactory)) {
            throw new UnsupportedSubscriptionTypeException(subscriptionType, String.format("Subscription type %s is not supported", subscriptionType));
        }

        return subscriptionPublisherFactory.getSubscriptionPublisher(subscription);
    }

    private List<EventFilter> getEventFilters(final Subscription subscription) {
        List<EventFilter> eventFilters = new ArrayList<>();
        subscription.getFilters().stream()
            .map(filter -> processFilter(subscription, filter))
            .forEach(eventFilters::add);
        return eventFilters;
    }

    private EventFilter processFilter(final Subscription subscription, final Filter filter) {
        String filterType = filter.getType();
        EventFilterFactory eventFilterFactory = eventFilterFactoryMap.get(filterType);
        if (Objects.isNull(eventFilterFactory)) {
            throw new UnsupportedFilterTypeException(filterType, String.format("Filter type %s is not supported", filterType));
        }
        return eventFilterFactory.getEventFilter(subscription, filter);
    }
}
