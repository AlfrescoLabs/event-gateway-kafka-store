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

import org.alfresco.event.gateway.subscription.SubscriptionPublisher;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.support.MessageBuilder;

/**
 * Implementation of {@link SubscriptionPublisher} that makes use of Spring Integration to publish the {@link RepoEvent} objects to a JMS destination.
 * <p>
 * The <code>release</code> method simply remove the Spring Integration flow using the {@link IntegrationFlowContext}. That subsequently releases all the
 * resources (spring beans, connections and caches) of the corresponding flow.
 */
public class SpringIntegrationJmsSubscriptionPublisher implements SubscriptionPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringIntegrationJmsSubscriptionPublisher.class);

    private IntegrationFlowContext integrationFlowContext;
    private IntegrationFlowRegistration integrationFlowRegistration;
    private MessagingTemplate messagingTemplate;

    /**
     * Constructor.
     *
     * @param integrationFlowContext      given {@link IntegrationFlowContext}
     * @param integrationFlowRegistration given {@link IntegrationFlowRegistration}
     * @param messagingTemplate           given {@link MessagingTemplate}
     */
    SpringIntegrationJmsSubscriptionPublisher(final IntegrationFlowContext integrationFlowContext,
        final IntegrationFlowRegistration integrationFlowRegistration, final MessagingTemplate messagingTemplate) {
        this.integrationFlowContext = integrationFlowContext;
        this.integrationFlowRegistration = integrationFlowRegistration;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishEvent(RepoEvent<DataAttributes<Resource>> event) {
        LOGGER.debug("Publishing the event {} to JMS", event);
        messagingTemplate.send(MessageBuilder.withPayload(event).build());
    }

    @Override
    public void release() {
        String flowId = integrationFlowRegistration.getId();
        LOGGER.debug("Releasing the resources of the flow {}", flowId);
        integrationFlowContext.remove(flowId);
    }
}
