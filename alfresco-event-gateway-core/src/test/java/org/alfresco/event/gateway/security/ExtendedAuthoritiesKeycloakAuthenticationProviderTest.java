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

import java.util.List;
import java.util.Set;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.adapters.tomcat.SimplePrincipal;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Unit tests for {@link ExtendedAuthoritiesKeycloakAuthenticationProvider}.
 */
public class ExtendedAuthoritiesKeycloakAuthenticationProviderTest extends AbstractUnitTest {

    private static final String TEST_USER = "test-user";
    private static final String TEST_KEYCLOAK_AUTHORITY = "test-keycloak";
    private static final String TEST_EXTENDED_AUTHORITY = "test-extended";

    private ExtendedAuthoritiesKeycloakAuthenticationProvider extendedAuthoritiesKeycloakAuthenticationProvider;

    @Mock
    private AuthoritiesProvider mockAuthoritiesProvider;
    @Mock
    private KeycloakAuthenticationToken mockKeycloakAuthenticationToken;
    @Mock
    private OidcKeycloakAccount mockKeycloakAccount;

    @BeforeEach
    public void setup() {
        extendedAuthoritiesKeycloakAuthenticationProvider = new ExtendedAuthoritiesKeycloakAuthenticationProvider(Set.of(mockAuthoritiesProvider));
    }

    @Test
    public void should_addExtendedAuthoritiesToTheAuthenticationObject() {
        GrantedAuthority keyCloakGrantedAuthority = new SimpleGrantedAuthority(TEST_KEYCLOAK_AUTHORITY);
        GrantedAuthority extendedGrantedAuthority = new SimpleGrantedAuthority(TEST_EXTENDED_AUTHORITY);
        given(mockKeycloakAuthenticationToken.getAccount()).willReturn(mockKeycloakAccount);
        given(mockKeycloakAccount.getPrincipal()).willReturn(new SimplePrincipal(TEST_USER));
        given(mockKeycloakAccount.getRoles()).willReturn(Set.of(TEST_KEYCLOAK_AUTHORITY));
        given(mockAuthoritiesProvider.getAuthorities(mockKeycloakAuthenticationToken)).willReturn(List.of(extendedGrantedAuthority));

        Authentication authentication = extendedAuthoritiesKeycloakAuthenticationProvider.authenticate(mockKeycloakAuthenticationToken);

        List<GrantedAuthority> authorities = (List<GrantedAuthority>) authentication.getAuthorities();
        assertThat(authorities).containsOnly(keyCloakGrantedAuthority, extendedGrantedAuthority);
    }

    @Test
    public void should_throwNullPointerException_when_nullAuthenticationIsProvided() {
        Assertions.assertThrows(NullPointerException.class, () -> extendedAuthoritiesKeycloakAuthenticationProvider.authenticate(null));
    }
}
