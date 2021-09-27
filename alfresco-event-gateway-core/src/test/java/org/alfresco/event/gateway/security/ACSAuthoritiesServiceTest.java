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
package org.alfresco.event.gateway.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.util.List;
import java.util.Set;
import org.alfresco.core.handler.GroupsApiClient;
import org.alfresco.core.model.Group;
import org.alfresco.core.model.GroupEntry;
import org.alfresco.core.model.GroupPaging;
import org.alfresco.core.model.GroupPagingList;
import org.alfresco.core.model.Pagination;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.alfresco.event.gateway.acs.client.exception.ACSClientException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Unit tests for {@link ACSAuthoritiesService}.
 */
public class ACSAuthoritiesServiceTest extends AbstractUnitTest {

    private static final String TEST_USER = "user";
    private static final String TEST_GROUP_ID = "group-id";

    @InjectMocks
    private ACSAuthoritiesService acsAuthoritiesService;

    @Mock
    private GroupsApiClient mockGroupsApiClient;
    @Mock
    private ResponseEntity<GroupPaging> mockResponseEntity;
    @Mock
    private GroupPaging mockGroupPaging;
    @Mock
    private GroupPagingList mockGroupPagingList;
    @Mock
    private Pagination mockPagination;

    @Test
    public void should_returnTheSetOfACSAuthoritiesForCurrentUser() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(TEST_USER, "creds"));
        SecurityContextHolder.setContext(context);
        lenient().when(mockGroupsApiClient.listGroupMembershipsForPerson(eq(TEST_USER), any(), any(), any(), any(), any(), any()))
            .thenReturn(mockResponseEntity);
        lenient().when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        lenient().when(mockResponseEntity.getBody()).thenReturn(mockGroupPaging);
        lenient().when(mockGroupPaging.getList()).thenReturn(mockGroupPagingList);
        lenient().when(mockGroupPagingList.getEntries()).thenReturn(List.of(new GroupEntry().entry(new Group().id(TEST_GROUP_ID))));
        lenient().when(mockGroupPagingList.getPagination()).thenReturn(mockPagination);
        lenient().when(mockPagination.isHasMoreItems()).thenReturn(false);

        Set<String> authorities = acsAuthoritiesService.getCurrentUserAuthorities();

        assertThat(authorities).containsOnly(TEST_GROUP_ID);
    }

    @Test
    public void should_returnTheSetOfACSAuthorities_when_properUsernameIsProvided() {
        lenient().when(mockGroupsApiClient.listGroupMembershipsForPerson(eq(TEST_USER), any(), any(), any(), any(), any(), any()))
            .thenReturn(mockResponseEntity);
        lenient().when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
        lenient().when(mockResponseEntity.getBody()).thenReturn(mockGroupPaging);
        lenient().when(mockGroupPaging.getList()).thenReturn(mockGroupPagingList);
        lenient().when(mockGroupPagingList.getEntries()).thenReturn(List.of(new GroupEntry().entry(new Group().id(TEST_GROUP_ID))));
        lenient().when(mockGroupPagingList.getPagination()).thenReturn(mockPagination);
        lenient().when(mockPagination.isHasMoreItems()).thenReturn(false);

        Set<String> authorities = acsAuthoritiesService.getUserAuthorities(TEST_USER);

        assertThat(authorities).containsOnly(TEST_GROUP_ID);
    }

    @Test
    public void should_returnTheAnEmptySetOfACSAuthorities_when_missingUsernameIsProvided() {
        lenient().when(mockGroupsApiClient.listGroupMembershipsForPerson(eq(TEST_USER), any(), any(), any(), any(), any(), any()))
            .thenReturn(mockResponseEntity);
        lenient().when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);
        lenient().when(mockPagination.isHasMoreItems()).thenReturn(false);

        Set<String> authorities = acsAuthoritiesService.getUserAuthorities(TEST_USER);

        assertThat(authorities).isEmpty();
    }

    @Test
    public void should_throwACSClientException_when_groupsResponseFromACSIsNotOk() {
        lenient().when(mockGroupsApiClient.listGroupMembershipsForPerson(eq(TEST_USER), any(), any(), any(), any(), any(), any()))
            .thenReturn(mockResponseEntity);
        lenient().when(mockResponseEntity.getStatusCode()).thenReturn(HttpStatus.BAD_GATEWAY);
        Assertions.assertThrows(ACSClientException.class, () -> acsAuthoritiesService.getUserAuthorities(TEST_USER));
    }
}
