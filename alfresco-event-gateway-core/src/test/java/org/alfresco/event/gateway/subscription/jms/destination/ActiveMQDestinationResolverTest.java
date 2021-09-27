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
package org.alfresco.event.gateway.subscription.jms.destination;

import static org.assertj.core.api.Assertions.assertThat;

import javax.jms.Destination;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.bootstrapping.SystemBootstrapChecker;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for {@link ActiveMQDestinationResolver}.
 */
public class ActiveMQDestinationResolverTest extends AbstractUnitTest {

    private static final String TEST_DESTINATION = "test";
    private static final String TEST_TOPIC_DESTINATION = "topic:" + TEST_DESTINATION;
    private static final String TEST_QUEUE_DESTINATION = "queue:" + TEST_DESTINATION;
    private static final String TEST_UNKNOWN_DESTINATION = "unknown:" + TEST_DESTINATION;

    @InjectMocks
    private ActiveMQDestinationResolver activeMQDestinationResolver;

    @Mock
    private JmsDestinationValidator mockJmsDestinationValidator;
    @Mock
    private SystemBootstrapChecker mockSystemBootstrapChecker;

    @Test
    public void should_resolveActiveMQTopic_when_topicTypedDestinationStringIsProvided() {
        Destination destination = activeMQDestinationResolver.resolveDestination(new JmsDestinationContext(TEST_TOPIC_DESTINATION));

        assertThat(destination).isEqualTo(new ActiveMQTopic(TEST_DESTINATION));
    }

    @Test
    public void should_resolveActiveMQQueue_when_queueTypedDestinationStringIsProvided() {
        Destination destination = activeMQDestinationResolver.resolveDestination(new JmsDestinationContext(TEST_QUEUE_DESTINATION));

        assertThat(destination).isEqualTo(new ActiveMQQueue(TEST_DESTINATION));
    }

    @Test
    public void should_resolveActiveMQTopic_when_notTypedDestinationStringIsProvided() {
        Destination destination = activeMQDestinationResolver.resolveDestination(new JmsDestinationContext(TEST_DESTINATION));

        assertThat(destination).isEqualTo(new ActiveMQTopic(TEST_DESTINATION));
    }

    @Test
    public void should_resolveActiveMQTopic_when_notRecognisedTypedDestinationStringIsProvided() {
        Destination destination = activeMQDestinationResolver.resolveDestination(new JmsDestinationContext(TEST_UNKNOWN_DESTINATION));

        assertThat(destination).isEqualTo(new ActiveMQTopic(TEST_UNKNOWN_DESTINATION));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_nullDestinationStringIsProvided() {
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> activeMQDestinationResolver.resolveDestination(new JmsDestinationContext(null)));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_emptyDestinationStringIsProvided() {
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> activeMQDestinationResolver.resolveDestination(new JmsDestinationContext("")));
    }
}
