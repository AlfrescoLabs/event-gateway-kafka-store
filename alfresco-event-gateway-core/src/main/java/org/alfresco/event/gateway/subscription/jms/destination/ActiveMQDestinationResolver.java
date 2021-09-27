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

import javax.jms.Queue;
import javax.jms.Topic;
import org.alfresco.event.gateway.bootstrapping.SystemBootstrapChecker;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete implementation of {@link JmsDestinationResolver} for ActiveMQ broker.
 */
public class ActiveMQDestinationResolver extends JmsDestinationResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveMQDestinationResolver.class);

    /**
     * Constructor.
     *
     * @param jmsDestinationValidator given {@link JmsDestinationValidator}
     * @param systemBootstrapChecker  given {@link SystemBootstrapChecker}
     */
    public ActiveMQDestinationResolver(final JmsDestinationValidator jmsDestinationValidator, final SystemBootstrapChecker systemBootstrapChecker) {
        super(jmsDestinationValidator, systemBootstrapChecker);
    }

    @Override
    protected Topic createTopic(String topicName) {
        LOGGER.debug("Creating topic with name {}", topicName);
        return new ActiveMQTopic(topicName);
    }

    @Override
    protected Queue createQueue(String queueName) {
        LOGGER.debug("Creating queue with name {}", queueName);
        return new ActiveMQQueue(queueName);
    }
}
