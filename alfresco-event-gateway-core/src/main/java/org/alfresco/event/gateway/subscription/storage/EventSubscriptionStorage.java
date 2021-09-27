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
package org.alfresco.event.gateway.subscription.storage;

import java.util.List;
import java.util.Optional;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository managing {@link Subscription} CRUD operations
 */
public interface EventSubscriptionStorage extends JpaRepository<Subscription, String> {

    /**
     * Find a {@link Subscription} with the given id
     *
     * @param id Id of the {@link Subscription} to be found
     * @return Optional of {@link Subscription}
     */
    Optional<Subscription> findById(String id);

    /**
     * Find a list of {@link Subscription} filtered by the given {@link SubscriptionStatus}
     *
     * @param status The {@link SubscriptionStatus} by which a {@link Subscription} is going to be filtered
     * @return A list of {@link Subscription} with the provided {@link SubscriptionStatus}
     */
    @Query("SELECT DISTINCT s FROM Subscription s LEFT JOIN FETCH s.filters WHERE s.status = ?1")
    List<Subscription> findSubscriptionsByStatus(SubscriptionStatus status);

    /**
     * Find a list of {@link Subscription} filtered by the given username
     *
     * @param user the username by which a {@link Subscription} is going to be filtered
     * @return A list of {@link Subscription} related to the provided username
     */
    @Query("SELECT s FROM Subscription s LEFT JOIN s.filters f WHERE s.user = ?1 AND f.type = ?2")
    List<Subscription> findSubscriptionsByUserAndFilterType(String user, String filterType);

    /**
     * Find the list of different subscription owners for all the subscriptions.
     *
     * @return the list of different subscription owners for all the subscriptions
     */
    @Query("SELECT DISTINCT s.user FROM Subscription s")
    List<String> findSubscriptionOwners();

    /**
     * Find a list of {@link Subscription} filtered by the given user and {@link SubscriptionStatus}
     *
     * @param user   the username of the user by which a {@link Subscription} is going to be filtered
     * @param status The {@link SubscriptionStatus} by which a {@link Subscription} is going to be filtered
     * @return A list of {@link Subscription} owned by the provided user and with the provided {@link SubscriptionStatus}
     */
    @Query("SELECT DISTINCT s FROM Subscription s LEFT JOIN FETCH s.filters WHERE s.user = ?1 AND s.status = ?2")
    List<Subscription> findSubscriptionsByUserAndStatus(String user, SubscriptionStatus status);
}
