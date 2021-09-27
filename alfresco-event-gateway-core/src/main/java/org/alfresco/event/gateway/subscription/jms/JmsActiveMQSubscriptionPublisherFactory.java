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

import java.util.Map;
import java.util.Objects;
import org.alfresco.event.gateway.BrokerConfig;
import org.alfresco.event.gateway.SubscriptionConfigurationConstants;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.subscription.RepoEventToJsonTransformer;
import org.alfresco.event.gateway.subscription.SubscriptionPublisher;
import org.alfresco.event.gateway.subscription.SubscriptionPublisherFactory;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.alfresco.event.gateway.subscription.jms.destination.ActiveMQDestinationResolver;
import org.alfresco.event.gateway.subscription.jms.destination.JmsDestinationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.dsl.context.IntegrationFlowContext;

/**
 * Implementation of {@link SubscriptionPublisherFactory} that creates {@link SpringIntegrationJmsSubscriptionPublisher} objects for ActiveMQ destinations.
 */
public class JmsActiveMQSubscriptionPublisherFactory implements SubscriptionPublisherFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsActiveMQSubscriptionPublisherFactory.class);

    private final IntegrationFlowContext integrationFlowContext;
    private final RepoEventToJsonTransformer repoEventToJsonTransformer;
    private final ActiveMQDestinationResolver activeMQDestinationResolver;
    private final BrokerConfigResolver brokerConfigResolver;

    /**
     * Constructor.
     *
     * @param integrationFlowContext      given {@link IntegrationFlowContext}
     * @param repoEventToJsonTransformer  given {@link RepoEventToJsonTransformer}
     * @param activeMQDestinationResolver given {@link ActiveMQDestinationResolver}
     */
    public JmsActiveMQSubscriptionPublisherFactory(final IntegrationFlowContext integrationFlowContext,
        final RepoEventToJsonTransformer repoEventToJsonTransformer, final ActiveMQDestinationResolver activeMQDestinationResolver,
        final BrokerConfigResolver brokerConfigResolver) {
        this.integrationFlowContext = integrationFlowContext;
        this.repoEventToJsonTransformer = repoEventToJsonTransformer;
        this.activeMQDestinationResolver = activeMQDestinationResolver;
        this.brokerConfigResolver = brokerConfigResolver;
    }

    @Override
    public SubscriptionPublisher getSubscriptionPublisher(Subscription subscription) {
        Objects.requireNonNull(subscription);
        LOGGER.debug("Building subscription publisher from subscription {}", subscription);

        Map<String, String> subscriptionConfig = subscription.getConfig();
        if (Objects.isNull(subscriptionConfig)) {
            throw new SubscriptionConfigurationException("No subscription configuration found");
        }

        BrokerConfig brokerConfig = brokerConfigResolver.resolveBrokerConfig(subscriptionConfig.get(SubscriptionConfigurationConstants.BROKER_ID));
        JmsDestinationContext jmsDestinationContext = new JmsDestinationContext(subscriptionConfig.get(SubscriptionConfigurationConstants.DESTINATION));
        jmsDestinationContext.setUsername(subscription.getUser());
        jmsDestinationContext.setDestinationPattern(brokerConfig.getDestinationPattern());

        return SpringIntegrationJmsSubscriptionPublisherBuilder.getInstance()
            .integrationFlowContext(integrationFlowContext)
            .destination(activeMQDestinationResolver.resolveDestination(jmsDestinationContext))
            .repoEventToJsonTransformer(repoEventToJsonTransformer)
            .brokerConfig(brokerConfig)
            .build();
    }
}
