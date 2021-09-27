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
package org.alfresco.event.gateway.subscription.rest;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.subscription.EventSubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Unit tests for {@link EventSubscriptionController}.
 */
public class EventSubscriptionControllerTest extends AbstractUnitTest {

    private static final String TEST_SUB_ID = "subscription-id";
    private static final String TEST_SUB_TYPE = "subscription-type";

    @InjectMocks
    private EventSubscriptionController eventSubscriptionController;

    @Mock
    private EventSubscriptionService mockEventSubscriptionService;
    @Mock
    private ObjectMapper mockObjectMapper;
    @Mock
    private SubscriptionPatchValidator mockSubscriptionPatchValidator;

    private Subscription mockSubscription = new Subscription();
    private JsonNode mockPatch = JsonNodeFactory.instance.objectNode();

    @BeforeEach
    void setUp() {
        mockSubscription.setType(TEST_SUB_TYPE);
        lenient().when(mockEventSubscriptionService.getSubscription(TEST_SUB_ID)).thenReturn(mockSubscription);
        lenient().when(mockEventSubscriptionService.createSubscription(mockSubscription)).thenReturn(mockSubscription);
    }

    @Test
    void should_returnSubscription_when_gettingSubscriptionById() {
        ResponseEntity<Subscription> responseEntity = eventSubscriptionController.getSubscription(TEST_SUB_ID);
        assertThat(responseEntity.getBody().getType()).isEqualTo(TEST_SUB_TYPE);
    }

    @Test
    void should_returnSubscription_when_creatingSubscription() {
        ResponseEntity<Subscription> responseEntity = eventSubscriptionController.createSubscription(mockSubscription);
        assertThat(responseEntity.getBody().getType()).isEqualTo(TEST_SUB_TYPE);
    }

    @Test
    void should_returnSubscription_when_partiallyUpdatingProperSubscription() throws Exception {
        lenient().when(mockEventSubscriptionService.getSubscription(TEST_SUB_ID)).thenReturn(mockSubscription);
        lenient().when(mockObjectMapper.valueToTree(mockSubscription)).thenReturn(mockPatch);
        lenient().when(mockObjectMapper.treeToValue(any(), eq(Subscription.class))).thenReturn(mockSubscription);
        lenient().when(mockEventSubscriptionService.updateSubscription(mockSubscription)).thenReturn(mockSubscription);

        ResponseEntity<Subscription> responseEntity = eventSubscriptionController.partiallyUpdateSubscription(TEST_SUB_ID, mockPatch);

        assertThat(responseEntity.getBody().getType()).isEqualTo(TEST_SUB_TYPE);
    }

    @Test
    void should_returnBadRequest_when_partiallyUpdatingIncorrectSubscriptionJSON() throws Exception {
        lenient().when(mockEventSubscriptionService.getSubscription(TEST_SUB_ID)).thenReturn(mockSubscription);
        lenient().when(mockObjectMapper.valueToTree(mockSubscription)).thenReturn(mockPatch);
        lenient().when(mockObjectMapper.treeToValue(any(), eq(Subscription.class))).thenThrow(JsonProcessingException.class);

        ResponseEntity<Subscription> responseEntity = eventSubscriptionController.partiallyUpdateSubscription(TEST_SUB_ID, mockPatch);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
