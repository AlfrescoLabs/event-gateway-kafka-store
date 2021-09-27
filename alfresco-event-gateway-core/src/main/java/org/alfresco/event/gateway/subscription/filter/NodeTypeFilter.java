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

import java.util.Objects;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link EventFilter} implementation that checks if an event corresponds to a specific node type.
 * <p>
 * Initially this is an internal filter implementation not offered by the gateway subscription API. That's the reason there is no corresponding factory class.
 */
public class NodeTypeFilter implements EventFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeTypeFilter.class);

    private final String acceptedNodeType;

    private NodeTypeFilter(final String acceptedNodeType) {
        this.acceptedNodeType = Objects.requireNonNull(acceptedNodeType);
    }

    /**
     * Create a {@link NodeTypeFilter} for a specific node type.
     *
     * @param nodeType given node type to be accepted by the filter
     * @return created {@link NodeTypeFilter}
     */
    public static NodeTypeFilter of(final String nodeType) {
        return new NodeTypeFilter(nodeType);
    }

    @Override
    public boolean test(RepoEvent<DataAttributes<Resource>> repoEvent) {
        Objects.requireNonNull(repoEvent);
        LOGGER.debug("Checking node type filter for event {}", repoEvent);
        boolean passed = containsNodeResource(repoEvent) &&
            acceptedNodeType.equals(((NodeResource) repoEvent.getData().getResource()).getNodeType());
        LOGGER.debug("Node type filter result: {}", passed);
        return passed;
    }

    private boolean containsNodeResource(RepoEvent<DataAttributes<Resource>> repoEvent) {
        return Objects.nonNull(repoEvent.getData()) && repoEvent.getData().getResource() instanceof NodeResource;
    }
}
