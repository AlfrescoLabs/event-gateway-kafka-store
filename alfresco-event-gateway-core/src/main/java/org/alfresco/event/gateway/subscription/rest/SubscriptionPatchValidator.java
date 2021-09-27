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

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validation class that checks that a subscription patch contains only fields included in a set of configured fields. Initially, just the STATUS field of a
 * {@link org.alfresco.event.gateway.entity.Subscription} is valid for a subscription patch.
 * <p>
 * If validation fails, then a {@link SubscriptionConfigurationException} is thrown.
 */
public class SubscriptionPatchValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionPatchValidator.class);
    private static final String STATUS_FIELD = "status";
    private static final Set<String> ALLOWED_FIELDS = Set.of(STATUS_FIELD);

    /**
     * Validate a subscription patch. If validation fails, then a {@link SubscriptionConfigurationException} is thrown.
     *
     * @param subscriptionPatch the subscription patch to validate
     */
    public void validatePatch(final JsonNode subscriptionPatch) {
        Objects.nonNull(subscriptionPatch);
        LOGGER.debug("Checking the validity of the subscription patch {}", subscriptionPatch);
        List<String> patchFields = getPatchFields(subscriptionPatch);
        if (!patchFieldsAreValid(patchFields)) {
            String errorMessage = String.format("Partial update only allows the modification of the fields [%s]", String.join(",", ALLOWED_FIELDS));
            LOGGER.error(errorMessage);
            throw new SubscriptionConfigurationException(errorMessage);
        } else {
            LOGGER.debug("Valid subscription patch");
        }
    }

    private List<String> getPatchFields(final JsonNode patch) {
        List<String> patchFields = new ArrayList<>();
        patch.fieldNames().forEachRemaining(patchFields::add);
        return patchFields;
    }

    private boolean patchFieldsAreValid(final List<String> patchFields) {
        return patchFieldsSizeIsCorrect(patchFields.size()) && allPatchFieldsAreAllowed(patchFields);
    }

    private boolean patchFieldsSizeIsCorrect(final int patchFieldsSize) {
        return patchFieldsSize > 0 && patchFieldsSize <= ALLOWED_FIELDS.size();
    }

    private boolean allPatchFieldsAreAllowed(final List<String> patchFields) {
        return patchFields.stream().allMatch(ALLOWED_FIELDS::contains);
    }
}
