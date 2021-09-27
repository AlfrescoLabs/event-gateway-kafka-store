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
package org.alfresco.event.gateway.subscription.jms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;

import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistrationBuilder;
import org.springframework.messaging.Message;

/**
 * Unit tests for {@link SpringIntegrationJmsSubscriptionPublisher}.
 */
public class SpringIntegrationJmsSubscriptionPublisherTest extends AbstractUnitTest {

    private static final String TEST_FLOW_ID = "test-flow-id";

    @InjectMocks
    private SpringIntegrationJmsSubscriptionPublisher springIntegrationJmsSubscriptionPublisher;

    @Mock
    private IntegrationFlowContext mockIntegrationFlowContext;
    @Mock
    private IntegrationFlowRegistration mockIntegrationFlowRegistration;
    @Mock
    private IntegrationFlowRegistrationBuilder mockIntegrationFlowRegistrationBuilder;
    @Mock
    private MessagingTemplate mockMessagingTemplate;
    @Captor
    private ArgumentCaptor<Message> messageCaptor;

    @BeforeEach
    public void setup() {
        lenient().when(mockIntegrationFlowContext.registration(any(IntegrationFlow.class))).thenReturn(mockIntegrationFlowRegistrationBuilder);
        lenient().when(mockIntegrationFlowRegistrationBuilder.register()).thenReturn(mockIntegrationFlowRegistration);
        lenient().when(mockIntegrationFlowRegistration.getId()).thenReturn(TEST_FLOW_ID);
        lenient().when(mockIntegrationFlowContext.messagingTemplateFor(TEST_FLOW_ID)).thenReturn(mockMessagingTemplate);
    }

    @Test
    public void should_publishAnyEventUsingTheIntegrationFlow_when_aProperEventIsProvided() {
        final RepoEvent<? extends DataAttributes<? extends Resource>> repoEvent = RepoEvent.<EventData<NodeResource>>builder().build();
        springIntegrationJmsSubscriptionPublisher.publishEvent((RepoEvent<DataAttributes<Resource>>) repoEvent);

        verify(mockMessagingTemplate).send(messageCaptor.capture());
        assertThat(messageCaptor.getValue().getPayload()).isEqualTo(repoEvent);
    }

    @Test
    public void should_throwIllegalArgumentException_when_aNullEventIsProvided() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> springIntegrationJmsSubscriptionPublisher.publishEvent(null));
    }

    @Test
    public void should_removeTheSpringIntegrationFlowFromContext_when_releaseInvoked() {
        springIntegrationJmsSubscriptionPublisher.release();

        verify(mockIntegrationFlowContext).remove(TEST_FLOW_ID);
    }
}
