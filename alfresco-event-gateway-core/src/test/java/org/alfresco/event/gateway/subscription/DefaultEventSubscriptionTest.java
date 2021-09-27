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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import org.alfresco.event.gateway.subscription.filter.EventFilter;
import org.alfresco.event.gateway.subscription.transformation.EventTransformation;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link DefaultEventSubscription}.
 */
@ExtendWith(MockitoExtension.class)
public class DefaultEventSubscriptionTest {

    private DefaultEventSubscription defaultEventSubscription;

    @Mock
    private SubscriptionPublisher mockSubscriptionPublisher;
    @Mock
    private EventTransformation mockEventTransformation;

    @Test
    public void should_publishTheEvent_when_noFiltersAreConfigured() {
        defaultEventSubscription = new DefaultEventSubscription(mockSubscriptionPublisher, Collections.emptyList(), List.of(mockEventTransformation));
        final RepoEvent<? extends DataAttributes<? extends Resource>> repoEvent = RepoEvent.<EventData<NodeResource>>builder()
            .build();
        given(mockEventTransformation.transform((RepoEvent<DataAttributes<Resource>>) repoEvent)).willReturn((RepoEvent<DataAttributes<Resource>>) repoEvent);
        defaultEventSubscription.consumeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);

        verify(mockSubscriptionPublisher).publishEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);
    }

    @Test
    public void should_publishTheEvent_when_passingFiltersAreConfigured() {
        EventFilter passingEventFilter = event -> Boolean.TRUE;
        defaultEventSubscription = new DefaultEventSubscription(mockSubscriptionPublisher, List.of(passingEventFilter), List.of(mockEventTransformation));
        final RepoEvent<? extends DataAttributes<? extends Resource>> repoEvent = RepoEvent.<EventData<NodeResource>>builder()
            .build();
        given(mockEventTransformation.transform((RepoEvent<DataAttributes<Resource>>) repoEvent)).willReturn((RepoEvent<DataAttributes<Resource>>) repoEvent);
        defaultEventSubscription.consumeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);

        verify(mockSubscriptionPublisher).publishEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);
    }

    @Test
    public void should_notPublishTheEvent_when_atLeastOneFailingFilterIsConfigured() {
        EventFilter passingEventFilter = event -> Boolean.TRUE;
        EventFilter failingEventFilter = event -> Boolean.FALSE;
        defaultEventSubscription = new DefaultEventSubscription(mockSubscriptionPublisher, List.of(passingEventFilter, failingEventFilter),
            List.of(mockEventTransformation));
        final RepoEvent<? extends DataAttributes<? extends Resource>> repoEvent = RepoEvent.<EventData<NodeResource>>builder()
            .build();
        defaultEventSubscription.consumeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);

        verify(mockSubscriptionPublisher, never()).publishEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);
    }

    @Test
    public void should_releasePublisherResources_when_releaseIsInvoked() {
        defaultEventSubscription = new DefaultEventSubscription(mockSubscriptionPublisher, Collections.EMPTY_LIST, Collections.EMPTY_LIST);

        defaultEventSubscription.release();

        verify(mockSubscriptionPublisher).release();
    }
}
