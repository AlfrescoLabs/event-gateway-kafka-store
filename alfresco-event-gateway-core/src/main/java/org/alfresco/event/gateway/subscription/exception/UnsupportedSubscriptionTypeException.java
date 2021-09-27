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
package org.alfresco.event.gateway.subscription.exception;

/**
 * {@link RuntimeException} that reflects an unsupported subscription type.
 */
public class UnsupportedSubscriptionTypeException extends EventSubscriptionException {

    private String unsupportedSubscriptionType;

    /**
     * Construct a new unsupported subscription type exception with the specified message.
     *
     * @param unsupportedSubscriptionType given unsupported subscription type
     * @param message                     given message to add to the exception
     */
    public UnsupportedSubscriptionTypeException(final String unsupportedSubscriptionType, final String message) {
        super(message);
        this.unsupportedSubscriptionType = unsupportedSubscriptionType;
    }

    /**
     * Get the unsupported subscription type.
     *
     * @return the unsupported subscription type
     */
    public String getUnsupportedSubscriptionType() {
        return unsupportedSubscriptionType;
    }
}
