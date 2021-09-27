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
package org.alfresco.event.gateway.storage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.alfresco.event.gateway.consumption.AbstractEventConsumer;
import org.alfresco.event.gateway.consumption.EventConsumer;
import org.alfresco.event.gateway.consumption.EventConsumerRegistry;
import org.alfresco.event.gateway.subscription.RepoEventToJsonTransformer;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.kafka.dsl.Kafka;
import org.springframework.integration.kafka.dsl.KafkaProducerMessageHandlerSpec;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.DefaultKafkaHeaderMapper;

/**
 * {@link EventStore} implementation that store the events in a Kafka cluster.
 * <p>
 * This implementation makes use of a Spring Integration flow to send the events to Kafka.
 */
public class KafkaEventStore extends AbstractEventConsumer implements EventStore, EventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaEventStore.class);
    private static final String FLOW_BASE_ID = "KafkaEventStorage#";
    private static final String PRODUCER_BASE_ID = "KafkaProducer#";
    private static final String TEMPLATE_BASE_ID = "KafkaTemplate#";
    private static final String EVENT_ID_HEADER = "event-id";

    private final IntegrationFlowContext integrationFlowContext;
    private final RepoEventToJsonTransformer repoEventToJsonTransformer;

    private final MessagingTemplate messagingTemplate;
    private final UUID internalId;

    /**
     * Constructor.
     *
     * @param eventConsumerRegistry      given {@link EventConsumerRegistry}
     * @param integrationFlowContext     given {@link IntegrationFlowContext}
     * @param repoEventToJsonTransformer given {@link RepoEventToJsonTransformer}
     * @param bootstrapServers           given Kafka bootstrap server locations
     * @param topic                      given topic to publish the events in Kafka
     */
    public KafkaEventStore(final EventConsumerRegistry eventConsumerRegistry, final IntegrationFlowContext integrationFlowContext,
        final RepoEventToJsonTransformer repoEventToJsonTransformer, final String bootstrapServers, final String topic) {
        // Auto-register as an event consumer
        super(eventConsumerRegistry, true);
        this.integrationFlowContext = integrationFlowContext;
        this.repoEventToJsonTransformer = repoEventToJsonTransformer;
        this.internalId = UUID.randomUUID();
        // Register flow
        IntegrationFlowRegistration integrationFlowRegistration = registerIntegrationFlow(bootstrapServers, topic);
        // once the flow is registered, create a messaging template to be able to send messages to it
        messagingTemplate = createMessagingTemplate(integrationFlowRegistration.getId());
    }

    @Override
    public void storeEvent(RepoEvent<DataAttributes<Resource>> event) {
        LOGGER.debug("Storing the event {}", event);
        messagingTemplate.send(MessageBuilder.withPayload(event).build());
    }

    @Override
    public void consumeEvent(RepoEvent<DataAttributes<Resource>> event) {
        LOGGER.debug("Consuming the event {}", event);
        storeEvent(event);
    }

    private IntegrationFlowRegistration registerIntegrationFlow(String bootstrapServers, String topic) {
        LOGGER.debug("Registering the Spring integration flow to send events to the Kafka cluster {} to topic {}", bootstrapServers, topic);
        return this.integrationFlowContext.registration(defineIntegrationFlow(bootstrapServers, topic))
            .id(FLOW_BASE_ID + internalId)
            .register();
    }

    private IntegrationFlow defineIntegrationFlow(String bootstrapServers, String topic) {
        return f -> f
            .enrichHeaders(h -> h.headerExpression(EVENT_ID_HEADER, "payload.id"))
            .transform(repoEventToJsonTransformer)
            .publishSubscribeChannel(c -> c
                .subscribe(sf -> sf.handle(
                    kafkaMessageHandler(bootstrapServers, topic),
                    e -> e.id(PRODUCER_BASE_ID + internalId)))
            );
    }

    private KafkaProducerMessageHandlerSpec<Integer, String, ?> kafkaMessageHandler(String bootstrapServers, String topic) {
        return Kafka
            .outboundChannelAdapter(producerFactory(bootstrapServers))
            .sync(true)
            .messageKey(m -> m
                .getHeaders()
                .get(EVENT_ID_HEADER))
            .headerMapper(new DefaultKafkaHeaderMapper())
            .partitionId(m -> 0)
            .topicExpression("headers[kafka_topic] ?: '" + topic + "'")
            .configureKafkaTemplate(t -> t.id(TEMPLATE_BASE_ID + internalId + "#" + topic));
    }

    private ProducerFactory<Integer, String> producerFactory(String bootstrapServers) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    private MessagingTemplate createMessagingTemplate(final String flowId) {
        return integrationFlowContext.messagingTemplateFor(flowId);
    }
}
