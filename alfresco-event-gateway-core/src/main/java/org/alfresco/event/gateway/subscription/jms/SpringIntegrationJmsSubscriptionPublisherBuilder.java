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

import java.util.Objects;
import java.util.UUID;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import org.alfresco.event.gateway.BrokerConfig;
import org.alfresco.event.gateway.subscription.RepoEventToJsonTransformer;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.handler.advice.RequestHandlerCircuitBreakerAdvice;
import org.springframework.integration.handler.advice.RequestHandlerRetryAdvice;
import org.springframework.integration.jms.dsl.Jms;
import org.springframework.retry.support.RetryTemplateBuilder;
import org.springframework.util.Assert;

/**
 * Builder for the class {@link SpringIntegrationJmsSubscriptionPublisher}.
 * <p>
 * The spring integration flow is registered on creation time using the {@link IntegrationFlowContext}. It basically defines a {@link
 * org.springframework.integration.jms.dsl.JmsOutboundChannelAdapterSpec} that sends the message (previously transformed to JSON using the transformer {@link
 * RepoEventToJsonTransformer}) to a specific JMS {@link Destination}.
 * <p>
 * The creation of this publisher requires a {@link ConnectionFactory} and a JMS {@link Destination} to know where the messages should be published.
 */
public class SpringIntegrationJmsSubscriptionPublisherBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringIntegrationJmsSubscriptionPublisherBuilder.class);

    private IntegrationFlowContext integrationFlowContext;
    private Destination destination;
    private RepoEventToJsonTransformer repoEventToJsonTransformer;
    private BrokerConfig brokerConfig;

    private SpringIntegrationJmsSubscriptionPublisherBuilder() {
        // Hide default constructor
    }

    /**
     * Provide an instance of the builder for the class {@link SpringIntegrationJmsSubscriptionPublisher}.
     *
     * @return the corresponding builder instance
     */
    public static SpringIntegrationJmsSubscriptionPublisherBuilder getInstance() {
        return new SpringIntegrationJmsSubscriptionPublisherBuilder();
    }

    public SpringIntegrationJmsSubscriptionPublisherBuilder integrationFlowContext(IntegrationFlowContext integrationFlowContext) {
        this.integrationFlowContext = integrationFlowContext;
        return this;
    }

    public SpringIntegrationJmsSubscriptionPublisherBuilder destination(Destination destination) {
        this.destination = destination;
        return this;
    }

    public SpringIntegrationJmsSubscriptionPublisherBuilder repoEventToJsonTransformer(RepoEventToJsonTransformer repoEventToJsonTransformer) {
        this.repoEventToJsonTransformer = repoEventToJsonTransformer;
        return this;
    }

    public SpringIntegrationJmsSubscriptionPublisherBuilder brokerConfig(BrokerConfig brokerConfig) {
        this.brokerConfig = brokerConfig;
        return this;
    }

    /**
     * Build a new {@link SpringIntegrationJmsSubscriptionPublisher}.
     *
     * @return the corresponding publisher instance
     */
    public SpringIntegrationJmsSubscriptionPublisher build() {
        // Check mandatory fields
        checkMandatory();
        // Register flow
        IntegrationFlowRegistration integrationFlowRegistration = registerIntegrationFlow(repoEventToJsonTransformer, destination);
        // once the flow is registered, create a messaging template to be able to send messages to it
        MessagingTemplate messagingTemplate = createMessagingTemplate(integrationFlowRegistration.getId());
        // create the subscription publisher object
        return new SpringIntegrationJmsSubscriptionPublisher(integrationFlowContext, integrationFlowRegistration, messagingTemplate);
    }

    private void checkMandatory() {
        Assert.notNull(integrationFlowContext, "IntegrationFlowContext is mandatory");
        Assert.notNull(destination, "JMS Destination is mandatory");
        Assert.notNull(repoEventToJsonTransformer, "RepoEventToJsonTransformer is mandatory");
        Assert.notNull(brokerConfig, "Broker configuration is mandatory");
    }

    private IntegrationFlowRegistration registerIntegrationFlow(final RepoEventToJsonTransformer repoEventToJsonTransformer, final Destination destination) {
        LOGGER.debug("Registering a new spring integration flow to publish event to JMS destination {}", destination);
        return integrationFlowContext.registration(defineIntegrationFlow(repoEventToJsonTransformer, destination))
            .id("SpringIntegrationJmsSubscriptionPublisherBuilder#" + UUID.randomUUID())
            .register();
    }

    private MessagingTemplate createMessagingTemplate(final String flowId) {
        LOGGER.debug("Creating a messaging template to be able to send messages to the registered flow {}", flowId);
        return integrationFlowContext.messagingTemplateFor(flowId);
    }

    private IntegrationFlow defineIntegrationFlow(final RepoEventToJsonTransformer repoEventToJsonTransformer, final Destination destination) {
        return f -> f
            .transform(repoEventToJsonTransformer)
            .handle(Jms.outboundAdapter(buildConnectionFactory())
                .destination(destination), s -> s.advice(buildCircuitBreaker()).advice(buildRetry()));
    }

    private ConnectionFactory buildConnectionFactory() {
        LOGGER.debug("Creating ActiveMQ connection factory from broker config {}", brokerConfig);
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerConfig.getUrl());
        if (Objects.nonNull(brokerConfig.getUsername())) {
            connectionFactory.setUserName(brokerConfig.getUsername());
        }
        if (Objects.nonNull(brokerConfig.getPassword())) {
            connectionFactory.setPassword(brokerConfig.getPassword());
        }
        return connectionFactory;
    }

    private RequestHandlerCircuitBreakerAdvice buildCircuitBreaker() {
        RequestHandlerCircuitBreakerAdvice circuitBreakerAdvice = new RequestHandlerCircuitBreakerAdvice();
        if (Objects.nonNull(brokerConfig.getCircuitBreakerThreshold())) {
            circuitBreakerAdvice.setThreshold(brokerConfig.getCircuitBreakerThreshold());
        }
        if (Objects.nonNull(brokerConfig.getCircuitBreakerHalfOpenAfter())) {
            circuitBreakerAdvice.setHalfOpenAfter(brokerConfig.getCircuitBreakerThreshold());
        }
        return circuitBreakerAdvice;
    }

    private RequestHandlerRetryAdvice buildRetry() {
        RequestHandlerRetryAdvice requestHandlerRetryAdvice = new RequestHandlerRetryAdvice();
        RetryTemplateBuilder retryTemplateBuilder = new RetryTemplateBuilder();
        if (Objects.nonNull(brokerConfig.getRetryMaxAttempts())) {
            retryTemplateBuilder.maxAttempts(brokerConfig.getRetryMaxAttempts());
        }
        if (brokerConfig.isBackoffPolicySet()) {
            retryTemplateBuilder.exponentialBackoff(brokerConfig.getRetryInitialInterval(), brokerConfig.getRetryMultiplier(),
                brokerConfig.getRetryMaxInterval());
        }
        requestHandlerRetryAdvice.setRetryTemplate(retryTemplateBuilder.build());
        return requestHandlerRetryAdvice;
    }
}
