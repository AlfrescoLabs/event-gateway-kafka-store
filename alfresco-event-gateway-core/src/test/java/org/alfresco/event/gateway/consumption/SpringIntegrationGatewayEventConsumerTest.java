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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import javax.jms.Destination;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistrationBuilder;

/**
 * Unit tests for {@link SpringIntegrationGatewayEventConsumer}.
 */
public class SpringIntegrationGatewayEventConsumerTest extends AbstractUnitTest {

    @InjectMocks
    private SpringIntegrationGatewayEventConsumer springIntegrationGatewayEventConsumer;

    @Mock
    private IntegrationFlowContext mockIntegrationFlowContext;
    @Mock
    private ActiveMQConnectionFactory mockActiveMQConnectionFactory;
    @Mock
    private JsonToRepoEventTransformer mockJsonToRepoEventTransformer;
    @Mock
    private EventRouter mockEventRouter;
    @Mock
    private Destination mockDestination;
    @Mock
    private IntegrationFlowRegistrationBuilder mockIntegrationFlowRegistrationBuilder;
    @Mock
    private IntegrationFlowRegistration mockIntegrationFlowRegistration;

    @BeforeEach
    public void setup() {
        springIntegrationGatewayEventConsumer = new SpringIntegrationGatewayEventConsumer(mockIntegrationFlowContext, mockActiveMQConnectionFactory,
            mockJsonToRepoEventTransformer, mockEventRouter, mockDestination, false);
    }

    @Test
    public void should_registerIntegrationFlow_when_startConsumingEvents() {
        given(mockIntegrationFlowContext.registration(any(IntegrationFlow.class))).willReturn(mockIntegrationFlowRegistrationBuilder);
        given(mockIntegrationFlowRegistrationBuilder.id(any(String.class))).willReturn(mockIntegrationFlowRegistrationBuilder);
        given(mockIntegrationFlowRegistrationBuilder.register()).willReturn(mockIntegrationFlowRegistration);

        springIntegrationGatewayEventConsumer.startConsumingEvents();

        verify(mockIntegrationFlowRegistrationBuilder).register();
    }

    @Test
    public void should_registerIntegrationFlowOnlyOnce_when_startConsumingEventsInvokedSeveralTimes() {
        given(mockIntegrationFlowContext.registration(any(IntegrationFlow.class))).willReturn(mockIntegrationFlowRegistrationBuilder);
        given(mockIntegrationFlowRegistrationBuilder.id(any(String.class))).willReturn(mockIntegrationFlowRegistrationBuilder);
        given(mockIntegrationFlowRegistrationBuilder.register()).willReturn(mockIntegrationFlowRegistration);

        springIntegrationGatewayEventConsumer.startConsumingEvents();
        springIntegrationGatewayEventConsumer.startConsumingEvents();
        springIntegrationGatewayEventConsumer.startConsumingEvents();
        springIntegrationGatewayEventConsumer.startConsumingEvents();

        verify(mockIntegrationFlowRegistrationBuilder).register();
    }
}
