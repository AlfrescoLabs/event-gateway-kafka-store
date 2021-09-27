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
package org.alfresco.event.gateway.autoconfigure.consumption;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.concurrent.Executor;
import javax.jms.Destination;
import org.alfresco.event.gateway.consumption.BroadcastEventRouter;
import org.alfresco.event.gateway.consumption.DefaultEventConsumerRegistry;
import org.alfresco.event.gateway.consumption.EventConsumerRegistry;
import org.alfresco.event.gateway.consumption.EventConsumptionStarter;
import org.alfresco.event.gateway.consumption.EventRouter;
import org.alfresco.event.gateway.consumption.GatewayEventConsumer;
import org.alfresco.event.gateway.consumption.JsonToRepoEventTransformer;
import org.alfresco.event.gateway.consumption.LoggingEventConsumer;
import org.alfresco.event.gateway.consumption.SpringIntegrationGatewayEventConsumer;
import org.alfresco.event.gateway.consumption.UserDeletionEventConsumer;
import org.alfresco.event.gateway.consumption.handling.UserDeletionHandler;
import org.alfresco.repo.event.databind.ObjectMapperFactory;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQConnectionFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class EventConsumptionConfiguration {

    @Bean
    Destination eventGatewayMainInputDestination(@Value("${alfresco.event.gateway.consumer.inputTopic:alfresco.repo.event2}") final String inputTopic) {
        return new ActiveMQTopic(inputTopic);
    }

    @ConditionalOnMissingClass("org.alfresco.enterprise.repo.event.databind.EnterpriseObjectMapperFactory")
    @Bean
    ObjectMapper acsEventObjectMapper() {
        return ObjectMapperFactory.createInstance();
    }

    @Bean
    MessageConverter jmsMessageConverter(final ObjectMapper objectMapper) {
        MappingJackson2MessageConverter jmsMessageConverter = new MappingJackson2MessageConverter();
        jmsMessageConverter.setObjectMapper(objectMapper);
        return jmsMessageConverter;
    }

    @ConditionalOnProperty(value = "alfresco.event.gateway.consumer.activemq.durableSubscription", havingValue = "true")
    @Bean
    ActiveMQConnectionFactoryCustomizer clientIdActiveMQConnectionFactoryCustomizer(
        @Value("${alfresco.event.gateway.consumer.activemq.clientId:alfresco-event-gateway-01}") final String clientId) {
        return activeMQConnectionFactory -> activeMQConnectionFactory.setClientID(clientId);
    }

    @Bean
    GatewayEventConsumer eventConsumer(IntegrationFlowContext integrationFlowContext,
        ActiveMQConnectionFactory activeMQConnectionFactory,
        JsonToRepoEventTransformer jsonToRepoEventTransformer,
        EventRouter eventRouter,
        @Qualifier("eventGatewayMainInputDestination") Destination inputDestination,
        @Value("${alfresco.event.gateway.consumer.activemq.durableSubscription:true}") final Boolean durableSubscription) {
        return new SpringIntegrationGatewayEventConsumer(integrationFlowContext, activeMQConnectionFactory, jsonToRepoEventTransformer, eventRouter,
            inputDestination, durableSubscription);
    }

    @Bean
    EventRouter eventRouter(EventConsumerRegistry eventConsumerRegistry, @Qualifier("consumptionExecutor") Executor executor) {
        return new BroadcastEventRouter(eventConsumerRegistry, executor);
    }

    @Bean
    public Executor consumptionExecutor(@Value("${alfresco.event.gateway.consumption.executor.corePoolSize:2}") Integer corePoolSize,
        @Value("${alfresco.event.gateway.consumption.executor.maxPoolSize:2}") Integer maxPoolSize,
        @Value("${alfresco.event.gateway.consumption.executor.queueCapacity:500}") Integer queueCapacity,
        @Value("${alfresco.event.gateway.consumption.executor.threadNamePrefix:Consumption-}") String threadNamePrefix) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.initialize();
        return executor;
    }

    @Bean
    EventConsumerRegistry eventConsumerRegistry() {
        return new DefaultEventConsumerRegistry();
    }

    @Bean
    LoggingEventConsumer loggingEventConsumer(EventConsumerRegistry eventConsumerRegistry,
        @Value("${alfresco.event.gateway.loggingEventConsumer.autoRegister:false}") final Boolean autoRegisterEnabled,
        @Value("${alfresco.event.gateway.loggingEventConsumer.logLevel:INFO}") final String logLevel,
        @Value("${alfresco.event.gateway.loggingEventConsumer.messageTemplate:Consuming the event {}}") final String messageTemplate) {
        return new LoggingEventConsumer(eventConsumerRegistry, autoRegisterEnabled, logLevel, messageTemplate);
    }

    @Bean
    EventConsumptionStarter eventConsumptionStarter(GatewayEventConsumer gatewayEventConsumer) {
        return new EventConsumptionStarter(gatewayEventConsumer);
    }

    @Bean
    JsonToRepoEventTransformer jsonToRepoEventTransformer(ObjectMapper objectMapper) {
        return new JsonToRepoEventTransformer(objectMapper);
    }

    @Bean
    UserDeletionEventConsumer userDeletionEventConsumer(EventConsumerRegistry eventConsumerRegistry, Set<UserDeletionHandler> userDeletionHandlers) {
        return new UserDeletionEventConsumer(eventConsumerRegistry, userDeletionHandlers);
    }
}
