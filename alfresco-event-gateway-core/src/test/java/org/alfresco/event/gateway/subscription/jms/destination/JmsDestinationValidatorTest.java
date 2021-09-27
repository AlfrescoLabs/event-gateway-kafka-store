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

import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JmsDestinationValidator}.
 */
public class JmsDestinationValidatorTest {

    private static final String TEST_USERNAME = "test";
    private static final String TEST_DESTINATION_NAME = TEST_USERNAME + "-01";
    private static final String TEST_FULL_DESTINATION = "topic:" + TEST_DESTINATION_NAME;
    private static final String TEST_DESTINATION_PATTERN = "{username}-(.+)";
    private static final String TEST_WRONG_DESTINATION_PATTERN = "[";


    private JmsDestinationValidator jmsDestinationValidator = new JmsDestinationValidator();

    @Test
    public void should_notThrowAnyValidationException_when_destinationContextWithoutPatternIsProvided() {
        jmsDestinationValidator.validateDestination(new JmsDestinationContext(TEST_FULL_DESTINATION));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_nullDestinationNameIsProvided() {
        JmsDestinationContext jmsDestinationContext = new JmsDestinationContext(TEST_FULL_DESTINATION);
        jmsDestinationContext.setDestinationPattern(TEST_DESTINATION_PATTERN);
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> jmsDestinationValidator.validateDestination(jmsDestinationContext));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_emptyDestinationNameIsProvided() {
        JmsDestinationContext jmsDestinationContext = new JmsDestinationContext(TEST_FULL_DESTINATION);
        jmsDestinationContext.setDestinationPattern(TEST_DESTINATION_PATTERN);
        jmsDestinationContext.setDestinationName(StringUtils.EMPTY);
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> jmsDestinationValidator.validateDestination(jmsDestinationContext));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_incorrectDestinationPatternIsProvided() {
        JmsDestinationContext jmsDestinationContext = new JmsDestinationContext(TEST_FULL_DESTINATION);
        jmsDestinationContext.setDestinationPattern(TEST_WRONG_DESTINATION_PATTERN);
        jmsDestinationContext.setDestinationName(TEST_DESTINATION_NAME);
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> jmsDestinationValidator.validateDestination(jmsDestinationContext));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_notMatchingDestinationNameIsProvided() {
        JmsDestinationContext jmsDestinationContext = new JmsDestinationContext(TEST_FULL_DESTINATION);
        jmsDestinationContext.setDestinationPattern(TEST_DESTINATION_PATTERN);
        jmsDestinationContext.setUsername(TEST_USERNAME);
        jmsDestinationContext.setDestinationName("not-matching");
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> jmsDestinationValidator.validateDestination(jmsDestinationContext));
    }

    @Test
    public void should_notThrowAnyValidationException_when_matchingDestinationNameIsProvided() {
        JmsDestinationContext jmsDestinationContext = new JmsDestinationContext(TEST_FULL_DESTINATION);
        jmsDestinationContext.setDestinationPattern(TEST_DESTINATION_PATTERN);
        jmsDestinationContext.setUsername(TEST_USERNAME);
        jmsDestinationContext.setDestinationName(TEST_DESTINATION_NAME);
        jmsDestinationValidator.validateDestination(jmsDestinationContext);
    }
}
