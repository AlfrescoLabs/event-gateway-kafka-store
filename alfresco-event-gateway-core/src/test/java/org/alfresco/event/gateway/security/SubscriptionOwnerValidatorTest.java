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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.subscription.EventSubscriptionService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for {@link SubscriptionOwnerValidator}.
 */
public class SubscriptionOwnerValidatorTest extends AbstractUnitTest {

    private static final String TEST_ID = "test-id";
    private static final String TEST_USER = "test-user";

    @InjectMocks
    private SubscriptionOwnerValidator subscriptionOwnerValidator;

    @Mock
    private EventSubscriptionService mockEventSubscriptionService;

    @Test
    public void should_returnReturnTrue_when_subscriptionOwnerAndCurrentUserMatch() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(TEST_USER, "creds"));
        SecurityContextHolder.setContext(context);
        Subscription subscription = new Subscription();
        subscription.setUser(TEST_USER);
        given(mockEventSubscriptionService.getSubscription(TEST_ID)).willReturn(subscription);

        assertThat(subscriptionOwnerValidator.currentUserOwnsSubscription(TEST_ID)).isTrue();
    }

    @Test
    public void should_returnReturnFalse_when_subscriptionOwnerAndCurrentUserDoNotMatch() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken("another-user", "creds"));
        SecurityContextHolder.setContext(context);
        Subscription subscription = new Subscription();
        subscription.setUser(TEST_USER);
        given(mockEventSubscriptionService.getSubscription(TEST_ID)).willReturn(subscription);

        assertThat(subscriptionOwnerValidator.currentUserOwnsSubscription(TEST_ID)).isFalse();
    }
}
