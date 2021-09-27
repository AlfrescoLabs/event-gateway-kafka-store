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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link AuthoritiesProvider} implementation that retrieves the ACS authorities of the current user in the security context and transform them into {@link
 * GrantedAuthority} objects.
 * <p>
 * This {@link AuthoritiesProvider} requires an HTTP servlet context and an authorization header to be able to "forward" it to ACS.
 */
public class ACSAuthoritiesProvider implements AuthoritiesProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(ACSAuthoritiesProvider.class);

    private final ACSAuthoritiesService acsAuthoritiesService;

    /**
     * Constructor.
     *
     * @param acsAuthoritiesService given {@link ACSAuthoritiesService}
     */
    public ACSAuthoritiesProvider(final ACSAuthoritiesService acsAuthoritiesService) {
        this.acsAuthoritiesService = acsAuthoritiesService;
    }

    @Override
    public List<GrantedAuthority> getAuthorities(Authentication authentication) {
        if (isApplicable()) {
            LOGGER.debug("Getting ACS authorities for current user");
            return this.acsAuthoritiesService.getCurrentUserAuthorities()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        } else {
            LOGGER.debug("No authorization header present. No authorities from ACS will be retrieved");
            return Collections.emptyList();
        }
    }

    private boolean isApplicable() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return Objects.nonNull(attributes) && Objects.nonNull(attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION));
    }
}
