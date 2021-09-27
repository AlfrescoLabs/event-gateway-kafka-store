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

import javax.jms.Destination;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.BrokerConfig;
import org.alfresco.event.gateway.subscription.RepoEventToJsonTransformer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistrationBuilder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link SpringIntegrationJmsSubscriptionPublisherBuilder}.
 */
public class SpringIntegrationJmsSubscriptionPublisherBuilderTest extends AbstractUnitTest {

    private static final String TEST_FLOW_ID = "test-flow-id";

    @Mock
    private IntegrationFlowContext mockIntegrationFlowContext;
    @Mock
    private Destination mockDestination;
    @Mock
    private RepoEventToJsonTransformer mockRepoEventToJsonTransformer;
    @Mock
    private BrokerConfig mockBrokerConfig;
    @Mock
    private IntegrationFlowRegistrationBuilder mockIntegrationFlowRegistrationBuilder;
    @Mock
    private IntegrationFlowRegistration mockIntegrationFlowRegistration;
    @Mock
    private MessagingTemplate mockMessagingTemplate;

    @BeforeEach
    public void setup() {
        lenient().when(mockIntegrationFlowContext.registration(any(IntegrationFlow.class))).thenReturn(mockIntegrationFlowRegistrationBuilder);
        lenient().when(mockIntegrationFlowRegistrationBuilder.id(any(String.class))).thenReturn(mockIntegrationFlowRegistrationBuilder);
        lenient().when(mockIntegrationFlowRegistrationBuilder.register()).thenReturn(mockIntegrationFlowRegistration);
        lenient().when(mockIntegrationFlowRegistration.getId()).thenReturn(TEST_FLOW_ID);
        lenient().when(mockIntegrationFlowContext.messagingTemplateFor(TEST_FLOW_ID)).thenReturn(mockMessagingTemplate);
    }

    @Test
    public void should_buildAProperJmsSubscriptionPublisherInstance_when_mandatoryFieldsAreProvided() {
        SpringIntegrationJmsSubscriptionPublisher springIntegrationJmsSubscriptionPublisher = SpringIntegrationJmsSubscriptionPublisherBuilder.getInstance()
            .integrationFlowContext(mockIntegrationFlowContext)
            .destination(mockDestination)
            .repoEventToJsonTransformer(mockRepoEventToJsonTransformer)
            .brokerConfig(mockBrokerConfig)
            .build();

        assertThat(ReflectionTestUtils.getField(springIntegrationJmsSubscriptionPublisher, "integrationFlowContext")).isEqualTo(mockIntegrationFlowContext);
        assertThat(ReflectionTestUtils.getField(springIntegrationJmsSubscriptionPublisher, "integrationFlowRegistration"))
            .isEqualTo(mockIntegrationFlowRegistration);
        assertThat(ReflectionTestUtils.getField(springIntegrationJmsSubscriptionPublisher, "messagingTemplate")).isEqualTo(mockMessagingTemplate);
    }

    @Test
    public void should_throwIllegalArgumentException_when_nullIntegrationFlowContextIsProvided() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> SpringIntegrationJmsSubscriptionPublisherBuilder.getInstance()
                .destination(mockDestination)
                .repoEventToJsonTransformer(mockRepoEventToJsonTransformer)
                .brokerConfig(mockBrokerConfig)
                .build());
    }

    @Test
    public void should_throwIllegalArgumentException_when_nullBrokerConfigIsProvided() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> SpringIntegrationJmsSubscriptionPublisherBuilder.getInstance()
                .integrationFlowContext(mockIntegrationFlowContext)
                .destination(mockDestination)
                .repoEventToJsonTransformer(mockRepoEventToJsonTransformer)
                .build());
    }

    @Test
    public void should_throwIllegalArgumentException_when_nullDestinationIsProvided() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> SpringIntegrationJmsSubscriptionPublisherBuilder.getInstance()
                .integrationFlowContext(mockIntegrationFlowContext)
                .repoEventToJsonTransformer(mockRepoEventToJsonTransformer)
                .brokerConfig(mockBrokerConfig)
                .build());
    }

    @Test
    public void should_throwIllegalArgumentException_when_nullRepoEventToJsonTransformerIsProvided() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> SpringIntegrationJmsSubscriptionPublisherBuilder.getInstance()
                .integrationFlowContext(mockIntegrationFlowContext)
                .destination(mockDestination)
                .brokerConfig(mockBrokerConfig)
                .build());
    }
}
