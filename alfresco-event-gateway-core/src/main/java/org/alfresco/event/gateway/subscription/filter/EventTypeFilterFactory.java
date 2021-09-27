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
package org.alfresco.event.gateway.subscription.filter;

import static org.alfresco.event.gateway.subscription.filter.EventFilterConfigurationConstants.EVENT_TYPES;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.alfresco.event.gateway.entity.Filter;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.subscription.exception.FilterConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link EventFilterFactory} that creates {@link EventTypeFilter} objects with the configuration provided in the {@link Filter} object.
 */
public class EventTypeFilterFactory implements EventFilterFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventTypeFilterFactory.class);

    @Override
    public EventFilter getEventFilter(Subscription subscription, Filter filter) {
        Objects.requireNonNull(subscription);
        Objects.requireNonNull(filter);
        LOGGER.info("Building event type filter from subscription {} and filter {}", subscription, filter);
        List<String> eventTypes = getEventTypes(filter);
        if (eventTypes.isEmpty()) {
            throw new FilterConfigurationException("Event type filter creation requested with no event types");
        }
        Iterator<String> listIterator = eventTypes.stream().skip(1).iterator();
        EventFilter eventFilter = EventTypeFilter.of(eventTypes.get(0));
        while(listIterator.hasNext()) {
            eventFilter = eventFilter.or(EventTypeFilter.of(listIterator.next()));
        }
        return eventFilter;
    }

    private List<String> getEventTypes(Filter filter) {
        List<String> eventTypes = new ArrayList<>();
        final Map<String, String> config = filter.getConfig();
        if (Objects.nonNull(config) && Objects.nonNull(config.get(EVENT_TYPES))) {
            eventTypes = Arrays.asList(config.get(EVENT_TYPES).split("\\s*,\\s*"));
        }
        return eventTypes;
    }
}
