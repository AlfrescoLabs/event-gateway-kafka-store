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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SubscriptionPatchValidator}.
 */
public class SubscriptionPatchValidatorTest {

    private SubscriptionPatchValidator subscriptionPatchValidator = new SubscriptionPatchValidator();

    @Test
    public void should_notThrowAnyValidationException_when_properPatchIsProvided() {
        ObjectNode subscriptionPatch = JsonNodeFactory.instance.objectNode();
        subscriptionPatch.set("status", JsonNodeFactory.instance.textNode("Active"));

        subscriptionPatchValidator.validatePatch(subscriptionPatch);
    }

    @Test
    public void should_throwNullPointerException_when_nullPatchIsProvided() {
        Assertions.assertThrows(NullPointerException.class, () -> subscriptionPatchValidator.validatePatch(null));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_emptyPatchIsProvided() {
        Assertions
            .assertThrows(SubscriptionConfigurationException.class, () -> subscriptionPatchValidator.validatePatch(JsonNodeFactory.instance.objectNode()));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_patchWithInvalidFieldsIsProvided() {
        ObjectNode subscriptionPatch = JsonNodeFactory.instance.objectNode();
        subscriptionPatch.set("status", JsonNodeFactory.instance.textNode("Active"));
        subscriptionPatch.set("invalid", JsonNodeFactory.instance.textNode("value"));
        Assertions
            .assertThrows(SubscriptionConfigurationException.class, () -> subscriptionPatchValidator.validatePatch(subscriptionPatch));
    }
}
