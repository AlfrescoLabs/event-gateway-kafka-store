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

import java.util.stream.Stream;

/**
 * Registry that manages all the {@link EventConsumer} objects of the event gateway microservice. This component offers functionality to add, remove and
 * retrieve all the event consumers.
 */
public interface EventConsumerRegistry {

    /**
     * Add a new {@link EventConsumer} to the registry if it wasn't already registered. From this moment, it starts receiving new events.
     *
     * @param eventConsumer the {@link EventConsumer} to register
     */
    void register(EventConsumer eventConsumer);

    /**
     * Remove an existing {@link EventConsumer} from the registry, so that it doesn't receive any new events.
     *
     * @param eventConsumer the {@link EventConsumer} to remove from the registry
     */
    void deregister(EventConsumer eventConsumer);

    /**
     * Retrieve all the registered {@link EventConsumer} objects in the registry.
     *
     * @return a {@link Stream} with all the {@link EventConsumer} objects of the registry
     */
    Stream<EventConsumer> getAll();
}
