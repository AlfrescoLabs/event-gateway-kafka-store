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
package org.alfresco.event.gateway.subscription;

import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;

/**
 * The subscription publisher is the component in charge of publishing the {@link RepoEvent} objects to the corresponding channels. The definition of what a
 * channel is will depend on each specific implementation of this interface (i.e. a JMS topic, a webhook or a web socket).
 */
@FunctionalInterface
public interface SubscriptionPublisher {

    /**
     * Publish a {@link RepoEvent}.
     *
     * @param event the {@link RepoEvent} to be published
     */
    void publishEvent(RepoEvent<DataAttributes<Resource>> event);

    /**
     * Method invoked when the publication resources must be release. This method is called when the subscription that is using it is cancelled/removed.
     */
    default void release() {
    }
}
