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

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.concurrent.Executor;
import java.util.stream.Stream;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;

/**
 * Unit tests for {@link BroadcastEventRouter}.
 */
public class BroadcastEventRouterTest extends AbstractUnitTest {

    private BroadcastEventRouter broadcastEventRouter;

    @Mock
    private EventConsumerRegistry mockEventConsumerRegistry;
    @Mock
    private EventConsumer mockEventConsumer;
    @Mock
    private EventConsumer mockEventConsumer2;

    @BeforeEach
    public void setup() {
        broadcastEventRouter = new BroadcastEventRouter(mockEventConsumerRegistry, new TestCurrentThreadExecutor());
    }

    @Test
    public void should_invokeEventConsumption_when_registryProvidesConsumer() {
        given(mockEventConsumerRegistry.getAll()).willReturn(Stream.of(mockEventConsumer));

        final RepoEvent<? extends DataAttributes<? extends Resource>> repoEvent = RepoEvent.<EventData<NodeResource>>builder().build();
        broadcastEventRouter.routeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);

        verify(mockEventConsumer).consumeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);
    }

    @Test
    public void should_continueInvokingEventConsumption_when_anyConsumerFailsItsConsumption() {
        given(mockEventConsumerRegistry.getAll()).willReturn(Stream.of(mockEventConsumer, mockEventConsumer2));
        final RepoEvent<? extends DataAttributes<? extends Resource>> repoEvent = RepoEvent.<EventData<NodeResource>>builder().build();
        BDDMockito.willThrow(IllegalArgumentException.class).given(mockEventConsumer).consumeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);

        broadcastEventRouter.routeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);

        verify(mockEventConsumer2).consumeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);
    }

    @Test
    public void should_notInvokeAnyEventConsumption_when_registryProvidesEmptyStream() {
        given(mockEventConsumerRegistry.getAll()).willReturn(Stream.empty());

        final RepoEvent<? extends DataAttributes<? extends Resource>> repoEvent = RepoEvent.<EventData<NodeResource>>builder().build();
        broadcastEventRouter.routeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);

        verify(mockEventConsumer, never()).consumeEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);
    }

    private static class TestCurrentThreadExecutor implements Executor {

        public void execute(Runnable r) {
            r.run();
        }
    }
}
