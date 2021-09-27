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
package org.alfresco.event.gateway.subscription.jms;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.BrokerConfig;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.core.env.Environment;

/**
 * Unit tests for {@link SpringPropertiesBrokerConfigResolver}.
 */
public class SpringPropertiesBrokerConfigResolverTest extends AbstractUnitTest {

    private static final String TEST_BROKER_CONFIG_ID = "test-broker-config-id";
    private static final String TEST_BROKER_URL = "broker-url";
    private static final String TEST_USERNAME = "username";
    private static final String TEST_PWD = "password";
    private static final String TEST_DESTINATION_PATTERN = "*";
    private static final String TEST_CIRCUIT_THRESHOLD = "1";
    private static final String TEST_CIRCUIT_OPEN = "10000";
    private static final String TEST_RETRY_MAX_ATTEMPTS = "5";
    private static final String TEST_RETRY_INIT_INTERVAL = "10";
    private static final String TEST_RETRY_MULTIPLIER = "2";
    private static final String TEST_RETRY_MAX_INTERVAL = "1000";

    @InjectMocks
    private SpringPropertiesBrokerConfigResolver springPropertiesBrokerConfigResolver;

    @Mock
    private Environment mockEnvironment;

    @Test
    public void should_resolveProperBrokerConfiguration_when_allMandatoryPropertiesAreSet() {
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.broker-url")).willReturn(TEST_BROKER_URL);

        BrokerConfig brokerConfig = springPropertiesBrokerConfigResolver.resolveBrokerConfig(TEST_BROKER_CONFIG_ID);

        assertThat(brokerConfig.getUrl()).isEqualTo(TEST_BROKER_URL);
    }

    @Test
    public void should_resolveProperBrokerConfiguration_when_allPropertiesAreSet() {
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.broker-url")).willReturn(TEST_BROKER_URL);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.username")).willReturn(TEST_USERNAME);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.password")).willReturn(TEST_PWD);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.destination-pattern"))
            .willReturn(TEST_DESTINATION_PATTERN);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.circuit-threshold"))
            .willReturn(TEST_CIRCUIT_THRESHOLD);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.circuit-halfOpenAfter"))
            .willReturn(TEST_CIRCUIT_OPEN);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.retry-maxAttempts"))
            .willReturn(TEST_RETRY_MAX_ATTEMPTS);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.retry-initInterval"))
            .willReturn(TEST_RETRY_INIT_INTERVAL);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.retry-multiplier"))
            .willReturn(TEST_RETRY_MULTIPLIER);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.retry-maxInterval"))
            .willReturn(TEST_RETRY_MAX_INTERVAL);

        BrokerConfig brokerConfig = springPropertiesBrokerConfigResolver.resolveBrokerConfig(TEST_BROKER_CONFIG_ID);

        assertThat(brokerConfig.getUrl()).isEqualTo(TEST_BROKER_URL);
        assertThat(brokerConfig.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(brokerConfig.getPassword()).isEqualTo(TEST_PWD);
        assertThat(brokerConfig.getDestinationPattern()).isEqualTo(TEST_DESTINATION_PATTERN);
        assertThat(brokerConfig.getCircuitBreakerThreshold()).isEqualTo(Integer.parseInt(TEST_CIRCUIT_THRESHOLD));
        assertThat(brokerConfig.getCircuitBreakerHalfOpenAfter()).isEqualTo(Integer.parseInt(TEST_CIRCUIT_OPEN));
        assertThat(brokerConfig.getRetryMaxAttempts()).isEqualTo(Integer.parseInt(TEST_RETRY_MAX_ATTEMPTS));
        assertThat(brokerConfig.getRetryInitialInterval()).isEqualTo(Integer.parseInt(TEST_RETRY_INIT_INTERVAL));
        assertThat(brokerConfig.getRetryMultiplier()).isEqualTo(Integer.parseInt(TEST_RETRY_MULTIPLIER));
        assertThat(brokerConfig.getRetryMaxInterval()).isEqualTo(Integer.parseInt(TEST_RETRY_MAX_INTERVAL));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_nullBrokerConfigIdIsProvided() {
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> springPropertiesBrokerConfigResolver.resolveBrokerConfig(null));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_emptyBrokerConfigIdIsProvided() {
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> springPropertiesBrokerConfigResolver.resolveBrokerConfig(StringUtils.EMPTY));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_nullBrokerUrlIsProvided() {
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.broker-url")).willReturn(null);
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> springPropertiesBrokerConfigResolver.resolveBrokerConfig(TEST_BROKER_CONFIG_ID));
    }

    @Test
    public void should_throwSubscriptionConfigurationException_when_emptyBrokerUrlIsProvided() {
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.broker-url")).willReturn(StringUtils.EMPTY);
        Assertions.assertThrows(SubscriptionConfigurationException.class,
            () -> springPropertiesBrokerConfigResolver.resolveBrokerConfig(TEST_BROKER_CONFIG_ID));
    }

    @Test
    public void should_throwNumberFormatException_when_incorrectIntegerPropertyValueIsProvided() {
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.broker-url")).willReturn(TEST_BROKER_URL);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.username")).willReturn(TEST_USERNAME);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.password")).willReturn(TEST_PWD);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.destination-pattern"))
            .willReturn(TEST_DESTINATION_PATTERN);
        given(mockEnvironment.getProperty("alfresco.event.gateway.publication.jms.broker.test-broker-config-id.circuit-threshold")).willReturn("test");
        Assertions.assertThrows(NumberFormatException.class,
            () -> springPropertiesBrokerConfigResolver.resolveBrokerConfig(TEST_BROKER_CONFIG_ID));
    }
}
