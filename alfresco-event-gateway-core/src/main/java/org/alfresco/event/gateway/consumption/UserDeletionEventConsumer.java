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
package org.alfresco.event.gateway.consumption;

import java.util.Set;
import org.alfresco.event.gateway.consumption.handling.UserDeletionHandler;
import org.alfresco.event.gateway.subscription.filter.EventFilter;
import org.alfresco.event.gateway.subscription.filter.EventTypeFilter;
import org.alfresco.event.gateway.subscription.filter.NodeTypeFilter;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventType;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link EventConsumer} implementation that consumes events that implies the deletion of a user in ACS and invokes the corresponding {@link
 * UserDeletionHandler}'s.
 */
public class UserDeletionEventConsumer extends AbstractEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDeletionEventConsumer.class);
    private static final String PERSON_TYPE = "cm:person";
    private static final String USERNAME_PROPERTY = "cm:userName";
    private static final EventFilter USER_DELETION_FILTER = EventTypeFilter.of(EventType.NODE_DELETED.getType())
        .and(NodeTypeFilter.of(PERSON_TYPE));

    private final Set<UserDeletionHandler> userDeletionHandlers;

    /**
     * Constructor.
     *
     * @param eventConsumerRegistry given {@link EventConsumerRegistry}
     * @param userDeletionHandlers  given {@link Set} of {@link UserDeletionHandler}'s
     */
    public UserDeletionEventConsumer(final EventConsumerRegistry eventConsumerRegistry, final Set<UserDeletionHandler> userDeletionHandlers) {
        super(eventConsumerRegistry, true);
        this.userDeletionHandlers = userDeletionHandlers;
    }

    @Override
    public void consumeEvent(RepoEvent<DataAttributes<Resource>> repoEvent) {
        LOGGER.debug("Checking if this consumer is applicable");
        if (isApplicable(repoEvent)) {
            processEvent(repoEvent);
        }
    }

    private boolean isApplicable(RepoEvent<DataAttributes<Resource>> repoEvent) {
        return USER_DELETION_FILTER.test(repoEvent);
    }

    private void processEvent(RepoEvent<DataAttributes<Resource>> repoEvent) {
        LOGGER.debug("Processing event {}", repoEvent);
        final NodeResource nodeResource = (NodeResource) repoEvent.getData().getResource();
        final String username = (String) nodeResource.getProperties().get(USERNAME_PROPERTY);
        LOGGER.debug("Invoking handlers bound to the deletion of the user {}", username);
        userDeletionHandlers.forEach(userDeletionHandler -> userDeletionHandler.userDeleted(username));
    }
}
