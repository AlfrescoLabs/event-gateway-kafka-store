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

import feign.RequestTemplate;
import java.util.Enumeration;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.alfresco.rest.sdk.feign.DelegatedAuthenticationProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * {@link DelegatedAuthenticationProvider} implementation that forwards the value of a set of headers from the current request context to the {@link
 * RequestTemplate} used in the feign client.
 */
public class HeadersForwardDelegatedAuthenticationProvider implements DelegatedAuthenticationProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HeadersForwardDelegatedAuthenticationProvider.class);

    private final Set<String> headersToForward;

    /**
     * Constructor.
     *
     * @param headersToForward given {@link Set} of headers to forward
     */
    public HeadersForwardDelegatedAuthenticationProvider(final Set<String> headersToForward) {
        this.headersToForward = headersToForward;
    }

    @Override
    public void setAuthentication(RequestTemplate requestTemplate) {
        LOGGER.debug("Applying forward interceptor for headers {}", headersToForward);
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            handleHeaders(requestTemplate, request, headerNames);
        }
    }

    private void handleHeaders(final RequestTemplate requestTemplate, final HttpServletRequest request, final Enumeration<String> headerNames) {
        while (headerNames.hasMoreElements()) {
            handleHeader(requestTemplate, request, headerNames.nextElement());
        }
    }

    private void handleHeader(final RequestTemplate requestTemplate, final HttpServletRequest request, final String headerName) {
        if (headerRequiresForward(headerName)) {
            LOGGER.debug("Forwarding header {}", headerName);
            String values = request.getHeader(headerName);
            requestTemplate.header(headerName, values);
        }
    }

    private boolean headerRequiresForward(final String headerName) {
        return headersToForward.stream()
            .anyMatch(headerToForward -> headerToForward.equalsIgnoreCase(headerName));
    }
}
