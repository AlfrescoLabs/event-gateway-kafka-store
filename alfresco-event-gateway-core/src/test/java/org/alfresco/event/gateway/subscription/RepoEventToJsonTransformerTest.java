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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.subscription.exception.EventPublicationException;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * Unit tests for {@link RepoEventToJsonTransformer}.
 */
public class RepoEventToJsonTransformerTest extends AbstractUnitTest {

    private static final RepoEvent<? extends DataAttributes<? extends Resource>> TEST_REPO_EVENT = RepoEvent.<EventData<NodeResource>>builder().build();

    @InjectMocks
    private RepoEventToJsonTransformer repoEventToJsonTransformer;

    @Mock
    private ObjectMapper mockObjectMapper;

    @Test
    public void should_returnJSONEventRepresentation_when_properRepoEventIsReceived() throws Exception {
        String jsonRepoEvent = "{}";
        given(mockObjectMapper.writeValueAsString(TEST_REPO_EVENT)).willReturn(jsonRepoEvent);

        String response = repoEventToJsonTransformer.transform((RepoEvent<DataAttributes<Resource>>) TEST_REPO_EVENT);

        assertThat(response).isEqualTo(jsonRepoEvent);
    }

    @Test
    public void should_throwEventPublicationException_when_incorrectRepoEventIsReceived() {
        Assertions.assertThrows(EventPublicationException.class, () -> {
            given(mockObjectMapper.writeValueAsString(TEST_REPO_EVENT)).willThrow(new JsonEOFException(null, null, ""));

            repoEventToJsonTransformer.transform((RepoEvent<DataAttributes<Resource>>) TEST_REPO_EVENT);
        });
    }
}
