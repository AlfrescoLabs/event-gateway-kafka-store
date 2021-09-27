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

import java.util.Objects;
import javax.jms.Destination;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.jms.dsl.Jms;

/**
 * Implementation of {@link GatewayEventConsumer} that makes use of Spring Integration to consume the ACS events from a configured topic in ActiveMQ.
 * <p>
 * The spring integration flow is registered at runtime using the {@link IntegrationFlowContext} when the method <code>startConsumingEvents</code> is invoked.
 * It basically defines a {@link org.springframework.integration.jms.dsl.JmsMessageDrivenChannelAdapterSpec} that queries the topic in ActiveMQ configured in
 * the property <code>alfresco.event.gateway.consumer.inputTopic</code> (defaulted to <code>alfresco.repo.event2</code>).
 * <p>
 * Once the JSON messages are retrieved from ActiveMQ, they are transformed to the event model and forwarded to the {@link EventRouter} to be distributed
 * appropriately.
 */
public class SpringIntegrationGatewayEventConsumer implements GatewayEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringIntegrationGatewayEventConsumer.class);

    private final IntegrationFlowContext integrationFlowContext;
    private final ActiveMQConnectionFactory activeMQConnectionFactory;
    private final JsonToRepoEventTransformer jsonToRepoEventTransformer;
    private final EventRouter eventRouter;
    private final Destination inputDestination;
    private final Boolean durableSubscription;

    private IntegrationFlowRegistration integrationFlowRegistration;

    /**
     * Constructor.
     *
     * @param integrationFlowContext     given {@link IntegrationFlowContext}
     * @param activeMQConnectionFactory  given {@link ActiveMQConnectionFactory}
     * @param jsonToRepoEventTransformer given {@link JsonToRepoEventTransformer}
     * @param eventRouter                given {@link EventRouter}
     * @param inputDestination           given input destination
     * @param durableSubscription        given durable subscription value
     */
    public SpringIntegrationGatewayEventConsumer(final IntegrationFlowContext integrationFlowContext,
        ActiveMQConnectionFactory activeMQConnectionFactory,
        JsonToRepoEventTransformer jsonToRepoEventTransformer,
        EventRouter eventRouter,
        final Destination inputDestination,
        final Boolean durableSubscription) {
        this.integrationFlowContext = integrationFlowContext;
        this.activeMQConnectionFactory = activeMQConnectionFactory;
        this.jsonToRepoEventTransformer = jsonToRepoEventTransformer;
        this.eventRouter = eventRouter;
        this.inputDestination = inputDestination;
        this.durableSubscription = durableSubscription;
    }

    @Override
    public void startConsumingEvents() {
        if (!isIntegrationFlowAlreadyRegistered()) {
            registerIntegrationFlow();
        } else {
            LOGGER.debug("Gateway event consumer spring integration flow already registered");
        }
    }

    private boolean isIntegrationFlowAlreadyRegistered() {
        return Objects.nonNull(integrationFlowRegistration);
    }

    private void registerIntegrationFlow() {
        LOGGER.debug("Registering the gateway event consumer spring integration flow");
        this.integrationFlowRegistration = this.integrationFlowContext.registration(defineIntegrationFlow())
            .id("GatewayEventConsumer")
            .register();
    }

    private IntegrationFlow defineIntegrationFlow() {
        return IntegrationFlows.from(Jms.messageDrivenChannelAdapter(activeMQConnectionFactory)
            .configureListenerContainer(container -> container.subscriptionDurable(durableSubscription))
            .destination(inputDestination))
            .transform(jsonToRepoEventTransformer)
            .log()
            .handle(m -> eventRouter.routeEvent((RepoEvent<DataAttributes<Resource>>) m.getPayload()))
            .get();
    }
}
