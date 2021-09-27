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
import java.util.Objects;
import javax.annotation.PostConstruct;
import org.alfresco.event.gateway.consumption.EventConsumer;
import org.alfresco.event.gateway.consumption.EventConsumerRegistry;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.entity.SubscriptionStatus;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.alfresco.event.gateway.subscription.exception.SubscriptionNotFoundException;
import org.alfresco.event.gateway.subscription.storage.EventSubscriptionStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default implementation of {@link EventSubscriptionService}.
 */
public class EventSubscriptionServiceImpl implements EventSubscriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventSubscriptionServiceImpl.class);

    private final EventSubscriptionStorage eventSubscriptionStorage;
    private final EventSubscriptionFactory eventSubscriptionFactory;
    private final EventConsumerRegistry eventConsumerRegistry;
    private final EventSubscriptionRegistry eventSubscriptionRegistry;

    /**
     * Constructor.
     *
     * @param eventSubscriptionStorage given {@link EventSubscriptionStorage}
     * @param eventSubscriptionFactory given {@link EventSubscriptionFactory}
     */
    public EventSubscriptionServiceImpl(final EventSubscriptionStorage eventSubscriptionStorage,
        final EventSubscriptionFactory eventSubscriptionFactory, final EventConsumerRegistry eventConsumerRegistry,
        final EventSubscriptionRegistry eventSubscriptionRegistry) {
        this.eventSubscriptionStorage = eventSubscriptionStorage;
        this.eventSubscriptionFactory = eventSubscriptionFactory;
        this.eventConsumerRegistry = eventConsumerRegistry;
        this.eventSubscriptionRegistry = eventSubscriptionRegistry;
    }

    /**
     * Fetches all previously persisted active {@link Subscription} and creates {@link EventSubscription} out of them
     */
    @PostConstruct
    @Transactional(readOnly = true)
    public void initializeSubscriptionsFromStorage() {
        eventSubscriptionStorage.findSubscriptionsByStatus(SubscriptionStatus.ACTIVE)
            .forEach(this::createAndRegisterEventSubscription);
    }

    @Override
    public Subscription getSubscription(String subscriptionId) {
        Subscription fetchedSubscription = eventSubscriptionStorage.getById(subscriptionId);
        LOGGER.debug("Fetching subscription {}", fetchedSubscription);
        return fetchedSubscription;
    }

    @Override
    @Transactional
    public Subscription createSubscription(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        setSubscriptionUser(subscription);
        // Subscription persistence
        Subscription subscriptionCreated = persistSubscription(subscription);
        // Event subscription registration (consumer & subscription)
        createAndRegisterEventSubscription(subscriptionCreated);
        return subscriptionCreated;
    }

    @Override
    @Transactional
    public Subscription updateSubscription(Subscription subscription) {
        Objects.requireNonNull(subscription);
        LOGGER.debug("Updating the subscription {}", subscription);
        // Explicitly set the modified date whenever this method is called to force modification
        subscription.setModifiedDate(System.currentTimeMillis());
        Subscription updatedSubscription = eventSubscriptionStorage.save(subscription);
        applyStatusToEventSubscription(subscription);
        return updatedSubscription;
    }

    @Override
    @Transactional
    public void refreshEventSubscription(String subscriptionId) {
        Objects.requireNonNull(subscriptionId);
        LOGGER.debug("Refreshing subscription with id {}", subscriptionId);

        Subscription subscription = getSubscription(subscriptionId);
        if (Objects.nonNull(subscription)) {
            unregisterEventSubscription(subscriptionId);
            createAndRegisterEventSubscription(subscription);
        } else {
            throw new SubscriptionNotFoundException(String.format("Subscription %s not found", subscriptionId));
        }
    }

    @Override
    public List<Subscription> findSubscriptionsByUserAndFilterType(String user, String filterType) {
        LOGGER.debug("Finding subscriptions by user {} and filter type {}", user, filterType);
        return eventSubscriptionStorage.findSubscriptionsByUserAndFilterType(user, filterType);
    }

    @Override
    public List<Subscription> findSubscriptionsByUserAndStatus(String user, SubscriptionStatus subscriptionStatus) {
        LOGGER.debug("Finding subscriptions by user {} and status {}", user, subscriptionStatus);
        return eventSubscriptionStorage.findSubscriptionsByUserAndStatus(user, subscriptionStatus);
    }

    @Override
    @Transactional
    public void unregisterEventSubscription(String subscriptionId) {
        LOGGER.debug("De-registering subscription with id {}", subscriptionId);
        EventSubscription eventSubscription = eventSubscriptionRegistry.getById(subscriptionId);
        if (Objects.nonNull(eventSubscription)) {
            eventSubscription.release();
            eventConsumerRegistry.deregister((EventConsumer) eventSubscription);
            eventSubscriptionRegistry.deregister(subscriptionId);
        }
    }

    private Subscription persistSubscription(Subscription subscription) {
        Subscription subscriptionPersisted = eventSubscriptionStorage.save(subscription);
        LOGGER.debug("Subscription persisted: {}", subscriptionPersisted);
        return subscriptionPersisted;
    }

    private void createAndRegisterEventSubscription(Subscription subscription) {
        if (Objects.isNull(eventSubscriptionRegistry.getById(subscription.getId()))) {
            EventSubscription eventSubscription = eventSubscriptionFactory.getEventSubscription(subscription);
            eventConsumerRegistry.register((EventConsumer) eventSubscription);
            eventSubscriptionRegistry.register(subscription.getId(), eventSubscription);
            LOGGER.debug("Event subscription registered: {}", eventSubscription);
        }
    }

    private void setSubscriptionUser(Subscription subscription) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.nonNull(authentication)) {
            subscription.setUser(authentication.getName());
        }
    }

    private void applyStatusToEventSubscription(Subscription subscription) {
        LOGGER.debug("Applying status {} to event subscription {}", subscription.getStatus(), subscription.getId());
        SubscriptionStatus subscriptionStatus = subscription.getStatus();
        if (SubscriptionStatus.ACTIVE.equals(subscriptionStatus)) {
            createAndRegisterEventSubscription(subscription);
        } else if (SubscriptionStatus.INACTIVE.equals(subscriptionStatus)) {
            unregisterEventSubscription(subscription.getId());
        } else {
            throw new SubscriptionConfigurationException(String.format("Invalid subscription status %s", subscription.getStatus()));
        }
    }
}
