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
package org.alfresco.event.gateway.subscription.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import org.alfresco.event.gateway.entity.Subscription;
import org.alfresco.event.gateway.subscription.EventSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller managing {@link Subscription} CRUD operations
 */
@Tag(name = "subscriptions", description = "Retrieve and manage event subscriptions")
@SecurityRequirements({
    @SecurityRequirement(name = "basic-auth"),
    @SecurityRequirement(name = "bearer-key")
})
@RequestMapping(
    value = "${alfresco.event.gateway.api.base-path}/subscriptions")
@Timed
public class EventSubscriptionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventSubscriptionController.class);

    private final EventSubscriptionService eventSubscriptionService;
    private final ObjectMapper objectMapper;
    private final SubscriptionPatchValidator subscriptionPatchValidator;

    /**
     * Constructor.
     *
     * @param eventSubscriptionService   the given {@link EventSubscriptionService} to handle the operations on {@link Subscription}
     * @param objectMapper               given {@link ObjectMapper}
     * @param subscriptionPatchValidator given {@link SubscriptionPatchValidator}
     */
    public EventSubscriptionController(EventSubscriptionService eventSubscriptionService, ObjectMapper objectMapper,
        SubscriptionPatchValidator subscriptionPatchValidator) {
        this.eventSubscriptionService = eventSubscriptionService;
        this.objectMapper = objectMapper;
        this.subscriptionPatchValidator = subscriptionPatchValidator;
    }

    @PostAuthorize("(returnObject.body.user == authentication.name) OR hasAuthority(@environment.getProperty('alfresco.event.gateway.subscription.security.managers.group'))")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Get a Subscription by its id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "404", description = "Subscription not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<Subscription> getSubscription(@Parameter(description = "The subscription id") @PathVariable String id) {
        return new ResponseEntity<>(eventSubscriptionService.getSubscription(id), HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority(@environment.getProperty('alfresco.event.gateway.subscription.security.group'))")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Create a Subscription")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid subscription format or unsupported subscription or filter type", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "422", description = "Invalid subscription or filter configuration", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<Subscription> createSubscription(
        @Parameter(description = "The subscription object to be created") @RequestBody @Valid Subscription eventSubscription) {
        return new ResponseEntity<>(eventSubscriptionService.createSubscription(eventSubscription), HttpStatus.OK);
    }

    @PreAuthorize("@subscriptionOwnerValidator.currentUserOwnsSubscription(#id) OR hasAuthority(@environment.getProperty('alfresco.event.gateway.subscription.security.managers.group'))")
    @PatchMapping(value = "/{id}", consumes = "application/merge-patch+json", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Update the status of a Subscription")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successful operation"),
        @ApiResponse(responseCode = "400", description = "Invalid request format", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
        @ApiResponse(responseCode = "422", description = "Invalid request attributes", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    public ResponseEntity<Subscription> partiallyUpdateSubscription(@Parameter(description = "The subscription id") @PathVariable String id,
        @Parameter(description = "The JSON snippet holding the new status of the subscription") @RequestBody JsonNode patch) {
        try {
            Subscription patchedSubscription = patchSubscription(id, patch);
            return new ResponseEntity<>(eventSubscriptionService.updateSubscription(patchedSubscription), HttpStatus.OK);
        } catch (JsonPatchException | JsonProcessingException e) {
            LOGGER.error("Error reading/applying the patch subscription update", e);
            return ResponseEntity.badRequest().build();
        }
    }

    private Subscription patchSubscription(final String id, final JsonNode patch) throws JsonProcessingException, JsonPatchException {
        subscriptionPatchValidator.validatePatch(patch);
        JsonNode original = objectMapper.valueToTree(eventSubscriptionService.getSubscription(id));
        JsonNode patchedSubscriptionNode = JsonMergePatch.fromJson(patch).apply(original);
        return objectMapper.treeToValue(patchedSubscriptionNode, Subscription.class);
    }
}
