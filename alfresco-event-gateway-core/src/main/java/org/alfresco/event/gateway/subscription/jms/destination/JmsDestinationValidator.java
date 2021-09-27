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
package org.alfresco.event.gateway.subscription.jms.destination;

import java.util.regex.PatternSyntaxException;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component in charge of the validation of a JMS destination. This version simply validates the destination name against a regular expression, if one has been
 * set in the {@link JmsDestinationContext}.
 */
public class JmsDestinationValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JmsDestinationValidator.class);
    private static final String USERNAME_PLACEHOLDER = "\\{username\\}";

    /**
     * Validate a designation context. If the designation is invalid a {@link org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException}
     * is thrown.
     *
     * @param jmsDestinationContext given {@link JmsDestinationContext} to validate
     */
    public void validateDestination(final JmsDestinationContext jmsDestinationContext) {
        LOGGER.debug("Validating destination {}", jmsDestinationContext);
        if (StringUtils.isNotBlank(jmsDestinationContext.getDestinationPattern())) {
            validateDestinationPattern(jmsDestinationContext);
        }
        LOGGER.debug("Valid destination {}", jmsDestinationContext);
    }

    private void validateDestinationPattern(final JmsDestinationContext jmsDestinationContext) {
        String destinationName = jmsDestinationContext.getDestinationName();
        if (StringUtils.isBlank(destinationName)) {
            throw new SubscriptionConfigurationException("Empty JMS destination name provided");
        }
        String regexp = buildRegExp(jmsDestinationContext);

        boolean valid;
        try {
            valid = destinationName.matches(regexp);
        } catch (PatternSyntaxException excp) {
            LOGGER.error(String.format("Invalid JMS destination pattern %s", regexp), excp);
            throw new SubscriptionConfigurationException(String.format("Invalid JMS destination pattern %s", regexp));
        }

        if (!valid) {
            throw new SubscriptionConfigurationException(String.format("JMS destination name %s does not matches the pattern %s", destinationName, regexp));
        }
    }

    private String buildRegExp(final JmsDestinationContext jmsDestinationContext) {
        String destinationPattern = jmsDestinationContext.getDestinationPattern();
        String username = jmsDestinationContext.getUsername();
        LOGGER.debug("Building regexp pattern from destination pattern {} and username {}", destinationPattern, username);
        return destinationPattern.replaceAll(USERNAME_PLACEHOLDER, username);
    }
}
