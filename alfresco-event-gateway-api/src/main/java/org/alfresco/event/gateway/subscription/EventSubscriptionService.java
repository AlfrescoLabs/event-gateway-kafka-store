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

import java.util.List;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.entity.SubscriptionStatus;

/**
 * Service in charge of dealing with the internal logic of the gateway
 */
public interface EventSubscriptionService {

    /**
     * Fetches a given {@link Subscription} based on Id
     *
     * @param subscriptionId the id of the {@link Subscription} to be returned
     * @return a {@link Subscription} with the subscription configuration of a given client application
     */
    Subscription getSubscription(String subscriptionId);

    /**
     * Creates a {@link Subscription} with the given configuration passed as a {@link Subscription} object
     *
     * @param subscription the configuration needed to create a {@link Subscription}
     * @return the created {@link Subscription}
     */
    Subscription createSubscription(Subscription subscription);

    /**
     * Updates a {@link Subscription} item based on the <code>subscription</code> parameter
     *
     * @param subscription the new subscription information to be updated
     * @return the updated {@link Subscription} object
     */
    Subscription updateSubscription(Subscription subscription);

    /**
     * Refreshes the {@link EventSubscription} instance corresponding to a subscription identifier.
     *
     * @param subscriptionId the identifier of the subscription to be refreshed
     */
    void refreshEventSubscription(String subscriptionId);

    /**
     * Find a list of {@link Subscription} filtered by the given username and containing at least one filter of a filter type
     *
     * @param user       the username by which a {@link Subscription} is going to be filtered
     * @param filterType the type of filter to search for
     * @return A list of {@link Subscription} matching with the requirements
     */
    List<Subscription> findSubscriptionsByUserAndFilterType(String user, String filterType);

    /**
     * Find a list of {@link Subscription} filtered by the given user and {@link SubscriptionStatus}
     *
     * @param user               the username by which a {@link Subscription} is going to be filtered
     * @param subscriptionStatus the subscription status to search for
     * @return A list of {@link Subscription} matching with the requirements
     */
    List<Subscription> findSubscriptionsByUserAndStatus(String user, SubscriptionStatus subscriptionStatus);

    /**
     * Deregister the {@link EventSubscription} instance corresponding to a subscription identifier.
     *
     * @param subscriptionId the identifier of the subscription to be deregistered
     */
    void unregisterEventSubscription(String subscriptionId);
}
