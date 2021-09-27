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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.consumption.handling.UserDeletionHandler;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * Unit tests fir {@link UserDeletionEventConsumer}.
 */
public class UserDeletionEventConsumerTest extends AbstractUnitTest {

    private static final String TEST_USER = "user";
    private static final String PERSON_TYPE = "cm:person";
    private static final String USERNAME_PROPERTY = "cm:userName";

    private UserDeletionEventConsumer userDeletionEventConsumer;

    @Mock
    private EventConsumerRegistry mockEventConsumerRegistry;
    @Mock
    private UserDeletionHandler mockUserDeletionHandler;

    @BeforeEach
    public void setup() {
        userDeletionEventConsumer = new UserDeletionEventConsumer(mockEventConsumerRegistry, Set.of(mockUserDeletionHandler));
    }

    @Test
    public void should_processEvent_when_userDeletionEventIsReceived() {
        final HashMap<String, Serializable> properties = new HashMap<>();
        properties.put(USERNAME_PROPERTY, TEST_USER);
        final NodeResource nodeResource = NodeResource.builder()
            .setNodeType(PERSON_TYPE)
            .setProperties(properties)
            .build();
        final EventData<NodeResource> eventData = EventData.<NodeResource>builder()
            .setResource(nodeResource)
            .build();
        final RepoEvent<? extends DataAttributes<? extends Resource>> repoEvent = RepoEvent.<EventData<NodeResource>>builder()
            .setType(EventType.NODE_DELETED.getType())
            .setData(eventData)
            .build();

        userDeletionEventConsumer.consumeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);

        verify(mockUserDeletionHandler).userDeleted(TEST_USER);
    }

    @Test
    public void should_notProcessEvent_when_notUserDeletionEventReceived() {
        final RepoEvent<? extends DataAttributes<? extends Resource>> repoEvent = RepoEvent.<EventData<NodeResource>>builder()
            .build();

        userDeletionEventConsumer.consumeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);

        verify(mockUserDeletionHandler, never()).userDeleted(any());
    }
}
