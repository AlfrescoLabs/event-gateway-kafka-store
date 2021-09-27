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
package org.alfresco.event.gateway;

import java.util.Objects;

/**
 * Domain class to represent the configuration of a JMS broker.
 */
public class BrokerConfig {

    private String url;
    private String username;
    private String password;
    private String destinationPattern;
    private Integer circuitBreakerThreshold;
    private Integer circuitBreakerHalfOpenAfter;
    private Integer retryMaxAttempts;
    private Integer retryInitialInterval;
    private Integer retryMultiplier;
    private Integer retryMaxInterval;

    /**
     * Provide an instance of the builder for the class {@link BrokerConfig}.
     *
     * @return the corresponding builder instance
     */
    public static BrokerConfigBuilder builder() {
        return new BrokerConfigBuilder();
    }

    /**
     * Get the broker url.
     *
     * @return the broker url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the broker username.
     *
     * @return the broker username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the broker password.
     *
     * @return the broker password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the broker destination pattern. Regexp to be fulfilled by any destination to be referenced in the broker.
     *
     * @return the broker destination pattern
     */
    public String getDestinationPattern() {
        return destinationPattern;
    }

    /**
     * Get the circuit breaker threshold (number of failed attempts to stop calling the broker).
     *
     * @return the circuit breaker threshold
     */
    public Integer getCircuitBreakerThreshold() {
        return circuitBreakerThreshold;
    }

    /**
     * Get the circuit breaker milliseconds to call again the broker once the circuit is open.
     *
     * @return the circuit breaker milliseconds to call again the broker once the circuit is open
     */
    public Integer getCircuitBreakerHalfOpenAfter() {
        return circuitBreakerHalfOpenAfter;
    }

    /**
     * Get the maximum number of retry attempts for the broker.
     *
     * @return the maximum number of retry attempts for the broker
     */
    public Integer getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    /**
     * Get the initial interval for a retry in milliseconds.
     *
     * @return the initial interval for a retry in milliseconds
     */
    public Integer getRetryInitialInterval() {
        return retryInitialInterval;
    }

    /**
     * Get the multiplier for a retry with exponential growth policy.
     *
     * @return the multiplier for a retry with exponential growth policy
     */
    public Integer getRetryMultiplier() {
        return retryMultiplier;
    }

    /**
     * Get the maximum interval for a retry in milliseconds.
     *
     * @return the maximum interval for a retry in milliseconds
     */
    public Integer getRetryMaxInterval() {
        return retryMaxInterval;
    }

    /**
     * Check if all the retry backoff policy attributes (<code>retryInitialInterval</code>, <code>retryMultiplier</code> and <code>retryMaxInterval</code>) are
     * set.
     *
     * @return <code>true</code> if all backoff policy attributes are set, <code>false</code> otherwise
     */
    public boolean isBackoffPolicySet() {
        return Objects.nonNull(retryInitialInterval) && Objects.nonNull(retryMultiplier) && Objects.nonNull(retryMaxInterval);
    }

    private void setUrl(String url) {
        this.url = url;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public void setDestinationPattern(String destinationPattern) {
        this.destinationPattern = destinationPattern;
    }

    private void setCircuitBreakerThreshold(Integer circuitBreakerThreshold) {
        this.circuitBreakerThreshold = circuitBreakerThreshold;
    }

    private void setCircuitBreakerHalfOpenAfter(Integer circuitBreakerHalfOpenAfter) {
        this.circuitBreakerHalfOpenAfter = circuitBreakerHalfOpenAfter;
    }

    private void setRetryMaxAttempts(Integer retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }

    private void setRetryInitialInterval(Integer retryInitialInterval) {
        this.retryInitialInterval = retryInitialInterval;
    }

    private void setRetryMultiplier(Integer retryMultiplier) {
        this.retryMultiplier = retryMultiplier;
    }

    private void setRetryMaxInterval(Integer retryMaxInterval) {
        this.retryMaxInterval = retryMaxInterval;
    }

    /**
     * {@link BrokerConfig} builder class.
     */
    public static class BrokerConfigBuilder {

        private BrokerConfig brokerConfig;

        private BrokerConfigBuilder() {
            // Hide default constructor
            brokerConfig = new BrokerConfig();
        }

        public BrokerConfigBuilder url(String url) {
            brokerConfig.setUrl(url);
            return this;
        }

        public BrokerConfigBuilder username(String username) {
            brokerConfig.setUsername(username);
            return this;
        }

        public BrokerConfigBuilder password(String password) {
            brokerConfig.setPassword(password);
            return this;
        }

        public BrokerConfigBuilder destinationPattern(String destinationPattern) {
            brokerConfig.setDestinationPattern(destinationPattern);
            return this;
        }

        public BrokerConfigBuilder circuitBreakerThreshold(Integer circuitBreakerThreshold) {
            brokerConfig.setCircuitBreakerThreshold(circuitBreakerThreshold);
            return this;
        }

        public BrokerConfigBuilder circuitBreakerHalfOpenAfter(Integer circuitBreakerHalfOpenAfter) {
            brokerConfig.setCircuitBreakerHalfOpenAfter(circuitBreakerHalfOpenAfter);
            return this;
        }

        public BrokerConfigBuilder retryMaxAttempts(Integer retryMaxAttempts) {
            brokerConfig.setRetryMaxAttempts(retryMaxAttempts);
            return this;
        }

        public BrokerConfigBuilder retryInitialInterval(Integer retryInitialInterval) {
            brokerConfig.setRetryInitialInterval(retryInitialInterval);
            return this;
        }

        public BrokerConfigBuilder retryMultiplier(Integer retryMultiplier) {
            brokerConfig.setRetryMultiplier(retryMultiplier);
            return this;
        }

        public BrokerConfigBuilder retryMaxInterval(Integer retryMaxInterval) {
            brokerConfig.setRetryMaxInterval(retryMaxInterval);
            return this;
        }

        public BrokerConfig build() {
            return brokerConfig;
        }
    }
}
