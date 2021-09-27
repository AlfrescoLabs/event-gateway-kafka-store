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
package org.alfresco.event.gateway.subscription;

import java.util.ArrayList;
import java.util.List;
import org.alfresco.event.gateway.subscription.filter.EventFilter;

/**
 * The event subscription component is in charge of publishing the events consumed from ACS to other channels. In order to do that, it makes use of instances of
 * {@link SubscriptionPublisher}.
 */
public interface EventSubscription {

    /**
     * Get the list of {@link EventFilter} that applies to the event subscription. Defaulted to an empty list.
     *
     * @return the list of {@link EventFilter}
     */
    default List<EventFilter> getEventFilters() {
        return new ArrayList<>();
    }

    /**
     * Method invoked when the publication resources must be released.
     */
    default void release() {
    }
}
