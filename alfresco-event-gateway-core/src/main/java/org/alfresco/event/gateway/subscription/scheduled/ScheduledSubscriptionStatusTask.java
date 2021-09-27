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
package org.alfresco.event.gateway.subscription.scheduled;

import java.time.Instant;
import java.util.List;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.entity.SubscriptionStatus;
import org.alfresco.event.gateway.subscription.EventSubscription;
import org.alfresco.event.gateway.subscription.EventSubscriptionService;
import org.alfresco.event.gateway.subscription.storage.EventSubscriptionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Scheduled Task to deregister the {@link EventSubscription} instance with active status but without activity.
 */
public class ScheduledSubscriptionStatusTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledSubscriptionStatusTask.class);

    private final EventSubscriptionStorage eventSubscriptionStorage;
    private final EventSubscriptionService eventSubscriptionService;
    private long limitTime;

    /**
     * Constructor.
     *
     * @param eventSubscriptionStorage given {@link EventSubscriptionStorage}
     * @param eventSubscriptionService given {@link EventSubscriptionService}
     * @param limitTime                given time in milliseconds to deactivate unmodified active subscriptions
     */
    public ScheduledSubscriptionStatusTask(EventSubscriptionStorage eventSubscriptionStorage, EventSubscriptionService eventSubscriptionService,
        long limitTime) {
        this.eventSubscriptionStorage = eventSubscriptionStorage;
        this.eventSubscriptionService = eventSubscriptionService;
        this.limitTime = limitTime;
    }

    @Scheduled(fixedDelayString = "${scheduled.subscription.fixed.delay:86400000}")
    public void checkStatusSubscription() {
        LOGGER.debug("Checking the status of the active subscriptions");
        final List<Subscription> activeSubscriptions = eventSubscriptionStorage.findSubscriptionsByStatus(SubscriptionStatus.ACTIVE);
        activeSubscriptions.forEach(subscription -> {
            final Instant instant = Instant.now();
            final long timeStampMillis = instant.toEpochMilli();
            final long differenceTime = timeStampMillis - subscription.getModifiedDate();
            if (differenceTime > limitTime) {
                LOGGER.debug("Automatically deactivating subscription {}", subscription);
                subscription.setStatus(SubscriptionStatus.INACTIVE);
                eventSubscriptionService.updateSubscription(subscription);
                LOGGER.debug("Subscription {} deactivated", subscription);
            }
        });
    }
}
