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
import static org.assertj.core.api.Assertions.assertThat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.alfresco.event.gateway.entity.Filter;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.subscription.exception.FilterConfigurationException;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EventTypeFilterFactory}.
 */
public class EventTypeFilterFactoryTest {

    private static final String EVENT_TYPE = "event-type";
    private static final String NODE_CREATED = "node.Created";
    private static final String NODE_UPDATED = "node.Updated";


    private EventTypeFilterFactory eventTypeFilterFactory;

    @BeforeEach
    public void setUp() {
        eventTypeFilterFactory = new EventTypeFilterFactory();
    }

    @Test
    public void should_createConfiguredEventTypeFilter_when_properlyFilterConfigurationIsProvided() {
        Subscription subscription = new Subscription();
        Filter filter = new Filter();
        filter.setType(EVENT_TYPE);
        Map<String, String> config = new HashMap<>();
        config.put(EVENT_TYPES, NODE_CREATED + "," + NODE_UPDATED);
        filter.setConfig(config);
        subscription.setFilters(List.of(filter));
        final RepoEvent<? extends DataAttributes<? extends Resource>> repoEvent = RepoEvent.<EventData<NodeResource>>builder()
                .setType(NODE_CREATED)
                .build();

        EventFilter eventFilter = eventTypeFilterFactory.getEventFilter(subscription, filter);

        assertThat(eventFilter).isNotNull();
        assertThat(eventFilter.test((RepoEvent<DataAttributes<Resource>>) repoEvent)).isTrue();
    }

    @Test
    public void should_throwNullPointerException_when_nullSubscriptionIsProvided() {
        Assertions.assertThrows(NullPointerException.class,
                () -> eventTypeFilterFactory.getEventFilter(null, new Filter()));
    }

    @Test
    public void should_throwNullPointerException_when_nullFilterIsProvided() {
        Assertions.assertThrows(NullPointerException.class,
                () -> eventTypeFilterFactory.getEventFilter(new Subscription(), null));
    }

    @Test
    public void should_throwFilterConfigurationException_when_emptyFilterConfigIsProvided() {
        Assertions.assertThrows(FilterConfigurationException.class,
                () -> eventTypeFilterFactory.getEventFilter(new Subscription(), new Filter()));
    }

}
