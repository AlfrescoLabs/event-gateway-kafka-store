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

/**
 * The gateway event consumer is the main entry point of the event gateway microservice. Its responsibility is to read the events from the event source in ACS,
 * transform them to standard {@link org.alfresco.repo.event.v1.model.RepoEvent} objects and route them to the {@link EventRouter} component.
 *
 * @see EventRouter
 * @see org.alfresco.repo.event.v1.model.RepoEvent
 */
@FunctionalInterface
public interface GatewayEventConsumer {

    /**
     * Start the consumption of events from the ACS event source. The implementation of this method should be idempotent, so that several invocations of it does
     * not disturb the normal consumption of events from the source.
     * <p>
     * The event gateway microservice invokes this method at the end of the microservice startup process, once the rest of services are up and ready.
     */
    void startConsumingEvents();
}
