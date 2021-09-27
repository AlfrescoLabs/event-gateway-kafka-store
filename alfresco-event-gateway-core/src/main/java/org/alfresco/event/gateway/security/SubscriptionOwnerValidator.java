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
package org.alfresco.event.gateway.security;

import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.subscription.EventSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Component in charge of validate that the owner of a subscription matches with the current user in the security context.
 */
public class SubscriptionOwnerValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionOwnerValidator.class);

    private final EventSubscriptionService eventSubscriptionService;

    /**
     * Constructor.
     *
     * @param eventSubscriptionService given {@link EventSubscriptionService}
     */
    public SubscriptionOwnerValidator(final EventSubscriptionService eventSubscriptionService) {
        this.eventSubscriptionService = eventSubscriptionService;
    }

    /**
     * Check if the owner of a given subscription is the same as the current user in the security context.
     *
     * @param subscriptionId identifier of the subscription to check its owner
     * @return <code>true</code> if the owner of the subscription is the same as the current user in the security context, <code>false</code> otherwise
     */
    public boolean currentUserOwnsSubscription(final String subscriptionId) {
        LOGGER.debug("Checking the owner of the subscription {}", subscriptionId);
        Subscription subscription = eventSubscriptionService.getSubscription(subscriptionId);
        return subscription.getUser().equals(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
