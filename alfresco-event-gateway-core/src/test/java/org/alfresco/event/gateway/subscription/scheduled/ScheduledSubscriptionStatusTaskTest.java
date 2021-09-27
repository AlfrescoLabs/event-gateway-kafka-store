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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.entity.SubscriptionStatus;
import org.alfresco.event.gateway.subscription.EventSubscriptionService;
import org.alfresco.event.gateway.subscription.storage.EventSubscriptionStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;

/**
 * Unit tests for {@link ScheduledSubscriptionStatusTask}.
 */
public class ScheduledSubscriptionStatusTaskTest extends AbstractUnitTest {

    private final static String TEST_ID = "Test";
    private final static long PREVIOUS_DATE_TIMESTAMP = 1629034200;

    @Mock
    private EventSubscriptionStorage mockEventSubscriptionStorage;
    @Mock
    private EventSubscriptionService mockEventSubscriptionService;

    @Captor
    private ArgumentCaptor<Subscription> subscriptionCaptor;

    private ScheduledSubscriptionStatusTask scheduledSubscriptionStatusTask;

    @BeforeEach
    public void setup() {
        scheduledSubscriptionStatusTask = new ScheduledSubscriptionStatusTask(mockEventSubscriptionStorage, mockEventSubscriptionService, 86400000);
    }

    @Test
    public void should_deactivateTheSubscription_when_notUpdatedWithinTheConfiguredPeriod() {
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setId(TEST_ID);
        subscription.setModifiedDate(PREVIOUS_DATE_TIMESTAMP);
        when(mockEventSubscriptionStorage.findSubscriptionsByStatus(SubscriptionStatus.ACTIVE)).thenReturn(List.of(subscription));

        scheduledSubscriptionStatusTask.checkStatusSubscription();

        verify(mockEventSubscriptionService).updateSubscription(subscriptionCaptor.capture());
        assertThat(subscriptionCaptor.getValue().getStatus()).isEqualTo(SubscriptionStatus.INACTIVE);
    }

    @Test
    public void should_notDeactivateTheSubscription_when_updatedWithinTheConfiguredPeriod() {
        Subscription subscription = new Subscription();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setId(TEST_ID);
        subscription.setModifiedDate(System.currentTimeMillis());
        when(mockEventSubscriptionStorage.findSubscriptionsByStatus(SubscriptionStatus.ACTIVE)).thenReturn(List.of(subscription));

        scheduledSubscriptionStatusTask.checkStatusSubscription();

        verify(mockEventSubscriptionService, never()).updateSubscription(any(Subscription.class));
    }
}
