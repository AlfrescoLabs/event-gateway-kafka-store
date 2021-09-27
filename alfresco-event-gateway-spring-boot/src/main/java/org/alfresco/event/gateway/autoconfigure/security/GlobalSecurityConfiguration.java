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
package org.alfresco.event.gateway.autoconfigure.security;

import java.util.Set;
import org.alfresco.core.handler.GroupsApiClient;
import org.alfresco.event.gateway.security.ACSAuthoritiesProvider;
import org.alfresco.event.gateway.security.ACSAuthoritiesService;
import org.alfresco.event.gateway.security.ExtendedAuthoritiesKeycloakAuthenticationProvider;
import org.alfresco.event.gateway.security.HeadersForwardDelegatedAuthenticationProvider;
import org.alfresco.event.gateway.security.SubscriptionOwnerValidator;
import org.alfresco.event.gateway.subscription.EventSubscriptionService;
import org.alfresco.rest.sdk.feign.DelegatedAuthenticationProvider;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@KeycloakConfiguration
@Import({KeycloakSpringBootConfigResolver.class})
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class GlobalSecurityConfiguration extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    public GroupsApiClient groupsApiClient;

    /**
     * Registers the KeycloakAuthenticationProvider with the authentication manager.
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    /**
     * Defines the session authentication strategy.
     */
    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http
            .csrf().disable()
            .authorizeRequests()
            .anyRequest()
            .authenticated();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/actuator/health",
            "/api-docs",
            "/configuration/ui",
            "/swagger-resources/**",
            "/configuration/security",
            "/swagger-ui.html",
            "/webjars/**",
            "/swagger-ui/**");
    }

    @Override
    protected KeycloakAuthenticationProvider keycloakAuthenticationProvider() {
        return new ExtendedAuthoritiesKeycloakAuthenticationProvider(Set.of(acsAuthoritiesProvider()));
    }

    protected ACSAuthoritiesProvider acsAuthoritiesProvider() {
        return new ACSAuthoritiesProvider(acsAuthoritiesService());
    }

    @Bean
    protected ACSAuthoritiesService acsAuthoritiesService() {
        return new ACSAuthoritiesService(groupsApiClient);
    }

    @Bean
    public DelegatedAuthenticationProvider headersForwardDelegatedAuthenticationProvider() {
        // Forward the value of the current request authorization header to the feign client template
        return new HeadersForwardDelegatedAuthenticationProvider(Set.of("authorization"));
    }

    @Bean
    public SubscriptionOwnerValidator subscriptionOwnerValidator(EventSubscriptionService eventSubscriptionService) {
        return new SubscriptionOwnerValidator(eventSubscriptionService);
    }
}
