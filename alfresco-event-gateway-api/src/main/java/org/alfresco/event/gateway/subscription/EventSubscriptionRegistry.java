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

/**
 * Registry that manages all the {@link EventSubscription} objects of the event gateway microservice. This component offers functionality to add, remove and
 * retrieve all the event subscriptions.
 */
public interface EventSubscriptionRegistry {

    /**
     * Add a new {@link EventSubscription} under an identifier to the registry. If the identifier was already registered, then the registration will be updated
     * with the new {@link EventSubscription}.
     *
     * @param id                the identifier to register the {@link EventSubscription} against
     * @param eventSubscription the {@link EventSubscription} to register
     */
    void register(String id, EventSubscription eventSubscription);

    /**
     * Remove an existing {@link EventSubscription} from the registry by its identifier. If the identifier is not included in the registry nothing happens.
     *
     * @param id the identifier to remove from the registry
     */
    void deregister(String id);

    /**
     * Get an {@link EventSubscription} from the registry by its identifier. If no {@link EventSubscription} is registered under that identifier,
     * <code>null</code> is returned instead.
     *
     * @param id the {@link EventSubscription} identifier to search in the registry
     * @return the corresponding {@link EventSubscription} or <code>null</code> if the identifier is not in the registry
     */
    EventSubscription getById(String id);
}
