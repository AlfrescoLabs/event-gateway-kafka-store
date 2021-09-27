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

import java.util.Objects;
import org.alfresco.event.gateway.BrokerConfig;
import org.alfresco.event.gateway.subscription.exception.SubscriptionConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * Default implementation of {@link BrokerConfigResolver} that searches for the broker configuration in Spring properties using the {@link Environment} utility
 * class.
 * <p>
 * It looks for properties following the pattern <code>alfresco.event.gateway.publication.jms.broker.BROKER_CONFIG_ID.PROPERTY_ID</code>. For instance, to setup
 * a broker configuration with id equals to my-broker, the properties would look like:
 * <pre>
 * {@code
 * alfresco.event.gateway.publication.jms.broker.my-broker.broker-url=tcp://localhost:61616
 * alfresco.event.gateway.publication.jms.broker.my-broker.username=user
 * alfresco.event.gateway.publication.jms.broker.my-broker.password=changeit
 * alfresco.event.gateway.publication.jms.broker.my-broker.destination-pattern=*
 * alfresco.event.gateway.publication.jms.broker.my-broker.circuit-threshold=5
 * alfresco.event.gateway.publication.jms.broker.my-broker.circuit-halfOpenAfter=1000
 * alfresco.event.gateway.publication.jms.broker.my-broker.retry-maxAttempts=3
 * alfresco.event.gateway.publication.jms.broker.my-broker.retry-initInterval=1000
 * alfresco.event.gateway.publication.jms.broker.my-broker.retry-multiplier=2
 * alfresco.event.gateway.publication.jms.broker.my-broker.retry-maxInterval=5000
 * }
 * </pre>
 */
public class SpringPropertiesBrokerConfigResolver implements BrokerConfigResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpringPropertiesBrokerConfigResolver.class);
    private static final String PROPERTY_PATTERN = "alfresco.event.gateway.publication.jms.broker.%s.%s";
    private static final String BROKER_URL_PROP = "broker-url";
    private static final String USERNAME_PROP = "username";
    private static final String PASSWORD_PROP = "password";
    private static final String DESTINATION_PATTERN_PROP = "destination-pattern";
    private static final String CIRCUIT_THRESHOLD_PROP = "circuit-threshold";
    private static final String CIRCUIT_OPEN_PROP = "circuit-halfOpenAfter";
    private static final String RETRY_MAX_ATTEMPTS_PROP = "retry-maxAttempts";
    private static final String RETRY_INIT_INTERVAL_PROP = "retry-initInterval";
    private static final String RETRY_MULTIPLIER_PROP = "retry-multiplier";
    private static final String RETRY_MAX_INTERVAL_PROP = "retry-maxInterval";

    private Environment environment;

    /**
     * Constructor.
     *
     * @param environment given {@link Environment}
     */
    public SpringPropertiesBrokerConfigResolver(final Environment environment) {
        this.environment = environment;
    }

    @Override
    public BrokerConfig resolveBrokerConfig(String brokerConfigId) {
        if (StringUtils.isEmpty(brokerConfigId)) {
            throw new SubscriptionConfigurationException(String.format("Empty broker ID provided for broker ID %s", brokerConfigId));
        }
        LOGGER.debug("Resolving broker configuration from identifier {}", brokerConfigId);
        return BrokerConfig.builder()
            .url(resolveMandatoryProperty(brokerConfigId, BROKER_URL_PROP))
            .username(resolveProperty(brokerConfigId, USERNAME_PROP))
            .password(resolveProperty(brokerConfigId, PASSWORD_PROP))
            .destinationPattern(resolveProperty(brokerConfigId, DESTINATION_PATTERN_PROP))
            .circuitBreakerThreshold(resolveIntProperty(brokerConfigId, CIRCUIT_THRESHOLD_PROP))
            .circuitBreakerHalfOpenAfter(resolveIntProperty(brokerConfigId, CIRCUIT_OPEN_PROP))
            .retryMaxAttempts(resolveIntProperty(brokerConfigId, RETRY_MAX_ATTEMPTS_PROP))
            .retryInitialInterval(resolveIntProperty(brokerConfigId, RETRY_INIT_INTERVAL_PROP))
            .retryMultiplier(resolveIntProperty(brokerConfigId, RETRY_MULTIPLIER_PROP))
            .retryMaxInterval(resolveIntProperty(brokerConfigId, RETRY_MAX_INTERVAL_PROP))
            .build();
    }

    private String resolveMandatoryProperty(final String brokerConfigId, final String propertyId) {
        String propertyValue = resolveProperty(brokerConfigId, propertyId);
        if (StringUtils.isEmpty(propertyValue)) {
            throw new SubscriptionConfigurationException(String.format("Empty %s provided for broker ID %s", propertyId, brokerConfigId));
        }
        return propertyValue;
    }

    private String resolveProperty(final String brokerConfigId, final String propertyId) {
        return environment.getProperty(buildPropertyKey(brokerConfigId, propertyId));
    }

    private String buildPropertyKey(final String brokerConfigId, final String propertyId) {
        return String.format(PROPERTY_PATTERN, brokerConfigId, propertyId);
    }

    private Integer resolveIntProperty(final String brokerConfigId, final String propertyId) {
        String propertyValue = environment.getProperty(buildPropertyKey(brokerConfigId, propertyId));
        return Objects.nonNull(propertyValue) ? Integer.parseInt(propertyValue) : null;
    }
}
