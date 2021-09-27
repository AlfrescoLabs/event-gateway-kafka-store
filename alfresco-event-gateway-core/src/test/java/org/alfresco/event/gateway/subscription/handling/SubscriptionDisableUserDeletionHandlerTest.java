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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.entity.SubscriptionStatus;
import org.alfresco.event.gateway.subscription.EventSubscriptionService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for {@link SubscriptionDisableUserDeletionHandler}.
 */
public class SubscriptionDisableUserDeletionHandlerTest extends AbstractUnitTest {

    private static final String TEST_USER = "test-user";

    @InjectMocks
    private SubscriptionDisableUserDeletionHandler subscriptionDisableUserDeletionHandler;

    @Mock
    private EventSubscriptionService mockEventSubscriptionService;
    @Mock
    private Subscription mockSubscription;

    @Test
    public void should_disableSubscriptionsOwnedByTheDeletedUser() {
        given(mockEventSubscriptionService.findSubscriptionsByUserAndStatus(TEST_USER, SubscriptionStatus.ACTIVE))
            .willReturn(List.of(mockSubscription));

        subscriptionDisableUserDeletionHandler.userDeleted(TEST_USER);

        verify(mockSubscription).setStatus(SubscriptionStatus.INACTIVE);
        verify(mockEventSubscriptionService).updateSubscription(mockSubscription);
    }

    @Test
    public void should_notDisableAnySubscriptions_when_theDeletedUserDoesNotOwnAnySubscription() {
        given(mockEventSubscriptionService.findSubscriptionsByUserAndStatus(TEST_USER, SubscriptionStatus.ACTIVE))
            .willReturn(Collections.emptyList());

        subscriptionDisableUserDeletionHandler.userDeleted(TEST_USER);

        verify(mockEventSubscriptionService, never()).updateSubscription(any());
    }
}
