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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.event.gateway.subscription.exception.EventPublicationException;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.transformer.GenericTransformer;

/**
 * {@link GenericTransformer} implementation that transforms a {@link RepoEvent} object into a repository event in JSON format (following the Repo Event JSON
 * schema).
 */
public class RepoEventToJsonTransformer implements GenericTransformer<RepoEvent<DataAttributes<Resource>>, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepoEventToJsonTransformer.class);

    private final ObjectMapper objectMapper;

    /**
     * Constructor.
     *
     * @param objectMapper given {@link ObjectMapper}
     */
    public RepoEventToJsonTransformer(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String transform(final RepoEvent<DataAttributes<Resource>> repoEvent) {
        LOGGER.debug("Transforming repo event {}", repoEvent);
        try {
            return objectMapper.writeValueAsString(repoEvent);
        } catch (final JsonProcessingException excp) {
            LOGGER.error("An error occurred transforming the repo event {}", repoEvent);
            throw new EventPublicationException("An error occurred transforming the repo event", excp);
        }
    }
}
