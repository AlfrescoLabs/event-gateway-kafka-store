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

import static org.mockito.Mockito.verify;

import org.alfresco.event.gateway.AbstractUnitTest;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Unit tests for {@link EventConsumptionStarter}.
 */
public class EventConsumptionStarterTest extends AbstractUnitTest {

    @InjectMocks
    private EventConsumptionStarter eventConsumptionStarter;

    @Mock
    private GatewayEventConsumer mockGatewayEventConsumer;
    @Mock
    private ApplicationContext mockApplicationContext;

    @Test
    public void should_invokeGatewayEventConsumerStartup_when_applicationEventIsReceived() {
        eventConsumptionStarter.onApplicationEvent(new ContextRefreshedEvent(mockApplicationContext));

        verify(mockGatewayEventConsumer).startConsumingEvents();
    }
}
