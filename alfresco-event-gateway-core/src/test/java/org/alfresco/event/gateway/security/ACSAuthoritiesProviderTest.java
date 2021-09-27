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
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Unit tests for {@link ACSAuthoritiesProvider}.
 */
public class ACSAuthoritiesProviderTest extends AbstractUnitTest {

    private static final String TEST_AUTHORITY = "TEST_GROUP";

    @InjectMocks
    private ACSAuthoritiesProvider acsAuthoritiesProvider;

    @Mock
    private ACSAuthoritiesService mockACSAuthoritiesService;
    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Test
    public void should_returnTheListOfGrantedAuthorities_when_authoritiesObtainedFromACS() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));
        given(mockHttpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("test");
        given(mockACSAuthoritiesService.getCurrentUserAuthorities()).willReturn(Set.of(TEST_AUTHORITY));

        List<GrantedAuthority> authorities = acsAuthoritiesProvider
            .getAuthorities(new AnonymousAuthenticationToken("test", "test", List.of(new SimpleGrantedAuthority("TEST_ANOTHER"))));

        assertThat(authorities).containsOnly(new SimpleGrantedAuthority(TEST_AUTHORITY));
    }

    @Test
    public void should_returnAnEmptyListOfGrantedAuthorities_when_noAuthoritiesObtainedFromACS() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));
        given(mockHttpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("test");
        given(mockACSAuthoritiesService.getCurrentUserAuthorities()).willReturn(Collections.EMPTY_SET);

        List<GrantedAuthority> authorities = acsAuthoritiesProvider
            .getAuthorities(new AnonymousAuthenticationToken("test", "test", List.of(new SimpleGrantedAuthority("TEST_ANOTHER"))));

        assertThat(authorities).isEmpty();
    }

    @Test
    public void should_returnAnEmptyListOfGrantedAuthorities_when_noAuthorizationHeaderIsPresentInContext() {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));
        given(mockHttpServletRequest.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(null);

        List<GrantedAuthority> authorities = acsAuthoritiesProvider
            .getAuthorities(new AnonymousAuthenticationToken("test", "test", List.of(new SimpleGrantedAuthority("TEST_ANOTHER"))));

        assertThat(authorities).isEmpty();
    }
}
