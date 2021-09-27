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

import java.util.List;
import org.alfresco.event.gateway.consumption.EventConsumer;
import org.alfresco.event.gateway.subscription.filter.EventFilter;
import org.alfresco.event.gateway.subscription.transformation.EventTransformation;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of {@link EventSubscription} that re-publishes the event consumed to the corresponding {@link SubscriptionPublisher} if it passes all
 * the configured filters.
 */
public class DefaultEventSubscription implements EventSubscription, EventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEventSubscription.class);

    private final SubscriptionPublisher subscriptionPublisher;
    private final List<EventFilter> eventFilters;
    private final List<EventTransformation> eventTransformations;

    /**
     * Constructor.
     *
     * @param subscriptionPublisher given {@link SubscriptionPublisher}
     * @param eventFilters          given {@link List} of {@link EventFilter}
     * @param eventTransformations  given {@link List} of {@link EventTransformation}
     */
    public DefaultEventSubscription(final SubscriptionPublisher subscriptionPublisher, final List<EventFilter> eventFilters,
        final List<EventTransformation> eventTransformations) {
        this.subscriptionPublisher = subscriptionPublisher;
        this.eventFilters = eventFilters;
        this.eventTransformations = eventTransformations;
    }

    @Override
    public void consumeEvent(RepoEvent<DataAttributes<Resource>> event) {
        LOGGER.debug("Consuming event {}", event);
        // Publish the event only if all filters are passing (or there are no filters for the subscription)
        if (filter(event)) {
            RepoEvent<DataAttributes<Resource>> transformedEvent = transform(event);
            publish(transformedEvent);
        }
    }

    private boolean filter(RepoEvent<DataAttributes<Resource>> event) {
        return eventFilters.stream().allMatch(eventFilter -> eventFilter.test(event));
    }

    private RepoEvent<DataAttributes<Resource>> transform(RepoEvent<DataAttributes<Resource>> event) {
        for (EventTransformation eventTransformation : eventTransformations) {
            event = eventTransformation.transform(event);
        }
        return event;
    }

    private void publish(RepoEvent<DataAttributes<Resource>> event) {
        subscriptionPublisher.publishEvent(event);
    }

    @Override
    public List<EventFilter> getEventFilters() {
        return eventFilters;
    }

    @Override
    public void release() {
        LOGGER.debug("Releasing resources from event subscription");
        subscriptionPublisher.release();
    }
}
