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
package org.alfresco.event.gateway.autoconfigure.api;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    OpenAPI alfrescoEventGatewayOpenAPI(@Value("${alfresco.event.gateway.api.version}") String apiVersion,
        @Value("${alfresco.event.gateway.api.base-path}") String apiBasePath) {
        return new OpenAPI()
            .components(new Components()
                .addSecuritySchemes("basic-auth",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("basic").name("Basic Authentication").description("Basic Authentication"))
                .addSecuritySchemes("bearer-key",
                    new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").name("JWT Authentication").description("JWT Authentication")
                        .bearerFormat("JWT")))
            .info(new Info().title("Alfresco Event Gateway REST API")
                .version(apiVersion)
                .description("Provides access to the core features of the Alfresco Event Gateway."))
            .addServersItem(new Server().description("Default").url(apiBasePath));
    }

    /*
     * OpenApiCustomiser to remove the base API path from the endpoints and leave it in the server URL only.
     */
    @Bean
    OpenApiCustomiser removeApiPathOpenApiCustomiser(@Value("${alfresco.event.gateway.api.base-path}") String apiBasePath) {
        return openApi -> {
            Paths currentPaths = openApi.getPaths();
            Paths newPaths = new Paths();
            currentPaths.forEach((path, pathItem) -> newPaths.addPathItem(path.replace(apiBasePath, StringUtils.EMPTY), pathItem));
            openApi.setPaths(newPaths);
        };
    }

    /*
     * OpenApiCustomiser to add global error responses to the API operations.
     */
    @Bean
    OpenApiCustomiser globalErrorResponsesOpenApiCustomiser() {
        return openApi -> {
            openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                ApiResponses apiResponses = operation.getResponses();
                ApiResponse unauthorizedApiResponse = new ApiResponse().description("Invalid authentication provided");
                ApiResponse forbiddenApiResponse = new ApiResponse().description("User not authorized to perform the operation");
                ApiResponse internalServerApiResponse = new ApiResponse().description("Uncategorized server error");
                apiResponses.addApiResponse(HttpStatus.UNAUTHORIZED.value() + StringUtils.EMPTY, unauthorizedApiResponse);
                apiResponses.addApiResponse(HttpStatus.FORBIDDEN.value() + StringUtils.EMPTY, forbiddenApiResponse);
                apiResponses.addApiResponse(HttpStatus.INTERNAL_SERVER_ERROR.value() + StringUtils.EMPTY, internalServerApiResponse);
            }));
        };
    }

    /*
     * OpenApiCustomiser to fix the media type of the patch operations of the API, since the integration is not able to handle application/merge-patch+json.
     */
    @Bean
    OpenApiCustomiser patchMediaTypeOpenApiCustomiser() {
        Schema schema = new Schema().$ref(JsonNode.class.getSimpleName());
        MediaType mediaType = new MediaType().schema(schema);
        return openApi -> {
            openApi.getPaths().values().stream()
                .map(pathItem -> pathItem.getPatch())
                .filter(Objects::nonNull)
                .forEach(
                    patchOperation -> patchOperation.getRequestBody().setContent(
                        new Content().addMediaType("application/merge-patch+json", mediaType)));
        };
    }
}
