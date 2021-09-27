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

import javax.jms.Destination;
import javax.jms.Queue;
import javax.jms.Topic;
import org.alfresco.event.gateway.bootstrapping.SystemBootstrapChecker;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to resolve a string to a JMS destination (topic or queue). This class is abstract, the concrete classes determines the specific implementation used for
 * the {@link Destination} objects.
 * <p>
 * The format of the destination string must be <code>DESTINATION_TYPE:DESTINATION_NAME</code> (i.e. topic:my-topic or queue:my-queue). By default, when no
 * destination type is provided, then a topic is created.
 */
public abstract class JmsDestinationResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsDestinationResolver.class);
    private static final String DESTINATION_SEPARATOR = ":";
    private static final String TOPIC = "topic";
    private static final String QUEUE = "queue";

    private final JmsDestinationValidator jmsDestinationValidator;
    private final SystemBootstrapChecker systemBootstrapChecker;

    /**
     * Constructor.
     *
     * @param jmsDestinationValidator given {@link JmsDestinationValidator}
     * @param systemBootstrapChecker  given {@link SystemBootstrapChecker}
     */
    public JmsDestinationResolver(final JmsDestinationValidator jmsDestinationValidator, final SystemBootstrapChecker systemBootstrapChecker) {
        this.jmsDestinationValidator = jmsDestinationValidator;
        this.systemBootstrapChecker = systemBootstrapChecker;
    }

    /**
     * Resolve a JMS destination from a string following the pattern <code>DESTINATION_TYPE:DESTINATION_NAME</code>.
     *
     * @param destinationContext the destination context
     * @return the resolved {@link Destination}
     * @throws SubscriptionConfigurationException when an empty destination string is provided
     */
    public Destination resolveDestination(JmsDestinationContext destinationContext) {
        if (StringUtils.isEmpty(destinationContext.getDestination())) {
            throw new SubscriptionConfigurationException("Empty JMS destination provided");
        }
        LOGGER.debug("Resolving destination {}", destinationContext.getDestination());
        return resolveDestinationInternal(destinationContext);
    }

    private Destination resolveDestinationInternal(JmsDestinationContext destinationContext) {
        String destination = destinationContext.getDestination();
        String[] destinationParts = destinationContext.getDestination().split(DESTINATION_SEPARATOR, 2);
        boolean topicWanted = true;
        if (destinationParts.length == 1) {
            // by default create topic
            destinationContext.setDestinationName(destination);
        } else {
            switch (destinationParts[0]) {
                case TOPIC:
                    destinationContext.setDestinationName(destinationParts[1]);
                    break;
                case QUEUE:
                    destinationContext.setDestinationName(destinationParts[1]);
                    topicWanted = false;
                    break;
                default:
                    // by default create topic
                    destinationContext.setDestinationName(destination);
            }
        }
        return createDestination(destinationContext, topicWanted);
    }

    private Destination createDestination(JmsDestinationContext destinationContext, boolean topicWanted) {
        if (!systemBootstrapChecker.isBootstrapping()) {
            // Just validate destinations for new/activated subscriptions, not for the existing ones being loaded on startup
            jmsDestinationValidator.validateDestination(destinationContext);
        }
        String destinationName = destinationContext.getDestinationName();
        return topicWanted ? createTopic(destinationName) : createQueue(destinationName);
    }

    /**
     * Create a topic with a specific name.
     *
     * @param topicName the topic name
     * @return the created topic
     */
    protected abstract Topic createTopic(final String topicName);

    /**
     * Create a queue with a specific name.
     *
     * @param queueName the queue name
     * @return the created queue
     */
    protected abstract Queue createQueue(final String queueName);
}
