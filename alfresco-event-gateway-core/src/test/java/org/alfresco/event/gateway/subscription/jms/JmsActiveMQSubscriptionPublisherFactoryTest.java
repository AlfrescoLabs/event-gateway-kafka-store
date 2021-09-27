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

import java.util.HashMap;
import java.util.Map;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.BrokerConfig;
import org.alfresco.event.gateway.SubscriptionConfigurationConstants;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.subscription.RepoEventToJsonTransformer;
import org.alfresco.event.gateway.subscription.SubscriptionPublisher;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.alfresco.event.gateway.subscription.jms.destination.ActiveMQDestinationResolver;
import org.alfresco.event.gateway.subscription.jms.destination.JmsDestinationContext;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistrationBuilder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link JmsActiveMQSubscriptionPublisherFactory}.
 */
public class JmsActiveMQSubscriptionPublisherFactoryTest extends AbstractUnitTest {

    private static final String TEST_FLOW_ID = "test-flow-id";
    private static final String TEST_DESTINATION = "test";

    @InjectMocks
    private JmsActiveMQSubscriptionPublisherFactory jmsActiveMQSubscriptionPublisherFactory;

    @Mock
    private IntegrationFlowContext mockIntegrationFlowContext;
    @Mock
    private RepoEventToJsonTransformer mockRepoEventToJsonTransformer;
    @Mock
    private IntegrationFlowRegistrationBuilder mockIntegrationFlowRegistrationBuilder;
    @Mock
    private IntegrationFlowRegistration mockIntegrationFlowRegistration;
    @Mock
    private MessagingTemplate mockMessagingTemplate;
    @Mock
    private ActiveMQDestinationResolver mockActiveMQDestinationResolver;
    @Mock
    private BrokerConfigResolver mockBrokerConfigResolver;

    @BeforeEach
    public void setup() {
        lenient().when(mockIntegrationFlowContext.registration(any(IntegrationFlow.class))).thenReturn(mockIntegrationFlowRegistrationBuilder);
        lenient().when(mockIntegrationFlowRegistrationBuilder.id(any(String.class))).thenReturn(mockIntegrationFlowRegistrationBuilder);
        lenient().when(mockIntegrationFlowRegistrationBuilder.register()).thenReturn(mockIntegrationFlowRegistration);
        lenient().when(mockIntegrationFlowRegistration.getId()).thenReturn(TEST_FLOW_ID);
        lenient().when(mockIntegrationFlowContext.messagingTemplateFor(TEST_FLOW_ID)).thenReturn(mockMessagingTemplate);
        lenient().when(mockActiveMQDestinationResolver.resolveDestination(any(JmsDestinationContext.class))).thenReturn(new ActiveMQTopic(TEST_DESTINATION));
        lenient().when(mockBrokerConfigResolver.resolveBrokerConfig(TEST_DESTINATION)).thenReturn(BrokerConfig.builder().url(TEST_DESTINATION).build());
    }

    @Test
    public void should_getSpringIntegrationJmsSubscriptionPublisher_when_properSubscriptionIsProvided() {
        Subscription subscription = new Subscription();
        Map<String, String> subscriptionConfig = new HashMap<>();
        subscriptionConfig.put(SubscriptionConfigurationConstants.BROKER_ID, TEST_DESTINATION);
        subscriptionConfig.put(SubscriptionConfigurationConstants.DESTINATION, TEST_DESTINATION);
        subscription.setConfig(subscriptionConfig);

        SubscriptionPublisher subscriptionPublisher = jmsActiveMQSubscriptionPublisherFactory.getSubscriptionPublisher(subscription);

        assertThat(ReflectionTestUtils.getField(subscriptionPublisher, "integrationFlowContext")).isEqualTo(mockIntegrationFlowContext);
        assertThat(ReflectionTestUtils.getField(subscriptionPublisher, "integrationFlowRegistration")).isEqualTo(mockIntegrationFlowRegistration);
        assertThat(ReflectionTestUtils.getField(subscriptionPublisher, "messagingTemplate")).isEqualTo(mockMessagingTemplate);
    }

    @Test
    public void should_throwNullPointerException_when_nullSubscriptionIsProvided() {
        Assertions.assertThrows(NullPointerException.class,
            () -> jmsActiveMQSubscriptionPublisherFactory.getSubscriptionPublisher(null));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_nullSubscriptionConfigurationIsProvided() {
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> jmsActiveMQSubscriptionPublisherFactory.getSubscriptionPublisher(new Subscription()));
    }
}
