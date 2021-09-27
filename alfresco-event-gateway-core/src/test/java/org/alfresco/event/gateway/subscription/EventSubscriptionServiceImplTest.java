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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.consumption.EventConsumer;
import org.alfresco.event.gateway.consumption.EventConsumerRegistry;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.entity.SubscriptionStatus;
import org.alfresco.event.gateway.subscription.exception.SubscriptionNotFoundException;
import org.alfresco.event.gateway.subscription.storage.EventSubscriptionStorage;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * Unit tests for {@link EventSubscriptionServiceImpl}.
 */
public class EventSubscriptionServiceImplTest extends AbstractUnitTest {

    private static final String TEST_USER = "test-user";
    private static final String TEST_SUB_ID = "generated-id";
    private static final TestEventSubscription TEST_EVENT_SUBSCRIPTION = new TestEventSubscription();
    private static final String TEST_FILTER_TYPE = "test-filter-type";

    @InjectMocks
    private EventSubscriptionServiceImpl eventSubscriptionService;

    @Mock
    private EventSubscriptionStorage eventSubscriptionStorage;
    @Mock
    private EventSubscriptionFactory eventSubscriptionFactory;
    @Mock
    private EventConsumerRegistry mockEventConsumerRegistry;
    @Mock
    private EventSubscriptionRegistry mockEventSubscriptionRegistry;

    @Test
    public void should_persistSubscriptionAndRegisterEventSubscription_when_subscriptionIsCreated() {
        //given
        Subscription subscriptionToBePersisted = new Subscription();
        subscriptionToBePersisted.setId(TEST_SUB_ID);
        when(eventSubscriptionStorage.save(subscriptionToBePersisted)).thenReturn(subscriptionToBePersisted);
        when(eventSubscriptionFactory.getEventSubscription(subscriptionToBePersisted)).thenReturn(TEST_EVENT_SUBSCRIPTION);
        //when
        eventSubscriptionService.createSubscription(subscriptionToBePersisted);
        //then
        verify(eventSubscriptionStorage).save(subscriptionToBePersisted);
        verify(mockEventConsumerRegistry).register((EventConsumer) TEST_EVENT_SUBSCRIPTION);
        verify(mockEventSubscriptionRegistry).register(TEST_SUB_ID, TEST_EVENT_SUBSCRIPTION);
    }

    @Test
    public void should_persistAuthenticatedUserInSubscription_when_subscriptionIsCreatedAndSecurityContextExists() {
        //given
        Authentication authentication = new UsernamePasswordAuthenticationToken(TEST_USER, "pwd");
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
        Subscription subscriptionToBePersisted = new Subscription();
        subscriptionToBePersisted.setId(TEST_SUB_ID);
        when(eventSubscriptionStorage.save(subscriptionToBePersisted)).thenReturn(subscriptionToBePersisted);
        //when
        eventSubscriptionService.createSubscription(subscriptionToBePersisted);
        //then
        verify(eventSubscriptionStorage).save(subscriptionToBePersisted);
        assertThat(subscriptionToBePersisted.getUser()).isEqualTo(TEST_USER);
    }

    @Test
    public void should_persistSubscriptionChangesAndRegisterEventSubscription_when_activeSubscriptionIsUpdated() {
        //given
        Subscription subscriptionToBePersisted = new Subscription();
        subscriptionToBePersisted.setId(TEST_SUB_ID);
        subscriptionToBePersisted.setStatus(SubscriptionStatus.ACTIVE);
        long previousModifiedDate = System.currentTimeMillis() - 1000;
        subscriptionToBePersisted.setModifiedDate(previousModifiedDate);
        when(eventSubscriptionStorage.save(subscriptionToBePersisted)).thenReturn(subscriptionToBePersisted);
        when(mockEventSubscriptionRegistry.getById(TEST_SUB_ID)).thenReturn(null);
        when(eventSubscriptionFactory.getEventSubscription(subscriptionToBePersisted)).thenReturn(TEST_EVENT_SUBSCRIPTION);
        //when
        eventSubscriptionService.updateSubscription(subscriptionToBePersisted);
        //then
        verify(eventSubscriptionStorage).save(subscriptionToBePersisted);
        verify(mockEventConsumerRegistry).register(TEST_EVENT_SUBSCRIPTION);
        verify(mockEventSubscriptionRegistry).register(TEST_SUB_ID, TEST_EVENT_SUBSCRIPTION);
        assertThat(previousModifiedDate).isLessThan(subscriptionToBePersisted.getModifiedDate());
    }

    @Test
    public void should_persistSubscriptionChangesAndDeRegisterEventSubscription_when_inactiveSubscriptionIsUpdated() {
        //given
        Subscription subscriptionToBePersisted = new Subscription();
        subscriptionToBePersisted.setId(TEST_SUB_ID);
        subscriptionToBePersisted.setStatus(SubscriptionStatus.INACTIVE);
        long previousModifiedDate = System.currentTimeMillis() - 1000;
        subscriptionToBePersisted.setModifiedDate(previousModifiedDate);
        when(eventSubscriptionStorage.save(subscriptionToBePersisted)).thenReturn(subscriptionToBePersisted);
        when(mockEventSubscriptionRegistry.getById(TEST_SUB_ID)).thenReturn(TEST_EVENT_SUBSCRIPTION);
        //when
        eventSubscriptionService.updateSubscription(subscriptionToBePersisted);
        //then
        verify(eventSubscriptionStorage).save(subscriptionToBePersisted);
        verify(mockEventConsumerRegistry).deregister(TEST_EVENT_SUBSCRIPTION);
        verify(mockEventSubscriptionRegistry).deregister(TEST_SUB_ID);
        assertThat(previousModifiedDate).isLessThan(subscriptionToBePersisted.getModifiedDate());
    }

    @Test
    public void should_fetchSubscription_whenGettingSubscription() {
        //given
        Subscription subscriptionToBeFetched = new Subscription();
        subscriptionToBeFetched.setId(TEST_SUB_ID);
        when(eventSubscriptionStorage.getById(TEST_SUB_ID)).thenReturn(subscriptionToBeFetched);
        //when
        eventSubscriptionService.getSubscription(TEST_SUB_ID);
        //then
        verify(eventSubscriptionStorage).getById(TEST_SUB_ID);
    }

    @Test
    public void should_getEventSubscription_whenCreatingSubscription() {
        //given
        Subscription subscriptionToBePersisted = new Subscription();
        subscriptionToBePersisted.setId(TEST_SUB_ID);
        when(eventSubscriptionStorage.save(subscriptionToBePersisted)).thenReturn(subscriptionToBePersisted);
        //when
        eventSubscriptionService.createSubscription(subscriptionToBePersisted);
        //then
        verify(eventSubscriptionFactory).getEventSubscription(subscriptionToBePersisted);
    }

    @Test
    public void should_deregisterCreateAndRegisterEventSubscription_whenRefreshingExistingSubscription() {
        //given
        Subscription subscription = new Subscription();
        subscription.setId(TEST_SUB_ID);
        when(eventSubscriptionStorage.getById(TEST_SUB_ID)).thenReturn(subscription);
        when(mockEventSubscriptionRegistry.getById(TEST_SUB_ID)).thenReturn(TEST_EVENT_SUBSCRIPTION, null);
        when(eventSubscriptionFactory.getEventSubscription(subscription)).thenReturn(TEST_EVENT_SUBSCRIPTION);
        //when
        eventSubscriptionService.refreshEventSubscription(TEST_SUB_ID);
        //then
        verify(mockEventConsumerRegistry).deregister(TEST_EVENT_SUBSCRIPTION);
        verify(mockEventSubscriptionRegistry).deregister(TEST_SUB_ID);
        verify(mockEventConsumerRegistry).register(TEST_EVENT_SUBSCRIPTION);
        verify(mockEventSubscriptionRegistry).register(TEST_SUB_ID, TEST_EVENT_SUBSCRIPTION);
    }

    @Test
    public void should_throwNullPointerException_when_refreshingEventSubscriptionWithNullId() {
        Assertions.assertThrows(NullPointerException.class, () -> eventSubscriptionService.refreshEventSubscription(null));
    }

    @Test
    public void should_throwSubscriptionNotFoundException_when_refreshingEventSubscriptionWithMissingId() {
        Assertions.assertThrows(SubscriptionNotFoundException.class, () -> eventSubscriptionService.refreshEventSubscription("another"));
    }

    @Test
    public void should_findTheSubscriptionsForAUserAndFilterType_whenSubscriptionsForAProperUserAndFilterTypeRequested() {
        //given
        Subscription subscription = new Subscription();
        when(eventSubscriptionStorage.findSubscriptionsByUserAndFilterType(TEST_USER, TEST_FILTER_TYPE)).thenReturn(List.of(subscription));
        //when
        List<Subscription> foundSubscriptions = eventSubscriptionService.findSubscriptionsByUserAndFilterType(TEST_USER, TEST_FILTER_TYPE);
        //then
        assertThat(foundSubscriptions).containsOnly(subscription);
    }

    @Test
    public void should_findTheSubscriptionsForAUserAndStatus_whenSubscriptionsForAProperUserAndStatusRequested() {
        //given
        Subscription subscription = new Subscription();
        when(eventSubscriptionStorage.findSubscriptionsByUserAndStatus(TEST_USER, SubscriptionStatus.ACTIVE)).thenReturn(List.of(subscription));
        //when
        List<Subscription> foundSubscriptions = eventSubscriptionService.findSubscriptionsByUserAndStatus(TEST_USER, SubscriptionStatus.ACTIVE);
        //then
        assertThat(foundSubscriptions).containsOnly(subscription);
    }

    private static class TestEventSubscription implements EventSubscription, EventConsumer {

        @Override
        public void consumeEvent(RepoEvent<DataAttributes<Resource>> event) {
        }
    }
}
