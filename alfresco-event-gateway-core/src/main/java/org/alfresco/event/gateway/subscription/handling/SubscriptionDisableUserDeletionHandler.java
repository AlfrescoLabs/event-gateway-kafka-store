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
package org.alfresco.event.gateway.subscription.handling;

import org.alfresco.event.gateway.consumption.handling.UserDeletionHandler;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.entity.SubscriptionStatus;
import org.alfresco.event.gateway.subscription.EventSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UserDeletionHandler} implementation that disables all the subscriptions owned by the deleted user.
 */
public class SubscriptionDisableUserDeletionHandler implements UserDeletionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionDisableUserDeletionHandler.class);

    private final EventSubscriptionService eventSubscriptionService;

    /**
     * Constructor.
     *
     * @param eventSubscriptionService given {@link EventSubscriptionService}
     */
    public SubscriptionDisableUserDeletionHandler(final EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

    @Override
    public void userDeleted(String username) {
        LOGGER.debug("Checking if the user {} is the owner of any active subscription", username);
        eventSubscriptionService.findSubscriptionsByUserAndStatus(username, SubscriptionStatus.ACTIVE)
            .forEach(this::disableSubscription);
    }

    private void disableSubscription(Subscription subscription) {
        LOGGER.debug("Disabling subscription {} due to its owner has been deleted", subscription);
        subscription.setStatus(SubscriptionStatus.INACTIVE);
        eventSubscriptionService.updateSubscription(subscription);
    }
}
