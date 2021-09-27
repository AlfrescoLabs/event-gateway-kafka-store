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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.alfresco.event.gateway.AbstractIT;
import org.alfresco.event.gateway.consumption.util.TestConsumptionUtils;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Integration tests for the consumption module.
 */
public class ConsumptionIT extends AbstractIT {

    @Autowired
    private TestConsumptionUtils testConsumptionUtils;
    @Autowired
    private EventConsumerRegistry eventConsumerRegistry;

    @Mock
    private EventConsumer mockEventConsumer;

    @BeforeEach
    public void setup() {
        eventConsumerRegistry.register(mockEventConsumer);
    }

    @Test
    void should_invokeEveryRegisteredEventConsumer_when_repoEventIsReceivedFromTheMainInputDestination() {
        final RepoEvent<? extends DataAttributes<? extends Resource>> simpleRepoEvent = RepoEvent.<EventData<NodeResource>>builder().build();

        testConsumptionUtils.sendRepoEventToMainInput(simpleRepoEvent);

        verify(mockEventConsumer, times(0)).consumeEvent((RepoEvent<DataAttributes<Resource>>) simpleRepoEvent);
    }
}
