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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.keycloak.adapters.springsecurity.account.KeycloakRole;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

/**
 * Extended version of {@link KeycloakAuthenticationProvider} to add an extension mechanism that allows the inclusion of more authorities (apart from the ones
 * coming from the Keycloak auth token) via {@link AuthoritiesProvider} instances.
 */
public class ExtendedAuthoritiesKeycloakAuthenticationProvider extends KeycloakAuthenticationProvider {

    private GrantedAuthoritiesMapper grantedAuthoritiesMapper;
    private final Set<AuthoritiesProvider> authoritiesProviders;

    /**
     * Constructor.
     *
     * @param authoritiesProviders given {@link Set} of {@link AuthoritiesProvider}'s for extended authorities
     */
    public ExtendedAuthoritiesKeycloakAuthenticationProvider(final Set<AuthoritiesProvider> authoritiesProviders) {
        this.authoritiesProviders = authoritiesProviders;
    }

    public void setGrantedAuthoritiesMapper(GrantedAuthoritiesMapper grantedAuthoritiesMapper) {
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) authentication;
        return new KeycloakAuthenticationToken(token.getAccount(), token.isInteractive(), getAuthorities(token));
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return KeycloakAuthenticationToken.class.isAssignableFrom(aClass);
    }

    protected Collection<? extends GrantedAuthority> getAuthorities(KeycloakAuthenticationToken token) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        addTokenAuthorities(token, grantedAuthorities);
        addExtendedAuthorities(token, grantedAuthorities);
        return mapAuthorities(grantedAuthorities);
    }

    private void addTokenAuthorities(KeycloakAuthenticationToken token, List<GrantedAuthority> grantedAuthorities) {
        for (String role : token.getAccount().getRoles()) {
            grantedAuthorities.add(new KeycloakRole(role));
        }
    }

    private void addExtendedAuthorities(Authentication authentication, List<GrantedAuthority> grantedAuthorities) {
        authoritiesProviders.forEach(authoritiesProvider -> grantedAuthorities.addAll(authoritiesProvider.getAuthorities(authentication)));
    }

    private Collection<? extends GrantedAuthority> mapAuthorities(
        Collection<? extends GrantedAuthority> authorities) {
        return grantedAuthoritiesMapper != null
            ? grantedAuthoritiesMapper.mapAuthorities(authorities)
            : authorities;
    }
}
