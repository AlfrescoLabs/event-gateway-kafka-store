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
package org.alfresco.event.gateway.subscription.filter;

import org.alfresco.event.gateway.entity.Filter;
import org.alfresco.event.gateway.entity.Subscription;

/**
 * The event filter factory has the responsibility of creating new event filter objects taking into account the specification provided by the client using a
 * {@link Subscription} object.
 */
@FunctionalInterface
public interface EventFilterFactory {

    /**
     * Obtain an instance of {@link EventFilter} given the requested subscription requirements.
     *
     * @param subscription given {@link Subscription} specification
     * @param filter       given {@link Filter} specification
     * @return the corresponding {@link EventFilter} object
     */
    EventFilter getEventFilter(Subscription subscription, Filter filter);
}
