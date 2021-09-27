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

import static com.fasterxml.jackson.databind.MapperFeature.USE_ANNOTATIONS;
import static org.alfresco.event.gateway.SubscriptionConfigurationConstants.SUBSCRIPTION_TYPE_JMS_ACTIVEMQ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.alfresco.event.gateway.AbstractIT;
import org.alfresco.event.gateway.SubscriptionConfigurationConstants;
import org.alfresco.event.gateway.entity.Filter;
import org.alfresco.event.gateway.entity.Subscription;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@AutoConfigureMockMvc
@WithMockUser(authorities = {"GROUP_TEST"})
public class SubscriptionIT extends AbstractIT {

    private static final String BROKER_ID_VALUE = "test-broker";
    private static final String DESTINATION = "test-destination";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Deserializes subscription configuration as a Map, not as top-level properties
     */
    private final ObjectMapper basicObjectMapper = new ObjectMapper().configure(USE_ANNOTATIONS, false);

    @Test
    public void should_createAndFetchAJmsActiveMQSubscription() throws Exception {
        Subscription subscription = createJmsActiveMQSubscriptionModel();

        MvcResult postResult = mockMvc.perform(post("/v1/subscriptions")
            .content(basicObjectMapper.writeValueAsString(subscription))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        Subscription createdSubscription = objectMapper
            .readValue(postResult.getResponse().getContentAsString(), Subscription.class);

        MvcResult getResult = mockMvc.perform(get("/v1/subscriptions/{id}", createdSubscription.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        Subscription fetchedSubscription = objectMapper
            .readValue(getResult.getResponse().getContentAsString(), Subscription.class);

        assertThat(createdSubscription).isEqualTo(fetchedSubscription);

    }

    @Test
    void should_returnResponseWithNotFoundStatusCode_when_fetchingNonExistentSubscriptions() throws Exception {
        mockMvc.perform(get("/v1/subscriptions/{id}", "nonExistentSubscriptionId")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andReturn();
    }

    @Test
    void should_returnResponseWithBadRequestStatusCode_when_creatingSubscriptionsWithUnsupportedTypes() throws Exception {
        Subscription subscription = new Subscription();
        String unsupportedType = "unsupportedType";
        subscription.setType(unsupportedType);
        subscription.setUser("test-user");

        MvcResult result = mockMvc.perform(post("/v1/subscriptions")
            .content(basicObjectMapper.writeValueAsString(subscription))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(Objects.requireNonNull(result.getResolvedException()).getMessage())
            .contains(String.format("Subscription type %s is not supported", unsupportedType));

    }

    @Test
    void should_returnResponseWithBadRequestStatusCode_when_creatingSubscriptionsWithUnsupportedFilterType() throws Exception {
        Subscription subscription = createJmsActiveMQSubscriptionModel();
        Filter filter = new Filter();
        String unsupportedFilterType = "unsupportedFilterType";
        filter.setType(unsupportedFilterType);
        subscription.setFilters(List.of(filter));

        MvcResult result = mockMvc.perform(post("/v1/subscriptions")
            .content(basicObjectMapper.writeValueAsString(subscription))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andReturn();

        assertThat(Objects.requireNonNull(result.getResolvedException()).getMessage())
            .contains(String.format("Filter type %s is not supported", unsupportedFilterType));
    }

    @Test
    void should_triggerValidationErrorsAndReturnResponseWithUnprocessableEntityStatusCode_when_creatingSubscriptionsWithoutType() throws Exception {
        String nonValidJSONString = "{\"foo\":\"bar\", \"waldo\":\"wombat\"}";

        MvcResult result = mockMvc.perform(post("/v1/subscriptions")
            .content(nonValidJSONString)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

        assertThat(Objects.requireNonNull((MethodArgumentNotValidException) result.getResolvedException())
            .getBindingResult()
            .getAllErrors()
            .stream()
            .map(ObjectError::getDefaultMessage)
            .collect(Collectors.toList()))
            .hasSameElementsAs(List.of("Subscription type must not be null or empty"));
    }

    private Subscription createJmsActiveMQSubscriptionModel() {
        Subscription subscription = new Subscription();

        subscription.setType(SUBSCRIPTION_TYPE_JMS_ACTIVEMQ);

        Map<String, String> subscriptionConfig = new HashMap<>();
        subscriptionConfig.put(SubscriptionConfigurationConstants.BROKER_ID, BROKER_ID_VALUE);
        subscriptionConfig.put(SubscriptionConfigurationConstants.DESTINATION, DESTINATION);
        subscription.setConfig(subscriptionConfig);

        subscription.setUser("test-user");

        return subscription;
    }
}
