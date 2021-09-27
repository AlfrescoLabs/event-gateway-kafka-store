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

import java.util.Objects;

/**
 * Context class to store the representative information in order to resolve a JMS destination.
 */
public class JmsDestinationContext {

    private String destination;
    private String destinationPattern;
    private String destinationName;
    private String username;

    /**
     * Constructor.
     *
     * @param destination given full destination string
     */
    public JmsDestinationContext(String destination) {
        this.destination = destination;
    }

    /**
     * Get the full destination string.
     *
     * @return the full destination string
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Get the destination pattern to be matched (if any).
     *
     * @return the destination pattern to be matched
     */
    public String getDestinationPattern() {
        return destinationPattern;
    }

    /**
     * Get the destination name.
     *
     * @return the destination name
     */
    public String getDestinationName() {
        return destinationName;
    }

    /**
     * Get the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the destination pattern to be matched.
     *
     * @param destinationPattern the destination pattern to be matched
     */
    public void setDestinationPattern(String destinationPattern) {
        this.destinationPattern = destinationPattern;
    }

    /**
     * Set the destination name.
     *
     * @param destinationName the destination name
     */
    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    /**
     * Set the username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JmsDestinationContext that = (JmsDestinationContext) o;
        return destination.equals(that.destination) && Objects.equals(destinationPattern, that.destinationPattern) && Objects
            .equals(destinationName, that.destinationName) && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(destination, destinationPattern, destinationName, username);
    }

    @Override
    public String toString() {
        return "JmsDestinationContext{" +
            "destination='" + destination + '\'' +
            '}';
    }
}
