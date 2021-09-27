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

import java.util.HashSet;
import java.util.Set;
import org.alfresco.core.handler.GroupsApiClient;
import org.alfresco.core.model.Group;
import org.alfresco.core.model.GroupEntry;
import org.alfresco.core.model.GroupPaging;
import org.alfresco.core.model.GroupPagingList;
import org.alfresco.event.gateway.acs.client.exception.ACSClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Service component in charge of retrieving authorities from ACS. It uses the ACS REST API to obtain this information.
 */
public class ACSAuthoritiesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ACSAuthoritiesService.class);
    private static final Integer PAGE_SIZE = 100;

    private final GroupsApiClient groupsApiClient;

    /**
     * Constructor.
     *
     * @param groupsApiClient given {@link GroupsApiClient}
     */
    public ACSAuthoritiesService(final GroupsApiClient groupsApiClient) {
        this.groupsApiClient = groupsApiClient;
    }

    /**
     * Get the set of ACS authorities for the current user in the security context.
     *
     * @return the set of ACS authorities for the current user
     */
    public Set<String> getCurrentUserAuthorities() {
        return getUserAuthorities(getCurrentUser());
    }

    /**
     * Get the set of ACS authorities for a given user.
     *
     * @param username given user to retrieve its ACS authorities
     * @return the set of ACS authorities for the given user
     */
    public Set<String> getUserAuthorities(String username) {
        LOGGER.debug("Getting ACS authorities for the user {}", username);
        Set<String> userAuthorities = new HashSet<>();
        getUserGroups(username, userAuthorities, 0);
        LOGGER.debug("Set of ACS authorities for the user {} -> {}", username, userAuthorities);
        return userAuthorities;
    }

    private String getCurrentUser() {
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    }

    private void getUserGroups(final String username, final Set<String> userGroups, final Integer skipCount) {
        ResponseEntity<GroupPaging> groupMembershipsResponse = groupsApiClient
            .listGroupMembershipsForPerson(username, skipCount, PAGE_SIZE, null, null, null, null);
        // Check result status code
        if (HttpStatus.OK.equals(groupMembershipsResponse.getStatusCode())) {
            GroupPagingList groupPagingList = groupMembershipsResponse.getBody().getList();
            // Add authorities
            groupPagingList
                .getEntries()
                .stream()
                .map(GroupEntry::getEntry)
                .map(Group::getId)
                .forEach(userGroups::add);
            // Check for more results
            if (groupPagingList.getPagination().isHasMoreItems()) {
                getUserGroups(username, userGroups, skipCount + PAGE_SIZE);
            }
        } else if (HttpStatus.NOT_FOUND.equals(groupMembershipsResponse.getStatusCode())) {
            // Missing user, maybe deleted, do nothing
            LOGGER.debug("404 response trying to obtain authorities for the user {}", username);
        } else {
            throw new ACSClientException(
                String.format("Error trying to get authorities for user %s. Response code %s", username, groupMembershipsResponse.getStatusCode()));
        }
    }
}
