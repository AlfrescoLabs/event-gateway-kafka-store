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

import feign.RequestTemplate;
import java.util.Collections;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.alfresco.event.gateway.AbstractUnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Unit tests for {@link HeadersForwardDelegatedAuthenticationProvider}.
 */
public class HeadersForwardDelegatedAuthenticationProviderTest extends AbstractUnitTest {

    private static final String TEST_HEADER = "test-header";
    private static final String TEST_HEADER_VALUE = "test-header-value";

    private HeadersForwardDelegatedAuthenticationProvider headersForwardDelegatedAuthenticationProvider;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @BeforeEach
    public void setup() {
        headersForwardDelegatedAuthenticationProvider = new HeadersForwardDelegatedAuthenticationProvider(Set.of(TEST_HEADER));
    }

    @Test
    public void should_forwardOnlyConfiguredHeaders_when_settingAuthenticationForRestTemplate() {
        RequestTemplate requestTemplate = new RequestTemplate();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));
        given(mockHttpServletRequest.getHeaderNames()).willReturn(Collections.enumeration(Set.of(TEST_HEADER, "another")));
        given(mockHttpServletRequest.getHeader(TEST_HEADER)).willReturn(TEST_HEADER_VALUE);

        headersForwardDelegatedAuthenticationProvider.setAuthentication(requestTemplate);

        assertThat(requestTemplate.headers().get(TEST_HEADER)).containsOnly(TEST_HEADER_VALUE);
        assertThat(requestTemplate.headers()).doesNotContainKeys("another");
    }
}
