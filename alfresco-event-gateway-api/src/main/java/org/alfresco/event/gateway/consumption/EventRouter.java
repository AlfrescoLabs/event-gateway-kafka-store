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

import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;

/**
 * The {@link GatewayEventConsumer} forwards the {@link RepoEvent} to the event router that uses the {@link EventConsumerRegistry} to subsequently send the
 * event to the corresponding {@link EventConsumer}/s.
 */
@FunctionalInterface
public interface EventRouter {

    /**
     * Route an event to the corresponding {@link EventConsumer}/s.
     *
     * @param event the {@link RepoEvent} that is routed to the consumer/s
     */
    void routeEvent(RepoEvent<DataAttributes<Resource>> event);
}
