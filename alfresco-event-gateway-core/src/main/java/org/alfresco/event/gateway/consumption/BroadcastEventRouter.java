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

import java.util.concurrent.Executor;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link EventRouter} that broadcasts any event to all the {@link EventConsumer}'s registered in the {@link EventConsumerRegistry}.
 */
public class BroadcastEventRouter implements EventRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BroadcastEventRouter.class);

    private final EventConsumerRegistry eventConsumerRegistry;
    private final Executor executor;

    /**
     * Constructor.
     *
     * @param eventConsumerRegistry given {@link EventConsumerRegistry}
     * @param executor              given {@link Executor}
     */
    public BroadcastEventRouter(final EventConsumerRegistry eventConsumerRegistry, final Executor executor) {
        this.eventConsumerRegistry = eventConsumerRegistry;
        this.executor = executor;
    }

    @Override
    public void routeEvent(RepoEvent<DataAttributes<Resource>> event) {
        LOGGER.debug("Routing the event {}", event);
        eventConsumerRegistry.getAll()
            .forEach(eventConsumer -> this.executeConsumer(eventConsumer, event));
    }

    private void executeConsumer(final EventConsumer eventConsumer, RepoEvent<DataAttributes<Resource>> event) {
        executor.execute(() -> this.invokeConsumer(eventConsumer, event));
    }

    private void invokeConsumer(final EventConsumer eventConsumer, RepoEvent<DataAttributes<Resource>> event) {
        try {
            eventConsumer.consumeEvent(event);
        } catch (Exception e) {
            LOGGER.error(String.format("Error invoking the consumer %s with the event %s", eventConsumer, event), e);
        }
    }
}
